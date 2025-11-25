package com.budgetwise.ad;

/**
 * Model class representing an expense category
 */
public class Category {
    private String categoryId;
    private String userId;
    private String name;
    private String icon;
    private String color;
    private boolean isDefault;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Constructor
    public Category() {
    }

    public Category(String categoryId, String name, String icon, String color) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.isDefault = false;
        this.isActive = true;
    }

    // Getters
    public String getCategoryId() {
        return categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public boolean isDefault() {
        return isDefault;
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
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get default categories for the app
     */
    public static Category[] getDefaultCategories() {
        return new Category[] {
                new Category("cat_food", "Food & Dining", "🍔", "#FF6B6B"),
                new Category("cat_transport", "Transportation", "🚗", "#4ECDC4"),
                new Category("cat_shopping", "Shopping", "🛍️", "#45B7D1"),
                new Category("cat_entertainment", "Entertainment", "🎬", "#96CEB4"),
                new Category("cat_bills", "Bills & Utilities", "💡", "#FFEAA7"),
                new Category("cat_education", "Education", "📚", "#DFE6E9"),
                new Category("cat_health", "Healthcare", "⚕️", "#74B9FF"),
                new Category("cat_personal", "Personal Care", "💅", "#FD79A8"),
                new Category("cat_travel", "Travel", "✈️", "#A29BFE"),
                new Category("cat_other", "Other", "📌", "#B2BEC3")
        };
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}