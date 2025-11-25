package com.budgetwise.ad;

public class CategoryReportItem {

    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private double totalAmount;
    private float percentage;

    public CategoryReportItem(String categoryId, String categoryName, String categoryIcon,
                              String categoryColor, double totalAmount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.totalAmount = totalAmount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
