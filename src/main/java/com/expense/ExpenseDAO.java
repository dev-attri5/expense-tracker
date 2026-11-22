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
     * Automatically ensures the table and required columns exist.
     */
    public synchronized void ensureTableExists() {
        try {
            // 1. Check if a database is selected, fallback to expense_tracker if none
            String currentDb = null;
            try {
                currentDb = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            } catch (Exception ignored) {}

            if (currentDb == null || currentDb.trim().isEmpty()) {
                try {
                    jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `expense_tracker`");
                    jdbcTemplate.execute("USE `expense_tracker`");
                } catch (Exception ignored) {}
            }

            // 2. Create table if not exists
            String createSql = "CREATE TABLE IF NOT EXISTS `transactions` (" +
                               "`id` INT PRIMARY KEY AUTO_INCREMENT, " +
                               "`type` VARCHAR(10) NOT NULL, " +
                               "`amount` DECIMAL(10,2) NOT NULL, " +
                               "`category` VARCHAR(50) NOT NULL, " +
                               "`note` VARCHAR(200), " +
                               "`transaction_date` DATE NOT NULL" +
                               ")";
            jdbcTemplate.execute(createSql);

            // 3. Fallback migrations in case table was created with different column names
            try {
                jdbcTemplate.execute("ALTER TABLE `transactions` ADD COLUMN `transaction_date` DATE");
            } catch (Exception ignored) {}

            try {
                jdbcTemplate.execute("ALTER TABLE `transactions` ADD COLUMN `category` VARCHAR(50)");
            } catch (Exception ignored) {}

            try {
                jdbcTemplate.execute("ALTER TABLE `transactions` ADD COLUMN `note` VARCHAR(200)");
            } catch (Exception ignored) {}

            System.out.println("Database table `transactions` verified successfully in: " + currentDb);
        } catch (Exception e) {
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
        StringBuilder sql = new StringBuilder("SELECT `id`, `type`, `amount`, `category`, `note`, `transaction_date` FROM `transactions` WHERE 1=1");
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
        String sql = "SELECT `id`, `type`, `amount`, `category`, `note`, `transaction_date` FROM `transactions` WHERE `id` = ?";
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
        String sql = "INSERT INTO `transactions` (`type`, `amount`, `category`, `note`, `transaction_date`) " +
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
        String sql = "UPDATE `transactions` " +
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
        String sql = "DELETE FROM `transactions` WHERE `id` = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Calculate total income.
     */
    public BigDecimal getTotalIncome() {
        ensureTableExists();
        String sql = "SELECT COALESCE(SUM(`amount`), 0) FROM `transactions` WHERE `type` = 'INCOME'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Calculate total expenses.
     */
    public BigDecimal getTotalExpenses() {
        ensureTableExists();
        String sql = "SELECT COALESCE(SUM(`amount`), 0) FROM `transactions` WHERE `type` = 'EXPENSE'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Check if MySQL connection is active.
     */
    public boolean testConnection() {
        try {
            ensureTableExists();
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `transactions`", Integer.class);
            return count != null;
        } catch (Exception e) {
            return false;
        }
    }
}
