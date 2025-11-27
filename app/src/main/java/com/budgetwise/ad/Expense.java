package com.budgetwise.ad;

public class Expense {
    private String expenseId;
    private String userId;
    private String categoryId;
    private String title;
    private double amount;
    private String note;
    private long date;
    private long createdAt;

    public Expense() {}

    public Expense(String expenseId, String userId, String categoryId, String title,
                   double amount, String note, long date) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getExpenseId() { return expenseId; }
    public String getUserId() { return userId; }
    public String getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getNote() { return note != null ? note : ""; }
    public long getDate() { return date; }

    // Setters
    public void setExpenseId(String id) { this.expenseId = id; }
    public void setUserId(String id) { this.userId = id; }
    public void setCategoryId(String id) { this.categoryId = id; }
    public void setTitle(String t) { this.title = t; }
    public void setAmount(double a) { this.amount = a; }
    public void setNote(String n) { this.note = n; }
    public void setDate(long d) { this.date = d; }
}