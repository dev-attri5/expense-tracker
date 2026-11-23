package com.expense;

import jakarta.servlet.http.HttpSession;
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

    private Long getLoggedInUserId(HttpSession session) {
        if (session == null) return null;
        return (Long) session.getAttribute("USER_ID");
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Personal Expense Tracker backend is running!");
        response.put("databaseConnected", expenseDAO.testConnection());
        response.put("activeDatabase", expenseDAO.getTargetDatabase());
        return response;
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {

        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Please log in.");
        }

        try {
            // Safely parse dates only if they are not null and not empty
            LocalDate start = null;
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    start = LocalDate.parse(startDate.trim());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid start date format. Please use YYYY-MM-DD.");
                }
            }

            LocalDate end = null;
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    end = LocalDate.parse(endDate.trim());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid end date format. Please use YYYY-MM-DD.");
                }
            }

            List<Expense> list = expenseDAO.getFilteredExpenses(userId, type, start, end);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorMessage(e));
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id, HttpSession session) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Please log in.");
        }

        try {
            Expense expense = expenseDAO.getExpenseById(id, userId);
            if (expense == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
            }
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorMessage(e));
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> addTransaction(@RequestBody Expense expense, HttpSession session) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Please log in.");
        }

        String validationError = validateExpense(expense);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        expense.setUserId(userId);
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
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody Expense expense, HttpSession session) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Please log in.");
        }

        Expense existing = expenseDAO.getExpenseById(id, userId);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }

        String validationError = validateExpense(expense);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        expense.setType(expense.getType().toUpperCase());

        try {
            int rowsAffected = expenseDAO.updateExpense(id, userId, expense);
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
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, HttpSession session) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Please log in.");
        }

        Expense existing = expenseDAO.getExpenseById(id, userId);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found with id: " + id);
        }

        try {
            int rowsAffected = expenseDAO.deleteExpense(id, userId);
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

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(HttpSession session) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Please log in.");
        }

        try {
            BigDecimal totalIncome = expenseDAO.getTotalIncome(userId);
            BigDecimal totalExpenses = expenseDAO.getTotalExpenses(userId);

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

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorMessage(e));
        }
    }

    /**
     * Unwraps nested exception causes to provide descriptive error details.
     */
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
