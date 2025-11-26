// UserDAO.java - Data Access Object for User operations
package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.budgetwise.ad.DatabaseContract.UserEntry;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDAO {
    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Create a new user
     */
    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Hash password
        String hashedPassword = hashPassword(user.getPasswordHash());

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_ID, user.getUserId());
        values.put(UserEntry.COLUMN_NAME, user.getName());
        values.put(UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(UserEntry.COLUMN_PASSWORD_HASH, hashedPassword);
        values.put(UserEntry.COLUMN_CURRENCY, user.getCurrency());
        values.put(UserEntry.COLUMN_DARK_MODE, user.isDarkModeEnabled() ? 1 : 0);
        values.put(UserEntry.COLUMN_CREATED_AT, user.getCreatedAt());
        values.put(UserEntry.COLUMN_UPDATED_AT, user.getUpdatedAt());

        long result = db.insert(UserEntry.TABLE_NAME, null, values);
        if (result != -1) {
            insertDefaultCategoriesForUser(db, user.getUserId());
        }
        return result;
    }

    /**
     * Authenticate user
     */
    public User authenticate(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                UserEntry.COLUMN_EMAIL + "=? AND " + UserEntry.COLUMN_PASSWORD_HASH + "=?",
                new String[]{email, hashedPassword},
                null, null, null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                UserEntry.COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Get user by ID
     */
    public User getUserById(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                UserEntry.COLUMN_USER_ID + "=?",
                new String[]{userId},
                null, null, null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_PASSWORD_HASH)));
        user.setProfileImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_PROFILE_IMAGE_URL)));
        user.setCurrency(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_CURRENCY)));
        user.setDarkModeEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_DARK_MODE)) == 1);
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_CREATED_AT)));
        user.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_UPDATED_AT)));
        return user;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to plain text for demo
            return password;
        }
    }

    private void insertDefaultCategoriesForUser(SQLiteDatabase db, String userId) {
        Category[] defaults = Category.getDefaultCategories();
        long now = System.currentTimeMillis();
        for (Category cat : defaults) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.CategoryEntry.COLUMN_CATEGORY_ID, cat.getCategoryId());
            values.put(DatabaseContract.CategoryEntry.COLUMN_USER_ID, userId);
            values.put(DatabaseContract.CategoryEntry.COLUMN_NAME, cat.getName());
            values.put(DatabaseContract.CategoryEntry.COLUMN_ICON, cat.getIcon());
            values.put(DatabaseContract.CategoryEntry.COLUMN_COLOR, cat.getColor());
            values.put(DatabaseContract.CategoryEntry.COLUMN_IS_DEFAULT, 1);
            values.put(DatabaseContract.CategoryEntry.COLUMN_IS_ACTIVE, 1);
            values.put(DatabaseContract.CategoryEntry.COLUMN_CREATED_AT, now);
            values.put(DatabaseContract.CategoryEntry.COLUMN_UPDATED_AT, now);
            db.insert(DatabaseContract.CategoryEntry.TABLE_NAME, null, values);
        }
    }
}