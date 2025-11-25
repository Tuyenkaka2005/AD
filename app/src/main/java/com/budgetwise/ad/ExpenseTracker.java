package com.budgetwise.ad;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

/**
 * Helper class to track expenses and trigger budget notifications
 * Call this whenever an expense is added or updated
 */
public class ExpenseTracker {
    private static final String TAG = "ExpenseTracker";
    private Context context;
    private BudgetNotificationManager notificationManager;
    private CategoryDAO categoryDAO;

    public ExpenseTracker(Context context) {
        this.context = context;
        this.notificationManager = new BudgetNotificationManager(context);
        this.categoryDAO = new CategoryDAO(context);
    }

    /**
     * Call this method after adding or updating an expense
     * to check if any budget limits are reached
     */
    public void onExpenseAdded(String userId, String categoryId, long expenseDate) {
        // Extract month and year from expense date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(expenseDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        // Get category name
        Category category = categoryDAO.getCategoryById(categoryId);
        if (category == null) {
            Log.w(TAG, "Category not found: " + categoryId);
            return;
        }

        // Check budget and send notification if needed
        notificationManager.checkBudgetAndNotify(
                userId, categoryId, category.getName(), month, year);

        Log.d(TAG, "Budget check completed for category: " + category.getName());
    }

    /**
     * Check all budgets for current month
     */
    public void checkCurrentMonthBudgets(String userId) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        notificationManager.checkAllBudgets(userId, month, year);
        Log.d(TAG, "Checked all budgets for current month");
    }

    /**
     * Schedule periodic budget checks (can be called from a background service)
     */
    public static void scheduleDailyBudgetCheck(Context context, String userId) {
        // This would typically use WorkManager or AlarmManager
        // For demonstration, here's the logic that would run daily

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        BudgetNotificationManager manager = new BudgetNotificationManager(context);
        manager.checkAllBudgets(userId, month, year);

        Log.d(TAG, "Daily budget check completed");
    }
}

/**
 * Example usage in your expense creation code:
 *
 * // After creating an expense
 * ExpenseDAO expenseDAO = new ExpenseDAO(context);
 * long result = expenseDAO.createExpense(expense);
 *
 * if (result > 0) {
 *     // Expense created successfully, check budget
 *     ExpenseTracker tracker = new ExpenseTracker(context);
 *     tracker.onExpenseAdded(
 *         expense.getUserId(),
 *         expense.getCategoryId(),
 *         expense.getDate()
 *     );
 * }
 */