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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseDAO expenseDAO;

    public ExpenseController(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Personal Expense Tracker backend is running!");
        response.put("databaseConnected", expenseDAO.testConnection());
        return response;
    }

    @GetMapping("/transactions")
    public List<Expense> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // Safely parse dates only if they are not null and not empty
        LocalDate start = (startDate != null && !startDate.trim().isEmpty()) ? LocalDate.parse(startDate.trim()) : null;
        LocalDate end = (endDate != null && !endDate.trim().isEmpty()) ? LocalDate.parse(endDate.trim()) : null;

        return expenseDAO.getFilteredExpenses(type, start, end);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        Expense expense = expenseDAO.getExpenseById(id);
        if (expense == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }
        return ResponseEntity.ok(expense);
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> addTransaction(@RequestBody Expense expense) {
        String validationError = validateExpense(expense);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        expense.setType(expense.getType().toUpperCase());

        try {
            int rowsAffected = expenseDAO.addExpense(expense);
            if (rowsAffected > 0) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Record saved successfully.");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save record.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorMessage(e));
        }
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody Expense expense) {
        Expense existing = expenseDAO.getExpenseById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }

        String validationError = validateExpense(expense);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        expense.setType(expense.getType().toUpperCase());

        try {
            int rowsAffected = expenseDAO.updateExpense(id, expense);
            if (rowsAffected > 0) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Record updated successfully.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update record.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorMessage(e));
        }
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        Expense existing = expenseDAO.getExpenseById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }

        try {
            int rowsAffected = expenseDAO.deleteExpense(id);
            if (rowsAffected > 0) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Record deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete record.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorMessage(e));
        }
    }

    private String buildErrorMessage(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : e.toString();
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
                msg += " | Cause: " + cause.getMessage();
            }
            cause = cause.getCause();
        }
        return "Database error: " + msg;
    }

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

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", balance);

        return summary;
    }

    /**
     * Shared helper method to validate an incoming Expense object.
     * Returns an error message if invalid, or null if valid.
     */
    private String validateExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Amount must be greater than zero.";
        }
        if (expense.getType() == null || (!expense.getType().equalsIgnoreCase("INCOME") && !expense.getType().equalsIgnoreCase("EXPENSE"))) {
            return "Type must be INCOME or EXPENSE.";
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            return "Category is required.";
        }
        if (expense.getTransactionDate() == null) {
            return "Date is required.";
        }
        return null;
    }
}
