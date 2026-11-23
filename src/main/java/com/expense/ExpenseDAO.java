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

        try {
            long uid = rs.getLong("user_id");
            if (!rs.wasNull()) {
                expense.setUserId(uid);
            }
        } catch (Exception ignored) {}

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
            String usersTable = (targetDatabase != null && !isSystemDatabase(targetDatabase))
                    ? "`" + targetDatabase + "`.`users`"
                    : "`users`";

            // 3. Create users table if not exists
            String createUsersSql = "CREATE TABLE IF NOT EXISTS " + usersTable + " (" +
                                    "`id` INT PRIMARY KEY AUTO_INCREMENT, " +
                                    "`username` VARCHAR(50) NOT NULL UNIQUE, " +
                                    "`password` VARCHAR(255) NOT NULL, " +
                                    "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                    ")";
            jdbcTemplate.execute(createUsersSql);

            // 4. Create transactions table if not exists
            String createSql = "CREATE TABLE IF NOT EXISTS " + tableIdentifier + " (" +
                               "`id` INT PRIMARY KEY AUTO_INCREMENT, " +
                               "`user_id` INT, " +
                               "`type` VARCHAR(10) NOT NULL, " +
                               "`amount` DECIMAL(10,2) NOT NULL, " +
                               "`category` VARCHAR(50) NOT NULL, " +
                               "`note` VARCHAR(200), " +
                               "`transaction_date` DATE NOT NULL" +
                               ")";
            jdbcTemplate.execute(createSql);

            // 5. Ensure required columns exist
            try {
                jdbcTemplate.execute("ALTER TABLE " + tableIdentifier + " ADD COLUMN `user_id` INT");
            } catch (Exception ignored) {}

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
            System.out.println("Database tables verified successfully.");
        } catch (Exception e) {
            initialized = false;
            System.err.println("Database table check notice: " + e.getMessage());
        }
    }

    /**
     * Claims existing transactions created before multi-user login was enabled.
     */
    public void assignOrphanRecords(Long userId) {
        if (userId == null) return;
        try {
            ensureTableExists();
            String sql = "UPDATE " + getTableName() + " SET `user_id` = ? WHERE `user_id` IS NULL";
            jdbcTemplate.update(sql, userId);
        } catch (Exception ignored) {}
    }

    /**
     * Get all records for a specific user.
     */
    public List<Expense> getAllExpenses(Long userId) {
        return getFilteredExpenses(userId, null, null, null);
    }

    /**
     * Filter records by user, type, start date, and end date.
     */
    public List<Expense> getFilteredExpenses(Long userId, String type, LocalDate startDate, LocalDate endDate) {
        ensureTableExists();
        StringBuilder sql = new StringBuilder("SELECT `id`, `user_id`, `type`, `amount`, `category`, `note`, `transaction_date` FROM ")
                .append(getTableName())
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND (`user_id` = ? OR `user_id` IS NULL)");
            params.add(userId);
        }
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
     * Find a single record by its ID, ensuring it belongs to the given user.
     */
    public Expense getExpenseById(Long id, Long userId) {
        ensureTableExists();
        String sql = "SELECT `id`, `user_id`, `type`, `amount`, `category`, `note`, `transaction_date` FROM " + getTableName() + " WHERE `id` = ?";
        List<Object> params = new ArrayList<>();
        params.add(id);

        if (userId != null) {
            sql += " AND (`user_id` = ? OR `user_id` IS NULL)";
            params.add(userId);
        }

        List<Expense> results = jdbcTemplate.query(sql, rowMapper, params.toArray());
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * Add a new record linked to a user.
     */
    public int addExpense(Expense expense) {
        ensureTableExists();
        String sql = "INSERT INTO " + getTableName() + " (`user_id`, `type`, `amount`, `category`, `note`, `transaction_date`) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        java.sql.Date sqlDate = expense.getTransactionDate() != null ? java.sql.Date.valueOf(expense.getTransactionDate()) : null;
        return jdbcTemplate.update(sql,
                expense.getUserId(),
                expense.getType(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getNote(),
                sqlDate
        );
    }

    /**
     * Update an existing record belonging to the user.
     */
    public int updateExpense(Long id, Long userId, Expense expense) {
        ensureTableExists();
        String sql = "UPDATE " + getTableName() + " " +
                     "SET `type` = ?, `amount` = ?, `category` = ?, `note` = ?, `transaction_date` = ? " +
                     "WHERE `id` = ?";
        List<Object> params = new ArrayList<>();
        java.sql.Date sqlDate = expense.getTransactionDate() != null ? java.sql.Date.valueOf(expense.getTransactionDate()) : null;
        params.add(expense.getType());
        params.add(expense.getAmount());
        params.add(expense.getCategory());
        params.add(expense.getNote());
        params.add(sqlDate);
        params.add(id);

        if (userId != null) {
            sql += " AND (`user_id` = ? OR `user_id` IS NULL)";
            params.add(userId);
        }

        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * Delete a record belonging to the user.
     */
    public int deleteExpense(Long id, Long userId) {
        ensureTableExists();
        String sql = "DELETE FROM " + getTableName() + " WHERE `id` = ?";
        List<Object> params = new ArrayList<>();
        params.add(id);

        if (userId != null) {
            sql += " AND (`user_id` = ? OR `user_id` IS NULL)";
            params.add(userId);
        }

        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * Calculate total income for a user.
     */
    public BigDecimal getTotalIncome(Long userId) {
        ensureTableExists();
        String sql = "SELECT COALESCE(SUM(`amount`), 0) FROM " + getTableName() + " WHERE `type` = 'INCOME'";
        if (userId != null) {
            sql += " AND (`user_id` = ? OR `user_id` IS NULL)";
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
        }
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Calculate total expenses for a user.
     */
    public BigDecimal getTotalExpenses(Long userId) {
        ensureTableExists();
        String sql = "SELECT COALESCE(SUM(`amount`), 0) FROM " + getTableName() + " WHERE `type` = 'EXPENSE'";
        if (userId != null) {
            sql += " AND (`user_id` = ? OR `user_id` IS NULL)";
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
        }
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
