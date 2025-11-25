package com.budgetwise.ad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.budgetwise.ad.DatabaseContract.CategoryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category operations
 */
public class CategoryDAO {
    private static final String TAG = "CategoryDAO";
    private DatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(String categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Category category = null;

        Cursor cursor = db.query(
                CategoryEntry.TABLE_NAME,
                null,
                CategoryEntry.COLUMN_CATEGORY_ID + "=?",
                new String[]{categoryId},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            category = cursorToCategory(cursor);
        }
        cursor.close();

        return category;
    }

    /**
     * Get all active categories
     */
    public List<Category> getAllActiveCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Category> categories = new ArrayList<>();

        Cursor cursor = db.query(
                CategoryEntry.TABLE_NAME,
                null,
                CategoryEntry.COLUMN_IS_ACTIVE + "=1",
                null,
                null, null,
                CategoryEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            categories.add(cursorToCategory(cursor));
        }
        cursor.close();

        return categories;
    }

    /**
     * Convert cursor to Category object
     */
    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();
        category.setCategoryId(cursor.getString(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_CATEGORY_ID)));

        int userIdIndex = cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_USER_ID);
        if (!cursor.isNull(userIdIndex)) {
            category.setUserId(cursor.getString(userIdIndex));
        }

        category.setName(cursor.getString(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME)));
        category.setIcon(cursor.getString(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_ICON)));
        category.setColor(cursor.getString(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_COLOR)));
        category.setDefault(cursor.getInt(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_IS_DEFAULT)) == 1);
        category.setActive(cursor.getInt(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_IS_ACTIVE)) == 1);
        category.setCreatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_CREATED_AT)));
        category.setUpdatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_UPDATED_AT)));

        return category;
    }
}