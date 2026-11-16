package com.expense;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExpenseController handles HTTP requests sent from the browser/frontend.
 * It calls ExpenseDAO to perform database operations.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows web browsers to make requests smoothly
public class ExpenseController {

    private final ExpenseDAO expenseDAO;

    public ExpenseController(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    /**
     * 1. GET /api/status
     * Simple health check to verify server and MySQL connection.
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Personal Expense Tracker backend is running!");
        response.put("databaseConnected", expenseDAO.testConnection());
        return response;
    }

    /**
     * 2. GET /api/transactions
     * Fetches all records from the database, with optional filtering by type, startDate, and endDate.
     */
    @GetMapping("/transactions")
    public List<Expense> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if ((type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) || startDate != null || endDate != null) {
            return expenseDAO.getFilteredExpenses(type, startDate, endDate);
        }
        return expenseDAO.getAllExpenses();
    }

    /**
     * 3. GET /api/transactions/{id}
     * Fetches a single record by its ID.
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        Expense expense = expenseDAO.getExpenseById(id);
        if (expense == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }
        return ResponseEntity.ok(expense);
    }

    /**
     * 4. POST /api/transactions
     * Saves a new transaction into the database.
     */
    @PostMapping("/transactions")
    public ResponseEntity<?> addTransaction(@RequestBody Expense expense) {
        // Simple validation checks for beginner safety
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be greater than zero.");
        }
        if (expense.getType() == null || (!expense.getType().equalsIgnoreCase("INCOME") && !expense.getType().equalsIgnoreCase("EXPENSE"))) {
            return ResponseEntity.badRequest().body("Type must be INCOME or EXPENSE.");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Category is required.");
        }
        if (expense.getTransactionDate() == null) {
            return ResponseEntity.badRequest().body("Date is required.");
        }

        // Normalize type to UPPERCASE (INCOME / EXPENSE)
        expense.setType(expense.getType().toUpperCase());

        int rowsAffected = expenseDAO.addExpense(expense);
        if (rowsAffected > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Record saved successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save record.");
        }
    }

    /**
     * 5. PUT /api/transactions/{id}
     * Updates an existing transaction.
     */
    @PutMapping("/transactions/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody Expense expense) {
        // Verify that the record exists first
        Expense existing = expenseDAO.getExpenseById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }

        // Validation
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be greater than zero.");
        }
        if (expense.getType() == null || (!expense.getType().equalsIgnoreCase("INCOME") && !expense.getType().equalsIgnoreCase("EXPENSE"))) {
            return ResponseEntity.badRequest().body("Type must be INCOME or EXPENSE.");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Category is required.");
        }
        if (expense.getTransactionDate() == null) {
            return ResponseEntity.badRequest().body("Date is required.");
        }

        expense.setType(expense.getType().toUpperCase());

        int rowsAffected = expenseDAO.updateExpense(id, expense);
        if (rowsAffected > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Record updated successfully.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update record.");
        }
    }

    /**
     * 6. DELETE /api/transactions/{id}
     * Deletes a record from the database.
     */
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        Expense existing = expenseDAO.getExpenseById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }

        int rowsAffected = expenseDAO.deleteExpense(id);
        if (rowsAffected > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Record deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete record.");
        }
    }

    /**
     * 7. GET /api/summary
     * Calculates and returns totalIncome, totalExpenses, and balance.
     * Balance = Total Income - Total Expenses
     */
    @GetMapping("/summary")
    public Map<String, BigDecimal> getSummary() {
        BigDecimal totalIncome = expenseDAO.getTotalIncome();
        BigDecimal totalExpenses = expenseDAO.getTotalExpenses();

        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        // Calculate balance using exact BigDecimal arithmetic
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", balance);

        return summary;
    }
}
