// CategoryHelper.java
package com.budgetwise.ad;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.List;

public class CategoryHelper {
    public static void loadCategoriesIntoSpinner(Context context, Spinner spinner) {
        CategoryDAO categoryDAO = new CategoryDAO(context);
        List<Category> categories = categoryDAO.getAllActiveCategories();

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}