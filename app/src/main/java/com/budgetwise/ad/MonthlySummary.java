// MonthlySummary.java - THAY NGUYÊN FILE NÀY
package com.budgetwise.ad;

import java.util.List;

public class MonthlySummary {
    public double totalSpent;
    public double totalBudget;
    public double remaining;
    public List<CategorySummary> categorySummaries;

    public MonthlySummary(double totalSpent, double totalBudget, double remaining, List<CategorySummary> categorySummaries) {
        this.totalSpent = totalSpent;
        this.totalBudget = totalBudget;
        this.remaining = remaining;
        this.categorySummaries = categorySummaries;
    }
}

class CategorySummary {
    public String categoryName;
    public String color;
    public double spent;
    public double budget;
    public double percent;

    public CategorySummary(String categoryName, String color, double spent, double budget, double percent) {
        this.categoryName = categoryName;
        this.color = color;
        this.spent = spent;
        this.budget = budget;
        this.percent = percent;
    }
}