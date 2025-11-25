package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.budgetwise.ad.DatabaseContract.BudgetEntry;
import com.budgetwise.ad.DatabaseContract.ExpenseEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data Access Object for Budget operations
 */
public class BudgetDAO {
    private final DatabaseHelper dbHelper;

    public BudgetDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // ================= CREATE =================
    public long createBudget(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Nếu budgetId null, generate một cái mới
            if (budget.getBudgetId() == null || budget.getBudgetId().isEmpty()) {
                budget.setBudgetId("budget_" + System.currentTimeMillis());
            }

            ContentValues values = new ContentValues();
            values.put(BudgetEntry.COLUMN_BUDGET_ID, budget.getBudgetId());
            values.put(BudgetEntry.COLUMN_USER_ID, budget.getUserId());
            values.put(BudgetEntry.COLUMN_CATEGORY_ID, budget.getCategoryId());
            values.put(BudgetEntry.COLUMN_AMOUNT_LIMIT, budget.getAmountLimit());
            values.put(BudgetEntry.COLUMN_MONTH, budget.getMonth());
            values.put(BudgetEntry.COLUMN_YEAR, budget.getYear());
            values.put(BudgetEntry.COLUMN_WARNING_THRESHOLD, budget.getWarningThreshold());
            values.put(BudgetEntry.COLUMN_IS_ACTIVE, budget.isActive() ? 1 : 0);

            long currentTime = System.currentTimeMillis();
            values.put(BudgetEntry.COLUMN_CREATED_AT, currentTime);
            values.put(BudgetEntry.COLUMN_UPDATED_AT, currentTime);

            // Thử insert trước
            long result = db.insert(BudgetEntry.TABLE_NAME, null, values);

            android.util.Log.d("BudgetDAO", "Insert result: " + result +
                    " for category: " + budget.getCategoryId() +
                    ", month: " + budget.getMonth() +
                    ", year: " + budget.getYear());

            // Nếu insert fail (có thể do UNIQUE constraint)
            if (result == -1) {
                android.util.Log.w("BudgetDAO", "Insert failed, might be duplicate. Trying update...");

                // Tìm budget hiện có
                Budget existing = getCategoryBudget(budget.getUserId(),
                        budget.getCategoryId(),
                        budget.getMonth(),
                        budget.getYear());

                if (existing != null) {
                    android.util.Log.d("BudgetDAO", "Found existing budget: " + existing.getBudgetId());
                    // Update budget hiện có
                    budget.setBudgetId(existing.getBudgetId()); // Giữ nguyên ID cũ
                    int updated = updateBudget(budget);
                    android.util.Log.d("BudgetDAO", "Updated existing budget, rows affected: " + updated);
                    return updated > 0 ? 1 : -1; // Return 1 nếu update thành công
                }
            }

            return result;

        } catch (Exception e) {
            android.util.Log.e("BudgetDAO", "Exception creating budget: " + e.getMessage(), e);
            return -1;
        }
    }

    // ================= READ =================
    public Budget getCategoryBudget(String userId, String categoryId, int month, int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = BudgetEntry.COLUMN_USER_ID + "=? AND " +
                BudgetEntry.COLUMN_CATEGORY_ID + "=? AND " +
                BudgetEntry.COLUMN_MONTH + "=? AND " +
                BudgetEntry.COLUMN_YEAR + "=?";
        String[] args = {userId, categoryId, String.valueOf(month), String.valueOf(year)};
        Cursor cursor = db.query(BudgetEntry.TABLE_NAME, null, selection, args,
                null, null, null);

        Budget budget = null;
        if (cursor.moveToFirst()) {
            budget = cursorToBudget(cursor);
        }
        cursor.close();
        return budget;
    }

    public List<Budget> getUserBudgets(String userId, int month, int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = BudgetEntry.COLUMN_USER_ID + "=? AND " +
                BudgetEntry.COLUMN_MONTH + "=? AND " +
                BudgetEntry.COLUMN_YEAR + "=?";
        String[] args = {userId, String.valueOf(month), String.valueOf(year)};
        Cursor cursor = db.query(BudgetEntry.TABLE_NAME, null, selection, args,
                null, null, null);

        List<Budget> budgets = new ArrayList<>();
        while (cursor.moveToNext()) {
            budgets.add(cursorToBudget(cursor));
        }
        cursor.close();
        return budgets;
    }

    private Budget cursorToBudget(Cursor cursor) {
        Budget budget = new Budget();
        budget.setBudgetId(cursor.getString(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_BUDGET_ID)));
        budget.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_USER_ID)));
        budget.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_CATEGORY_ID)));
        budget.setAmountLimit(cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_AMOUNT_LIMIT)));
        budget.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_MONTH)));
        budget.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_YEAR)));
        budget.setWarningThreshold(cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_WARNING_THRESHOLD)));
        budget.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_IS_ACTIVE)) == 1);
        budget.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_CREATED_AT)));
        budget.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(BudgetEntry.COLUMN_UPDATED_AT)));
        return budget;
    }

    // ================= UPDATE =================
    public int updateBudget(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BudgetEntry.COLUMN_AMOUNT_LIMIT, budget.getAmountLimit());
        values.put(BudgetEntry.COLUMN_WARNING_THRESHOLD, budget.getWarningThreshold());
        values.put(BudgetEntry.COLUMN_IS_ACTIVE, budget.isActive() ? 1 : 0);
        values.put(BudgetEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String where = BudgetEntry.COLUMN_BUDGET_ID + "=?";
        String[] args = {budget.getBudgetId()};
        return db.update(BudgetEntry.TABLE_NAME, values, where, args);
    }

    // ================= DELETE =================
    public int deleteBudget(String budgetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = BudgetEntry.COLUMN_BUDGET_ID + "=?";
        String[] args = {budgetId};
        return db.delete(BudgetEntry.TABLE_NAME, where, args);
    }

    // ================= BUDGET STATUS =================
    public static class BudgetStatus {
        public Budget budget;
        public double spent;
        public double remaining;
        public double percentage;

        public BudgetStatus(Budget budget, double spent) {
            this.budget = budget;
            this.spent = spent;
            this.remaining = budget.getAmountLimit() - spent;
            this.percentage = (spent / budget.getAmountLimit()) * 100.0;
        }

        public boolean isOverBudget() {
            return spent > budget.getAmountLimit();
        }

        public boolean isNearLimit() {
            return spent >= budget.getAmountLimit() * budget.getWarningThreshold()
                    && !isOverBudget();
        }
    }

    /**
     * Get BudgetStatus for a specific category
     */
    public BudgetStatus getBudgetStatus(String userId, String categoryId, int month, int year) {
        Budget budget = getCategoryBudget(userId, categoryId, month, year);
        if (budget == null) return null;

        double spent = getSpentAmount(userId, categoryId, month, year);
        return new BudgetStatus(budget, spent);
    }

    /**
     * Get all BudgetStatus objects for a user in a given month/year
     */
    public List<BudgetStatus> getUserBudgetStatuses(String userId, int month, int year) {
        List<Budget> budgets = getUserBudgets(userId, month, year);
        List<BudgetStatus> statuses = new ArrayList<>();
        for (Budget budget : budgets) {
            double spent = getSpentAmount(userId, budget.getCategoryId(), month, year);
            statuses.add(new BudgetStatus(budget, spent));
        }
        return statuses;
    }

    /**
     * Calculate spent amount for a category in a month/year
     */
    private double getSpentAmount(String userId, String categoryId, int month, int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = ExpenseEntry.COLUMN_USER_ID + "=? AND " +
                ExpenseEntry.COLUMN_CATEGORY_ID + "=? AND " +
                "strftime('%m', datetime(" + ExpenseEntry.COLUMN_DATE + "/1000, 'unixepoch'))=? AND " +
                "strftime('%Y', datetime(" + ExpenseEntry.COLUMN_DATE + "/1000, 'unixepoch'))=?";
        String[] args = {
                userId,
                categoryId,
                String.format(Locale.US, "%02d", month),  // ← SỬA DÒNG NÀY
                String.valueOf(year)
        };

        android.util.Log.d("BudgetDAO", "Query getSpentAmount - userId: " + userId +
                ", categoryId: " + categoryId +
                ", month: " + String.format(Locale.US, "%02d", month) +
                ", year: " + year);

        Cursor cursor = db.query(ExpenseEntry.TABLE_NAME,
                new String[]{"SUM(" + ExpenseEntry.COLUMN_AMOUNT + ") AS total"},
                selection, args, null, null, null);

        double total = 0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow("total");
            if (!cursor.isNull(columnIndex)) {  // ← THÊM CHECK NULL
                total = cursor.getDouble(columnIndex);
            }
        }
        cursor.close();

        android.util.Log.d("BudgetDAO", "Total spent: " + total);
        return total;
    }
}
