package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.budgetwise.ad.DatabaseContract.*;
// Lưu ý: Đảm bảo file CategoryReportItem nằm đúng package này hoặc .model
// Nếu code báo đỏ dòng này, hãy trỏ chuột vào và nhấn Alt+Enter để import đúng.
import com.budgetwise.ad.CategoryReportItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQLiteOpenHelper for managing local database.
 * Handles database creation, upgrades, and default data insertion.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private Context context;

    // Singleton pattern to prevent multiple database instances
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database...");

        // Create all tables
        db.execSQL(UserEntry.SQL_CREATE_TABLE);
        db.execSQL(CategoryEntry.SQL_CREATE_TABLE);
        db.execSQL(ExpenseEntry.SQL_CREATE_TABLE);
        db.execSQL(BudgetEntry.SQL_CREATE_TABLE);
        db.execSQL(RecurringExpenseEntry.SQL_CREATE_TABLE);
        db.execSQL(NotificationEntry.SQL_CREATE_TABLE);
        db.execSQL(SyncEntry.SQL_CREATE_TABLE);

        // Create indexes for better performance
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_USER);
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_DATE);
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_CATEGORY);

        // Insert default categories
        insertDefaultCategories(db);

        Log.d(TAG, "Database created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from v" + oldVersion + " to v" + newVersion);
        if (oldVersion < 2) {
            // Future migration code
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Insert default expense categories
     */
    private void insertDefaultCategories(SQLiteDatabase db) {
        Category[] defaults = Category.getDefaultCategories();
        long now = System.currentTimeMillis();

        for (Category cat : defaults) {
            String sql = "INSERT INTO " + CategoryEntry.TABLE_NAME + " (" +
                    CategoryEntry.COLUMN_CATEGORY_ID + ", " +
                    CategoryEntry.COLUMN_NAME + ", " +
                    CategoryEntry.COLUMN_ICON + ", " +
                    CategoryEntry.COLUMN_COLOR + ", " +
                    CategoryEntry.COLUMN_IS_DEFAULT + ", " +
                    CategoryEntry.COLUMN_IS_ACTIVE + ", " +
                    CategoryEntry.COLUMN_CREATED_AT + ", " +
                    CategoryEntry.COLUMN_UPDATED_AT +
                    ") VALUES (?, ?, ?, ?, 1, 1, ?, ?)";

            db.execSQL(sql, new Object[]{
                    cat.getCategoryId(),
                    cat.getName(),
                    cat.getIcon(),
                    cat.getColor(),
                    now,
                    now
            });
        }
        Log.d(TAG, "Inserted " + defaults.length + " default categories");
    }

    /**
     * Helper method to insert a new expense.
     * Changed to PUBLIC so it can be used by the AddExpenseActivity later.
     */
    public void insertExpense(SQLiteDatabase db, String userId, String catId, String title, double amount, long date) {
        ContentValues values = new ContentValues();
        values.put(ExpenseEntry.COLUMN_EXPENSE_ID, UUID.randomUUID().toString());
        values.put(ExpenseEntry.COLUMN_USER_ID, userId);
        values.put(ExpenseEntry.COLUMN_CATEGORY_ID, catId);
        values.put(ExpenseEntry.COLUMN_TITLE, title);
        values.put(ExpenseEntry.COLUMN_AMOUNT, amount);
        values.put(ExpenseEntry.COLUMN_DATE, date);
        values.put(ExpenseEntry.COLUMN_IS_SYNCED, 0);
        long now = System.currentTimeMillis();
        values.put(ExpenseEntry.COLUMN_CREATED_AT, now);
        values.put(ExpenseEntry.COLUMN_UPDATED_AT, now);

        // If db is null (called from outside), get writable database
        if (db == null) {
            db = getWritableDatabase();
            db.insert(ExpenseEntry.TABLE_NAME, null, values);
            // Don't close db here if using singleton
        } else {
            db.insert(ExpenseEntry.TABLE_NAME, null, values);
        }
    }

    /**
     * Clear all user data (for logout)
     */
    public void clearUserData(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(NotificationEntry.TABLE_NAME, NotificationEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(RecurringExpenseEntry.TABLE_NAME, RecurringExpenseEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(BudgetEntry.TABLE_NAME, BudgetEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(ExpenseEntry.TABLE_NAME, ExpenseEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(CategoryEntry.TABLE_NAME, CategoryEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(UserEntry.TABLE_NAME, UserEntry.COLUMN_USER_ID + "=?", new String[]{userId});

            db.setTransactionSuccessful();
            Log.d(TAG, "Cleared all data for user: " + userId);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Clear all data (for development/testing)
     */
    public void clearAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(NotificationEntry.TABLE_NAME, null, null);
            db.delete(RecurringExpenseEntry.TABLE_NAME, null, null);
            db.delete(BudgetEntry.TABLE_NAME, null, null);
            db.delete(ExpenseEntry.TABLE_NAME, null, null);
            db.delete(CategoryEntry.TABLE_NAME, CategoryEntry.COLUMN_IS_DEFAULT + "=0", null);
            db.delete(UserEntry.TABLE_NAME, null, null);
            db.delete(SyncEntry.TABLE_NAME, null, null);

            db.setTransactionSuccessful();
            Log.d(TAG, "Cleared all data");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get database statistics
     */
    public String getDatabaseStats() {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder stats = new StringBuilder();
        String[] tables = {
                UserEntry.TABLE_NAME, CategoryEntry.TABLE_NAME, ExpenseEntry.TABLE_NAME,
                BudgetEntry.TABLE_NAME, RecurringExpenseEntry.TABLE_NAME, NotificationEntry.TABLE_NAME
        };
        for (String table : tables) {
            android.database.Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
            if (cursor.moveToFirst()) {
                stats.append(table).append(": ").append(cursor.getInt(0)).append("\n");
            }
            cursor.close();
        }
        return stats.toString();
    }

    // ============================================================
    // REPORTING SECTION (CHỨC NĂNG BÁO CÁO)
    // ============================================================

    /**
     * Tính tổng chi tiêu của người dùng trong một khoảng thời gian.
     */
    public double getTotalExpenseForPeriod(String userId, long startDate, long endDate) {
        double total = 0;
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT SUM(" + ExpenseEntry.COLUMN_AMOUNT + ") " +
                "FROM " + ExpenseEntry.TABLE_NAME + " " +
                "WHERE " + ExpenseEntry.COLUMN_USER_ID + " = ? " +
                "AND " + ExpenseEntry.COLUMN_DATE + " >= ? " +
                "AND " + ExpenseEntry.COLUMN_DATE + " <= ?";

        String[] selectionArgs = new String[]{
                userId, String.valueOf(startDate), String.valueOf(endDate)
        };

        android.database.Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating total expense", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    /**
     * Lấy danh sách báo cáo phân tích theo từng danh mục (Category).
     * Đã sửa lỗi SQL alias.
     */
    public List<com.budgetwise.ad.CategoryReportItem> getCategoryReport(String userId, long startDate, long endDate) {
        List<com.budgetwise.ad.CategoryReportItem> reportItems = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String eTable = ExpenseEntry.TABLE_NAME;
        String cTable = CategoryEntry.TABLE_NAME;

        // Câu lệnh SQL ĐÃ SỬA: Thêm " AS e" và " AS c"
        String query = "SELECT " +
                "e." + ExpenseEntry.COLUMN_CATEGORY_ID + ", " +
                "c." + CategoryEntry.COLUMN_NAME + ", " +
                "c." + CategoryEntry.COLUMN_ICON + ", " +
                "c." + CategoryEntry.COLUMN_COLOR + ", " +
                "SUM(e." + ExpenseEntry.COLUMN_AMOUNT + ") as total_amount " +
                "FROM " + eTable + " AS e " +  // <--- QUAN TRỌNG: Định nghĩa alias e
                "JOIN " + cTable + " AS c " +  // <--- QUAN TRỌNG: Định nghĩa alias c
                "ON e." + ExpenseEntry.COLUMN_CATEGORY_ID + " = c." + CategoryEntry.COLUMN_CATEGORY_ID + " " +
                "WHERE e." + ExpenseEntry.COLUMN_USER_ID + " = ? " +
                "AND e." + ExpenseEntry.COLUMN_DATE + " >= ? " +
                "AND e." + ExpenseEntry.COLUMN_DATE + " <= ? " +
                "GROUP BY e." + ExpenseEntry.COLUMN_CATEGORY_ID + " " +
                "ORDER BY total_amount DESC";

        String[] selectionArgs = new String[]{
                userId, String.valueOf(startDate), String.valueOf(endDate)
        };

        android.database.Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst()) {
                do {
                    String catId = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_CATEGORY_ID));
                    String catName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME));
                    String catIcon = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_ICON));
                    String catColor = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_COLOR));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

                    com.budgetwise.ad.CategoryReportItem item = new com.budgetwise.ad.CategoryReportItem(catId, catName, catIcon, catColor, amount);
                    reportItems.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error fetching category report", ex);
        } finally {
            if (cursor != null) cursor.close();
        }
        return reportItems;
    }
}