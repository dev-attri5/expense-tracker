package com.expense;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExpenseDAO {

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
        StringBuilder sql = new StringBuilder("SELECT id, type, amount, category, note, transaction_date FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
            sql.append(" AND type = ?");
            params.add(type.toUpperCase());
        }
        if (startDate != null) {
            sql.append(" AND transaction_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND transaction_date <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY transaction_date DESC, id DESC");

        // If no filter parameters, use simple query; otherwise pass parameter array
        if (params.isEmpty()) {
            return jdbcTemplate.query(sql.toString(), rowMapper);
        }
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    /**
     * Find a single record by its ID.
     */
    public Expense getExpenseById(Long id) {
        String sql = "SELECT id, type, amount, category, note, transaction_date FROM transactions WHERE id = ?";
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
        String sql = "INSERT INTO transactions (type, amount, category, note, transaction_date) " +
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
        String sql = "UPDATE transactions " +
                     "SET type = ?, amount = ?, category = ?, note = ?, transaction_date = ? " +
                     "WHERE id = ?";
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
        String sql = "DELETE FROM transactions WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Calculate total income.
     */
    public BigDecimal getTotalIncome() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Calculate total expenses.
     */
    public BigDecimal getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    /**
     * Check if MySQL connection is active.
     */
    public boolean testConnection() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Integer.class);
            return count != null;
        } catch (Exception e) {
            return false;
        }
    }
}
