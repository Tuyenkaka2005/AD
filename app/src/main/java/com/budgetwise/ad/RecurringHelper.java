package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecurringHelper {

    public static boolean insertRecurringExpense(Context context, RecurringExpense re) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID, re.recurringId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID, re.userId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_CATEGORY_ID, re.categoryId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_TITLE, re.title);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_AMOUNT, re.amount);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NOTE, re.note);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_INTERVAL, re.interval);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_START_DATE, re.startDate);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE,  re.nextRunDate);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_IS_ACTIVE, 1);
        long now = System.currentTimeMillis();
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_CREATED_AT, now);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_UPDATED_AT, now);

        return db.insert(DatabaseContract.RecurringExpenseEntry.TABLE_NAME, null, cv) != -1;
    }

    public static List<RecurringExpense> getAllRecurringExpenses(Context context) {
        List<RecurringExpense> list = new ArrayList<>();
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
//        String userId = UserSession.getCurrentUserId(context);
        String userId = "user_demo";

        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseContract.RecurringExpenseEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID + "=? AND " +
                DatabaseContract.RecurringExpenseEntry.COLUMN_IS_ACTIVE + "=1", new String[]{userId});

        while (c.moveToNext()) {
            list.add(new RecurringExpense(
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID)),
                    userId,
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_CATEGORY_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_TITLE)),
                    c.getDouble(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_AMOUNT)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_INTERVAL)),
                    c.getLong(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_START_DATE)),
                    c.getLong(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE))
            ));
        }
        c.close();
        return list;
    }

    public static long calculateNextRunDate(String interval, long current) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(current);
        if (interval.contains("Monthly")) {
            cal.add(Calendar.MONTH, 1);
        } else if (interval.contains("Weekly")) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (interval.contains("2 weeks")) {
            cal.add(Calendar.WEEK_OF_YEAR, 2);
        } else if (interval.contains("Yearly")) {
            cal.add(Calendar.YEAR, 1);
        }
        return cal.getTimeInMillis();
    }

    public static void generateMissedRecurringExpenses(Context context) {
        // Gọi hàm này mỗi khi mở Overview hoặc khi mở app
        List<RecurringExpense> all = getAllRecurringExpenses(context);
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        long now = System.currentTimeMillis();

        db.beginTransaction();
        try {
            for (RecurringExpense re : all) {
                while (re.nextRunDate <= now) {
                    // Tạo expense thực tế
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_EXPENSE_ID, java.util.UUID.randomUUID().toString());
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_USER_ID, re.userId);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_CATEGORY_ID, re.categoryId);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_TITLE, re.title);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_AMOUNT, re.amount);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_DATE, re.nextRunDate);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_NOTE, re.note + " (định kỳ)");
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_IS_RECURRING, 1);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_RECURRING_ID, re.recurringId);
                    long time = System.currentTimeMillis();
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_CREATED_AT, time);
                    cv.put(DatabaseContract.ExpenseEntry.COLUMN_UPDATED_AT, time);

                    db.insert(DatabaseContract.ExpenseEntry.TABLE_NAME, null, cv);

                    // Cập nhật next run
                    long next = calculateNextRunDate(re.interval, re.nextRunDate);
                    ContentValues updateCv = new ContentValues();
                    updateCv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE, next);
                    updateCv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_LAST_RUN_DATE, re.nextRunDate);
                    db.update(DatabaseContract.RecurringExpenseEntry.TABLE_NAME, updateCv,
                            DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID + "=?",
                            new String[]{re.recurringId});

                    re.nextRunDate = next;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}