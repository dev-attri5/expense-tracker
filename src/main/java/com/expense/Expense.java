package com.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense represents a single transaction (either INCOME or EXPENSE).
 * It holds the 6 fields described in ARCHITECTURE.md and PRD.md.
 */
public class Expense {

    private Long id;
    private String type;             // "INCOME" or "EXPENSE"
    private BigDecimal amount;       // Exact monetary amount
    private String category;         // e.g. "Food", "Travel", "Salary"
    private String note;             // Optional description
    private LocalDate transactionDate; // Date of the transaction

    // Default constructor (required by Spring and JSON parsers)
    public Expense() {
    }

    // Constructor with all fields (useful when reading from MySQL)
    public Expense(Long id, String type, BigDecimal amount, String category, String note, LocalDate transactionDate) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    // Constructor without id (useful when creating a new record before MySQL assigns an id)
    public Expense(String type, BigDecimal amount, String category, String note, LocalDate transactionDate) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
