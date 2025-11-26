// ExpenseActivity.java - Activity for adding/editing expenses
package com.budgetwise.ad;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class ExpenseActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etNote, etDate;
    private Spinner spinnerCategory;
    private Button btnSave;
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private ExpenseTracker expenseTracker;
    private String expenseId;  // Null for new, ID for edit
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);  // Assume you create activity_expense.xml

        expenseDAO = new ExpenseDAO(this);
        categoryDAO = new CategoryDAO(this);
        expenseTracker = new ExpenseTracker(this);
        userId = UserSession.getCurrentUserId(this);

        initViews();
        loadCategories();

        expenseId = getIntent().getStringExtra("EXPENSE_ID");
        if (expenseId != null) {
            loadExpense();
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etExpenseTitle);
        etAmount = findViewById(R.id.etExpenseAmount);
        etNote = findViewById(R.id.etExpenseNote);
        etDate = findViewById(R.id.etExpenseDate);
        spinnerCategory = findViewById(R.id.spinnerExpenseCategory);
        btnSave = findViewById(R.id.btnSaveExpense);

        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void loadCategories() {
        CategoryHelper.loadCategoriesIntoSpinner(this, spinnerCategory);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            etDate.setText(String.format("%02d/%02d/%d", day, month + 1, year));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        try {
            String[] parts = dateStr.split("/");
            cal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }
        long date = cal.getTimeInMillis();

        Category category = (Category) spinnerCategory.getSelectedItem();
        if (category == null) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Expense expense;
        if (expenseId == null) {
            expenseId = "exp_" + System.currentTimeMillis();
            expense = new Expense(expenseId, userId, category.getCategoryId(), title, amount, date, note);
            long result = expenseDAO.createExpense(expense);
            if (result > 0) {
                expenseTracker.onExpenseAdded(userId, category.getCategoryId(), date);
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding expense", Toast.LENGTH_SHORT).show();
            }
        } else {
            expense = expenseDAO.getExpenseById(expenseId);
            if (expense != null) {
                expense.setCategoryId(category.getCategoryId());
                expense.setTitle(title);
                expense.setAmount(amount);
                expense.setDate(date);
                expense.setNote(note);
                int result = expenseDAO.updateExpense(expense);
                if (result > 0) {
                    expenseTracker.onExpenseAdded(userId, category.getCategoryId(), date);  // Re-check budget
                    Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error updating expense", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadExpense() {
        Expense expense = expenseDAO.getExpenseById(expenseId);
        if (expense != null) {
            etTitle.setText(expense.getTitle());
            etAmount.setText(String.valueOf(expense.getAmount()));
            etNote.setText(expense.getNote());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(expense.getDate());
            etDate.setText(String.format("%02d/%02d/%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));

            // Set spinner selection
            for (int i = 0; i < spinnerCategory.getCount(); i++) {
                Category cat = (Category) spinnerCategory.getItemAtPosition(i);
                if (cat.getCategoryId().equals(expense.getCategoryId())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }
}