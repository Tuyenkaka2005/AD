package com.budgetwise.ad;

/**
 * Model class representing a budget
 */
public class Budget {
    private String budgetId;
    private String userId;
    private String categoryId;
    private double amountLimit;
    private int month;
    private int year;
    private double warningThreshold; // 0.0 to 1.0 (e.g., 0.8 = 80%)
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Constructor
    public Budget() {
        this.warningThreshold = 0.8; // Default 80%
        this.isActive = true;
    }

    public Budget(String budgetId, String userId, String categoryId,
                  double amountLimit, int month, int year) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amountLimit = amountLimit;
        this.month = month;
        this.year = year;
        this.warningThreshold = 0.8;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters
    public String getBudgetId() {
        return budgetId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public double getAmountLimit() {
        return amountLimit;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getWarningThreshold() {
        return warningThreshold;
    }

    public boolean isActive() {
        return isActive;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setAmountLimit(double amountLimit) {
        this.amountLimit = amountLimit;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "budgetId='" + budgetId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", amountLimit=" + amountLimit +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}