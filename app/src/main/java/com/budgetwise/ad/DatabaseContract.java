package com.budgetwise.ad;

import android.provider.BaseColumns;

/**
 * Contract class defining SQLite database schema.
 * Contains table names, column names, and SQL statements.
 */
public final class DatabaseContract {

    // Database info
    public static final String DATABASE_NAME = "campus_expense.db";
    public static final int DATABASE_VERSION = 1;

    // Prevent instantiation
    private DatabaseContract() {}

    // ==================== USER TABLE ====================
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_PROFILE_IMAGE_URL = "profile_image_url";
        public static final String COLUMN_CURRENCY = "currency";
        public static final String COLUMN_DARK_MODE = "dark_mode_enabled";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_USER_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
                        COLUMN_PASSWORD_HASH + " TEXT," +
                        COLUMN_PROFILE_IMAGE_URL + " TEXT," +
                        COLUMN_CURRENCY + " TEXT DEFAULT 'VND'," +
                        COLUMN_DARK_MODE + " INTEGER DEFAULT 0," +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL," +
                        COLUMN_UPDATED_AT + " INTEGER NOT NULL)";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ==================== CATEGORY TABLE ====================
    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_IS_DEFAULT = "is_default";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_CATEGORY_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_USER_ID + " TEXT," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_ICON + " TEXT," +
                        COLUMN_COLOR + " TEXT," +
                        COLUMN_IS_DEFAULT + " INTEGER DEFAULT 0," +
                        COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1," +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL," +
                        COLUMN_UPDATED_AT + " INTEGER NOT NULL)";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ==================== EXPENSE TABLE ====================
    public static class ExpenseEntry implements BaseColumns {
        public static final String TABLE_NAME = "expenses";

        public static final String COLUMN_EXPENSE_ID = "expense_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_IS_RECURRING = "is_recurring";
        public static final String COLUMN_RECURRING_ID = "recurring_id";
        public static final String COLUMN_RECEIPT_IMAGE_URL = "receipt_image_url";
        public static final String COLUMN_IS_SYNCED = "is_synced";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_EXPENSE_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_USER_ID + " TEXT NOT NULL," +
                        COLUMN_CATEGORY_ID + " TEXT NOT NULL," +
                        COLUMN_TITLE + " TEXT NOT NULL," +
                        COLUMN_AMOUNT + " REAL NOT NULL," +
                        COLUMN_DATE + " INTEGER NOT NULL," +
                        COLUMN_NOTE + " TEXT," +
                        COLUMN_IS_RECURRING + " INTEGER DEFAULT 0," +
                        COLUMN_RECURRING_ID + " TEXT," +
                        COLUMN_RECEIPT_IMAGE_URL + " TEXT," +
                        COLUMN_IS_SYNCED + " INTEGER DEFAULT 0," +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL," +
                        COLUMN_UPDATED_AT + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_USER_ID + ")," +
                        "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " +
                        CategoryEntry.TABLE_NAME + "(" + CategoryEntry.COLUMN_CATEGORY_ID + "))";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        // Index for faster queries
        public static final String SQL_CREATE_INDEX_USER =
                "CREATE INDEX idx_expense_user ON " + TABLE_NAME + "(" + COLUMN_USER_ID + ")";

        public static final String SQL_CREATE_INDEX_DATE =
                "CREATE INDEX idx_expense_date ON " + TABLE_NAME + "(" + COLUMN_DATE + ")";

        public static final String SQL_CREATE_INDEX_CATEGORY =
                "CREATE INDEX idx_expense_category ON " + TABLE_NAME + "(" + COLUMN_CATEGORY_ID + ")";
    }

    // ==================== BUDGET TABLE ====================
    public static class BudgetEntry implements BaseColumns {
        public static final String TABLE_NAME = "budgets";

        public static final String COLUMN_BUDGET_ID = "budget_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_AMOUNT_LIMIT = "amount_limit";
        public static final String COLUMN_MONTH = "month";
        public static final String COLUMN_YEAR = "year";
        public static final String COLUMN_WARNING_THRESHOLD = "warning_threshold";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_BUDGET_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_USER_ID + " TEXT NOT NULL," +
                        COLUMN_CATEGORY_ID + " TEXT," +
                        COLUMN_AMOUNT_LIMIT + " REAL NOT NULL," +
                        COLUMN_MONTH + " INTEGER NOT NULL," +
                        COLUMN_YEAR + " INTEGER NOT NULL," +
                        COLUMN_WARNING_THRESHOLD + " REAL DEFAULT 0.8," +
                        COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1," +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL," +
                        COLUMN_UPDATED_AT + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_USER_ID + ")," +
                        "UNIQUE(" + COLUMN_USER_ID + "," + COLUMN_CATEGORY_ID + "," +
                        COLUMN_MONTH + "," + COLUMN_YEAR + "))";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ==================== RECURRING EXPENSE TABLE ====================
    public static class RecurringExpenseEntry implements BaseColumns {
        public static final String TABLE_NAME = "recurring_expenses";

        public static final String COLUMN_RECURRING_ID = "recurring_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_INTERVAL = "interval";
        public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
        public static final String COLUMN_DAY_OF_MONTH = "day_of_month";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_NEXT_RUN_DATE = "next_run_date";
        public static final String COLUMN_LAST_RUN_DATE = "last_run_date";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_RECURRING_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_USER_ID + " TEXT NOT NULL," +
                        COLUMN_CATEGORY_ID + " TEXT NOT NULL," +
                        COLUMN_TITLE + " TEXT NOT NULL," +
                        COLUMN_AMOUNT + " REAL NOT NULL," +
                        COLUMN_NOTE + " TEXT," +
                        COLUMN_INTERVAL + " TEXT NOT NULL," +
                        COLUMN_DAY_OF_WEEK + " INTEGER," +
                        COLUMN_DAY_OF_MONTH + " INTEGER," +
                        COLUMN_START_DATE + " INTEGER NOT NULL," +
                        COLUMN_END_DATE + " INTEGER," +
                        COLUMN_NEXT_RUN_DATE + " INTEGER NOT NULL," +
                        COLUMN_LAST_RUN_DATE + " INTEGER," +
                        COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1," +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL," +
                        COLUMN_UPDATED_AT + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_USER_ID + "))";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ==================== NOTIFICATION TABLE ====================
    public static class NotificationEntry implements BaseColumns {
        public static final String TABLE_NAME = "notifications";

        public static final String COLUMN_NOTIFICATION_ID = "notification_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_RELATED_ID = "related_id";
        public static final String COLUMN_IS_READ = "is_read";
        public static final String COLUMN_DATE_SENT = "date_sent";
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NOTIFICATION_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_USER_ID + " TEXT NOT NULL," +
                        COLUMN_TYPE + " TEXT NOT NULL," +
                        COLUMN_TITLE + " TEXT NOT NULL," +
                        COLUMN_MESSAGE + " TEXT," +
                        COLUMN_RELATED_ID + " TEXT," +
                        COLUMN_IS_READ + " INTEGER DEFAULT 0," +
                        COLUMN_DATE_SENT + " INTEGER NOT NULL," +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_USER_ID + "))";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ==================== SYNC METADATA TABLE ====================
    public static class SyncEntry implements BaseColumns {
        public static final String TABLE_NAME = "sync_metadata";

        public static final String COLUMN_TABLE_NAME = "table_name";
        public static final String COLUMN_LAST_SYNC = "last_sync";
        public static final String COLUMN_STATUS = "status";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_TABLE_NAME + " TEXT UNIQUE NOT NULL," +
                        COLUMN_LAST_SYNC + " INTEGER," +
                        COLUMN_STATUS + " TEXT)";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}