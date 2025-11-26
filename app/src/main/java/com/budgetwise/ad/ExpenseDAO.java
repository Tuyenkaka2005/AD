// ExpenseDAO.java - Data Access Object for Expense operations
package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.budgetwise.ad.DatabaseContract.ExpenseEntry;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    private final DatabaseHelper dbHelper;

    public ExpenseDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Create a new expense
     */
    public long createExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ExpenseEntry.COLUMN_EXPENSE_ID, expense.getExpenseId());
        values.put(ExpenseEntry.COLUMN_USER_ID, expense.getUserId());
        values.put(ExpenseEntry.COLUMN_CATEGORY_ID, expense.getCategoryId());
        values.put(ExpenseEntry.COLUMN_TITLE, expense.getTitle());
        values.put(ExpenseEntry.COLUMN_AMOUNT, expense.getAmount());
        values.put(ExpenseEntry.COLUMN_DATE, expense.getDate());
        values.put(ExpenseEntry.COLUMN_NOTE, expense.getNote());
        values.put(ExpenseEntry.COLUMN_IS_RECURRING, expense.isRecurring() ? 1 : 0);
        values.put(ExpenseEntry.COLUMN_RECURRING_ID, expense.getRecurringId());
        values.put(ExpenseEntry.COLUMN_CREATED_AT, expense.getCreatedAt());
        values.put(ExpenseEntry.COLUMN_UPDATED_AT, expense.getUpdatedAt());

        return db.insert(ExpenseEntry.TABLE_NAME, null, values);
    }

    /**
     * Update an existing expense
     */
    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ExpenseEntry.COLUMN_CATEGORY_ID, expense.getCategoryId());
        values.put(ExpenseEntry.COLUMN_TITLE, expense.getTitle());
        values.put(ExpenseEntry.COLUMN_AMOUNT, expense.getAmount());
        values.put(ExpenseEntry.COLUMN_DATE, expense.getDate());
        values.put(ExpenseEntry.COLUMN_NOTE, expense.getNote());
        values.put(ExpenseEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(ExpenseEntry.TABLE_NAME, values,
                ExpenseEntry.COLUMN_EXPENSE_ID + "=?",
                new String[]{expense.getExpenseId()});
    }

    /**
     * Delete an expense
     */
    public int deleteExpense(String expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(ExpenseEntry.TABLE_NAME,
                ExpenseEntry.COLUMN_EXPENSE_ID + "=?",
                new String[]{expenseId});
    }

    /**
     * Get expense by ID
     */
    public Expense getExpenseById(String expenseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseEntry.TABLE_NAME,
                null,
                ExpenseEntry.COLUMN_EXPENSE_ID + "=?",
                new String[]{expenseId},
                null, null, null
        );

        Expense expense = null;
        if (cursor.moveToFirst()) {
            expense = cursorToExpense(cursor);
        }
        cursor.close();
        return expense;
    }

    /**
     * Get all expenses for a user in a month/year
     */
    public List<Expense> getUserExpenses(String userId, int month, int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Expense> expenses = new ArrayList<>();

        String selection = ExpenseEntry.COLUMN_USER_ID + "=? AND " +
                "strftime('%m', datetime(" + ExpenseEntry.COLUMN_DATE + "/1000, 'unixepoch'))=? AND " +
                "strftime('%Y', datetime(" + ExpenseEntry.COLUMN_DATE + "/1000, 'unixepoch'))=?";
        String[] args = {userId, String.format("%02d", month), String.valueOf(year)};

        Cursor cursor = db.query(ExpenseEntry.TABLE_NAME, null, selection, args,
                null, null, ExpenseEntry.COLUMN_DATE + " DESC");

        while (cursor.moveToNext()) {
            expenses.add(cursorToExpense(cursor));
        }
        cursor.close();
        return expenses;
    }

    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();
        expense.setExpenseId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_EXPENSE_ID)));
        expense.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_USER_ID)));
        expense.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_CATEGORY_ID)));
        expense.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_TITLE)));
        expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_AMOUNT)));
        expense.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_DATE)));
        expense.setNote(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NOTE)));
        expense.setRecurring(cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_IS_RECURRING)) == 1);
        expense.setRecurringId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_RECURRING_ID)));
        expense.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_CREATED_AT)));
        expense.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_UPDATED_AT)));
        return expense;
    }
}