package com.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {

    private Long id;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private String category;
    private String note;
    private LocalDate transactionDate;

    // Default constructor (required by Spring and JSON parsers)
    public Expense() {
    }

    // Constructor without userId (backward-compatible)
    public Expense(Long id, String type, BigDecimal amount, String category, String note, LocalDate transactionDate) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    // All-arguments constructor with userId
    public Expense(Long id, Long userId, String type, BigDecimal amount, String category, String note, LocalDate transactionDate) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
