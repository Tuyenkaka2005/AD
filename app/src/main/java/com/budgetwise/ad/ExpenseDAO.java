package com.budgetwise.ad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseDAO {
    private DatabaseHelper dbHelper;

    public ExpenseDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<Expense> searchExpenses(String userId, String keyword, String categoryId,
                                        Integer month, Integer year, Double minAmount) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM " + DatabaseContract.ExpenseEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.ExpenseEntry.COLUMN_USER_ID + "=?";
        List<String> args = new ArrayList<>();
        args.add(userId);

        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (" + DatabaseContract.ExpenseEntry.COLUMN_TITLE + " LIKE ? OR " +
                    DatabaseContract.ExpenseEntry.COLUMN_NOTE + " LIKE ?)";
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        if (categoryId != null) {
            sql += " AND " + DatabaseContract.ExpenseEntry.COLUMN_CATEGORY_ID + "=?";
            args.add(categoryId);
        }
        if (minAmount != null) {
            sql += " AND " + DatabaseContract.ExpenseEntry.COLUMN_AMOUNT + ">=?";
            args.add(String.valueOf(minAmount));
        }
        if (month != null && year != null) {
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, 1, 0, 0, 0);
            long start = c.getTimeInMillis();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.MILLISECOND, -1);
            long end = c.getTimeInMillis();
            sql += " AND " + DatabaseContract.ExpenseEntry.COLUMN_DATE + " BETWEEN ? AND ?";
            args.add(String.valueOf(start));
            args.add(String.valueOf(end));
        }

        sql += " ORDER BY " + DatabaseContract.ExpenseEntry.COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]));
        while (cursor.moveToNext()) {
            Expense e = new Expense();
            e.setExpenseId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_EXPENSE_ID)));
            e.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_USER_ID)));
            e.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_CATEGORY_ID)));
            e.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_TITLE)));
            e.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_AMOUNT)));
            e.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_NOTE)));
            e.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_DATE)));
            list.add(e);
        }
        cursor.close();
        return list;
    }

    // Hàm hỗ trợ chuyển Cursor → Expense (bạn có thể đã có rồi)
    public Expense cursorToExpense(Cursor c) {
        Expense e = new Expense();
        e.setExpenseId(c.getString(c.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_EXPENSE_ID)));
        e.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_TITLE)));
        e.setAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_AMOUNT)));
        e.setCategoryId(c.getString(c.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_CATEGORY_ID)));
        e.setDate(c.getLong(c.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_DATE)));
        e.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseContract.ExpenseEntry.COLUMN_NOTE)));
        return e;
    }
}