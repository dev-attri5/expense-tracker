package com.expense;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExpenseDAO implements InitializingBean {

    private final JdbcTemplate jdbcTemplate;
    private volatile String targetDatabase = null;
    private volatile boolean initialized = false;

    // A RowMapper converts each row from MySQL into an Expense Java object
    private final RowMapper<Expense> rowMapper = (rs, rowNum) -> {
        Expense expense = new Expense();
        expense.setId(rs.getLong("id"));
        expense.setType(rs.getString("type"));
        expense.setAmount(rs.getBigDecimal("amount"));
        expense.setCategory(rs.getString("category"));
        expense.setNote(rs.getString("note"));

        if (rs.getDate("transaction_date") != null) {
            expense.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
        }
        return expense;
    };

    public ExpenseDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        ensureTableExists();
    }

    /**
     * Checks if a database name is null, empty, or an internal MySQL system database.
     */
    private boolean isSystemDatabase(String db) {
        if (db == null || db.trim().isEmpty()) {
            return true;
        }
        String lower = db.trim().toLowerCase();
        return lower.equals("sys")
                || lower.equals("information_schema")
                || lower.equals("performance_schema")
                || lower.equals("mysql");
    }

    /**
     * Returns the target database currently in use.
     */
    public String getTargetDatabase() {
        if (!initialized || targetDatabase == null) {
            ensureTableExists();
        }
        return targetDatabase;
    }

    /**
     * Returns the fully qualified table name (e.g. `expense_tracker`.`transactions`)
     * to avoid defaulting to internal schemas like `sys`.
     */
    public String getTableName() {
        if (targetDatabase != null && !isSystemDatabase(targetDatabase)) {
            return "`" + targetDatabase + "`.`transactions`";
        }
        return "`transactions`";
    }

    /**
     * Automatically ensures the target database, table, and columns exist.
     */
    public synchronized void ensureTableExists() {
        if (initialized && targetDatabase != null && !isSystemDatabase(targetDatabase)) {
            return;
        }

        try {
            // 1. Identify currently active database
            String currentDb = null;
            try {
                currentDb = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            } catch (Exception e) {
                System.err.println("Notice querying current database: " + e.getMessage());
            }

            // 2. If connected to a system schema (e.g. sys) or no database is selected
            if (isSystemDatabase(currentDb)) {
                boolean created = false;
                // Attempt A: Create and use `expense_tracker`
                try {
                    jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `expense_tracker`");
                    jdbcTemplate.execute("USE `expense_tracker`");
                    targetDatabase = "expense_tracker";
                    created = true;
                    System.out.println("Created and switched to database: expense_tracker");
                } catch (Exception e) {
                    System.err.println("Cannot CREATE DATABASE expense_tracker (likely restricted cloud user): " + e.getMessage());
                }

                // Attempt B: If user lacks CREATE DATABASE privileges, look for available non-system databases
                if (!created) {
                    try {
                        List<String> dbs = jdbcTemplate.queryForList("SHOW DATABASES", String.class);
                        for (String db : dbs) {
                            if (!isSystemDatabase(db)) {
                                targetDatabase = db;
                                try {
                                    jdbcTemplate.execute("USE `" + db + "`");
                                } catch (Exception ignored) {}
                                System.out.println("Found and switched to user database: " + db);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Notice querying SHOW DATABASES: " + e.getMessage());
                    }
                }
            } else {
                targetDatabase = currentDb;
            }

            String tableIdentifier = getTableName();

            // 3. Create table if not exists
            String createSql = "CREATE TABLE IF NOT EXISTS " + tableIdentifier + " (" +
                               "`id` INT PRIMARY KEY AUTO_INCREMENT, " +
                               "`type` VARCHAR(10) NOT NULL, " +
                               "`amount` DECIMAL(10,2) NOT NULL, " +
                               "`category` VARCHAR(50) NOT NULL, " +
                               "`note` VARCHAR(200), " +
                               "`transaction_date` DATE NOT NULL" +
                               ")";
            jdbcTemplate.execute(createSql);

            // 4. Ensure required columns exist
            try {
                jdbcTemplate.execute("ALTER TABLE " + tableIdentifier + " ADD COLUMN `transaction_date` DATE");
            } catch (Exception ignored) {}

            try {
                jdbcTemplate.execute("ALTER TABLE " + tableIdentifier + " ADD COLUMN `category` VARCHAR(50)");
            } catch (Exception ignored) {}

            try {
                jdbcTemplate.execute("ALTER TABLE " + tableIdentifier + " ADD COLUMN `note` VARCHAR(200)");
            } catch (Exception ignored) {}

            initialized = true;
            System.out.println("Database table " + tableIdentifier + " verified successfully.");
        } catch (Exception e) {
            initialized = false;
            System.err.println("Database table check notice: " + e.getMessage());
        }
    }

    /**
     * Get all records from the database.
     */
    public List<Expense> getAllExpenses() {
        return getFilteredExpenses(null, null, null);
    }

    /**
     * Filter records by type, start date, and end date (or return all if no filters given).
     */
    public List<Expense> getFilteredExpenses(String type, LocalDate startDate, LocalDate endDate) {
        ensureTableExists();
        StringBuilder sql = new StringBuilder("SELECT `id`, `type`, `amount`, `category`, `note`, `transaction_date` FROM ")
                .append(getTableName())
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
            sql.append(" AND `type` = ?");
            params.add(type.toUpperCase());
        }
        if (startDate != null) {
            sql.append(" AND `transaction_date` >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND `transaction_date` <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY `transaction_date` DESC, `id` DESC");

        if (params.isEmpty()) {
            return jdbcTemplate.query(sql.toString(), rowMapper);
        }
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    /**
     * Find a single record by its ID.
     */
    public Expense getExpenseById(Long id) {
        ensureTableExists();
        String sql = "SELECT `id`, `type`, `amount`, `category`, `note`, `transaction_date` FROM " + getTableName() + " WHERE `id` = ?";
        List<Expense> results = jdbcTemplate.query(sql, rowMapper, id);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * Add a new record into the database.
     */
    public int addExpense(Expense expense) {
        ensureTableExists();
        String sql = "INSERT INTO " + getTableName() + " (`type`, `amount`, `category`, `note`, `transaction_date`) " +
                     "VALUES (?, ?, ?, ?, ?)";
        java.sql.Date sqlDate = expense.getTransactionDate() != null ? java.sql.Date.valueOf(expense.getTransactionDate()) : null;
        return jdbcTemplate.update(sql,
                expense.getType(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getNote(),
                sqlDate
        );
    }

    /**
     * Update an existing record by its ID.
     */
    public int updateExpense(Long id, Expense expense) {
        ensureTableExists();
        String sql = "UPDATE " + getTableName() + " " +
                     "SET `type` = ?, `amount` = ?, `category` = ?, `note` = ?, `transaction_date` = ? " +
                     "WHERE `id` = ?";
        java.sql.Date sqlDate = expense.getTransactionDate() != null ? java.sql.Date.valueOf(expense.getTransactionDate()) : null;
        return jdbcTemplate.update(sql,
                expense.getType(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getNote(),
                sqlDate,
                id
        );
    }

    /**
     * Delete a record from the database using its ID.
     */
    public int deleteExpense(Long id) {
        ensureTableExists();
        String sql = "DELETE FROM " + getTableName() + " WHERE `id` = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Calculate total income.
     */
    public BigDecimal getTotalIncome() {
        ensureTableExists();
        String sql = "SELECT COALESCE(SUM(`amount`), 0) FROM " + getTableName() + " WHERE `type` = 'INCOME'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Calculate total expenses.
     */
    public BigDecimal getTotalExpenses() {
        ensureTableExists();
        String sql = "SELECT COALESCE(SUM(`amount`), 0) FROM " + getTableName() + " WHERE `type` = 'EXPENSE'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Check if MySQL connection is active.
     */
    public boolean testConnection() {
        try {
            ensureTableExists();
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + getTableName(), Integer.class);
            return count != null;
        } catch (Exception e) {
            return false;
        }
    }
}
