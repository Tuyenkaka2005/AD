package com.budgetwise.ad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OverviewHelper {

    public static void generateMissedRecurringExpenses(Context context) {
        RecurringHelper.generateMissedRecurringExpenses(context);
    }

    public static MonthlySummary getMonthlySummary(Context context, int month, int year) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        String userId = "user_demo";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        long endOfMonth = cal.getTimeInMillis();

        // Tổng chi tiêu trong tháng
        double totalSpent = 0;
        String sqlExpense = "SELECT SUM(" + DatabaseContract.ExpenseEntry.COLUMN_AMOUNT + ") FROM " +
                DatabaseContract.ExpenseEntry.TABLE_NAME + " WHERE " +
                DatabaseContract.ExpenseEntry.COLUMN_USER_ID + "=? AND " +
                DatabaseContract.ExpenseEntry.COLUMN_DATE + " BETWEEN ? AND ?";
        Cursor c = db.rawQuery(sqlExpense, new String[]{userId, String.valueOf(startOfMonth), String.valueOf(endOfMonth)});
        if (c.moveToFirst() && !c.isNull(0)) {
            totalSpent = c.getDouble(0);
        }
        c.close();

        // Tổng ngân sách trong tháng
        double totalBudget = 0;
        String sqlBudget = "SELECT SUM(" + DatabaseContract.BudgetEntry.COLUMN_AMOUNT_LIMIT + ") FROM " +
                DatabaseContract.BudgetEntry.TABLE_NAME + " WHERE " +
                DatabaseContract.BudgetEntry.COLUMN_USER_ID + "=? AND " +
                DatabaseContract.BudgetEntry.COLUMN_MONTH + "=? AND " +
                DatabaseContract.BudgetEntry.COLUMN_YEAR + "=? AND " +
                DatabaseContract.BudgetEntry.COLUMN_IS_ACTIVE + "=1";
        c = db.rawQuery(sqlBudget, new String[]{userId, String.valueOf(month), String.valueOf(year)});
        if (c.moveToFirst() && !c.isNull(0)) {
            totalBudget = c.getDouble(0);
        }
        c.close();

        // Chi tiết từng category
        List<CategorySummary> categorySummaries = new ArrayList<>();
        String sqlCat = "SELECT c." + DatabaseContract.CategoryEntry.COLUMN_NAME + ", " +
                "c." + DatabaseContract.CategoryEntry.COLUMN_COLOR + ", " +
                "SUM(e." + DatabaseContract.ExpenseEntry.COLUMN_AMOUNT + ") as spent, " +
                "b." + DatabaseContract.BudgetEntry.COLUMN_AMOUNT_LIMIT + " as budget " +
                "FROM " + DatabaseContract.CategoryEntry.TABLE_NAME + " c " +
                "LEFT JOIN " + DatabaseContract.ExpenseEntry.TABLE_NAME + " e ON c." + DatabaseContract.CategoryEntry.COLUMN_CATEGORY_ID + " = e." + DatabaseContract.ExpenseEntry.COLUMN_CATEGORY_ID +
                " AND e." + DatabaseContract.ExpenseEntry.COLUMN_DATE + " BETWEEN ? AND ? AND e." + DatabaseContract.ExpenseEntry.COLUMN_USER_ID + "=? " +
                "LEFT JOIN " + DatabaseContract.BudgetEntry.TABLE_NAME + " b ON c." + DatabaseContract.CategoryEntry.COLUMN_CATEGORY_ID + " = b." + DatabaseContract.BudgetEntry.COLUMN_CATEGORY_ID +
                " AND b." + DatabaseContract.BudgetEntry.COLUMN_MONTH + "=? AND b." + DatabaseContract.BudgetEntry.COLUMN_YEAR + "=? AND b." + DatabaseContract.BudgetEntry.COLUMN_IS_ACTIVE + "=1 " +
                "WHERE (c." + DatabaseContract.CategoryEntry.COLUMN_IS_DEFAULT + "=1 OR c." + DatabaseContract.CategoryEntry.COLUMN_USER_ID + "=?)" +
                " GROUP BY c." + DatabaseContract.CategoryEntry.COLUMN_CATEGORY_ID;

        c = db.rawQuery(sqlCat, new String[]{
                String.valueOf(startOfMonth), String.valueOf(endOfMonth), userId,
                String.valueOf(month), String.valueOf(year), userId
        });

        while (c.moveToNext()) {
            String name = c.getString(0);
            String color = c.getString(1);
            double spent = c.isNull(2) ? 0 : c.getDouble(2);
            double budget = c.isNull(3) ? 0 : c.getDouble(3);
            double percent = budget > 0 ? (spent / budget) * 100 : 0;

            categorySummaries.add(new CategorySummary(name, color, spent, budget, percent));
        }
        c.close();

        return new MonthlySummary(totalSpent, totalBudget, totalBudget - totalSpent, categorySummaries);
    }

    public static void triggerGenerateRecurringExpenses(Context context) {
        generateMissedRecurringExpenses(context);
    }
}