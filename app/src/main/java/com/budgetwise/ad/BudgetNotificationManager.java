package com.budgetwise.ad;

import android.content.Context;
import android.widget.Toast;

import java.util.List;

/**
 * Manager to check budgets and notify user
 */
public class BudgetNotificationManager {
    private Context context;
    private BudgetDAO budgetDAO;

    public BudgetNotificationManager(Context context) {
        this.context = context;
        this.budgetDAO = new BudgetDAO(context);
    }

    /**
     * Check all budgets for a user in a given month/year
     */
    public void checkAllBudgets(String userId, int month, int year) {
        List<BudgetDAO.BudgetStatus> statuses = budgetDAO.getUserBudgetStatuses(userId, month, year);

        for (BudgetDAO.BudgetStatus status : statuses) {
            if (status.isOverBudget()) {
                notifyUser("Budget exceeded for category: " +
                        getCategoryName(status));
            } else if (status.isNearLimit()) {
                notifyUser("Budget near limit for category: " +
                        getCategoryName(status));
            }
        }
    }

    private String getCategoryName(BudgetDAO.BudgetStatus status) {
        Category category = new CategoryDAO(context).getCategoryById(status.budget.getCategoryId());
        return category != null ? category.getName() : "Unknown";
    }

    private void notifyUser(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public void checkBudgetAndNotify(String userId, String categoryId, String categoryName, int month, int year) {
        List<BudgetDAO.BudgetStatus> statuses = budgetDAO.getUserBudgetStatuses(userId, month, year);

        for (BudgetDAO.BudgetStatus status : statuses) {
            if (!status.budget.getCategoryId().equals(categoryId)) continue;

            if (status.isOverBudget()) {
                notifyUser("Budget exceeded for category: " + categoryName);
            } else if (status.isNearLimit()) {
                notifyUser("Budget near limit for category: " + categoryName);
            }
        }
    }

}
