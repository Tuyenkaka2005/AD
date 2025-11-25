package com.budgetwise.ad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.budgetwise.ad.DatabaseContract;
import com.budgetwise.ad.DatabaseContract.*;
import com.budgetwise.ad.Category;

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

        // Handle migrations based on version
        if (oldVersion < 2) {
            // Future migration: Add new columns or tables
            // Example: db.execSQL("ALTER TABLE expenses ADD COLUMN new_field TEXT");
        }

        // For development, you can drop and recreate
        // WARNING: This deletes all data! Only use during development.
        /*
        db.execSQL(NotificationEntry.SQL_DROP_TABLE);
        db.execSQL(RecurringExpenseEntry.SQL_DROP_TABLE);
        db.execSQL(BudgetEntry.SQL_DROP_TABLE);
        db.execSQL(ExpenseEntry.SQL_DROP_TABLE);
        db.execSQL(CategoryEntry.SQL_DROP_TABLE);
        db.execSQL(UserEntry.SQL_DROP_TABLE);
        db.execSQL(SyncEntry.SQL_DROP_TABLE);
        onCreate(db);
        */
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

            db.execSQL(sql, new Object[] {
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
     * Clear all user data (for logout)
     */
    public void clearUserData(String userId) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            // Delete in order to respect foreign keys
            db.delete(NotificationEntry.TABLE_NAME,
                    NotificationEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(RecurringExpenseEntry.TABLE_NAME,
                    RecurringExpenseEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(BudgetEntry.TABLE_NAME,
                    BudgetEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(ExpenseEntry.TABLE_NAME,
                    ExpenseEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            // Keep default categories, delete only custom ones
            db.delete(CategoryEntry.TABLE_NAME,
                    CategoryEntry.COLUMN_USER_ID + "=?", new String[]{userId});
            db.delete(UserEntry.TABLE_NAME,
                    UserEntry.COLUMN_USER_ID + "=?", new String[]{userId});

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
            db.delete(CategoryEntry.TABLE_NAME,
                    CategoryEntry.COLUMN_IS_DEFAULT + "=0", null);
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
                UserEntry.TABLE_NAME,
                CategoryEntry.TABLE_NAME,
                ExpenseEntry.TABLE_NAME,
                BudgetEntry.TABLE_NAME,
                RecurringExpenseEntry.TABLE_NAME,
                NotificationEntry.TABLE_NAME
        };

        for (String table : tables) {
            android.database.Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + table, null);
            if (cursor.moveToFirst()) {
                stats.append(table).append(": ").append(cursor.getInt(0)).append("\n");
            }
            cursor.close();
        }

        return stats.toString();
    }
}