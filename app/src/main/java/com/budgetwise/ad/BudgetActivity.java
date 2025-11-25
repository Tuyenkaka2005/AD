package com.budgetwise.ad;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetwise.ad.BudgetDAO.BudgetStatus;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity for managing budgets
 */
public class BudgetActivity extends AppCompatActivity {
    private static final String TAG = "BudgetActivity";

    // UI Components
    private ListView budgetListView;
    private Button btnAddBudget;
    private TextView tvMonthYear;
    private Button btnPrevMonth, btnNextMonth;

    // Data
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private BudgetNotificationManager notificationManager;
    private String currentUserId = "user_demo"; // Replace with actual user ID from session
    private int currentMonth;
    private int currentYear;
    private List<BudgetStatus> budgetStatuses;
    private BudgetAdapter budgetAdapter;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Initialize
        initializeViews();
        initializeData();
        loadBudgets();

    }

    private void initializeViews() {
        budgetListView = findViewById(R.id.budgetListView);
        btnAddBudget = findViewById(R.id.btnAddBudget);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));
    }

    private void initializeData() {
        budgetDAO = new BudgetDAO(this);
        categoryDAO = new CategoryDAO(this);
        notificationManager = new BudgetNotificationManager(this);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Set current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentYear = calendar.get(Calendar.YEAR);

        updateMonthYearDisplay();

        budgetStatuses = new ArrayList<>();
    }

    private void loadBudgets() {
        Log.d(TAG, "========== LOAD BUDGETS ==========");
        Log.d(TAG, "User: " + currentUserId + ", Month: " + currentMonth + ", Year: " + currentYear);

        budgetStatuses.clear();

        List<Budget> budgets = budgetDAO.getUserBudgets(currentUserId, currentMonth, currentYear);
        Log.d(TAG, "Found " + budgets.size() + " budget(s)");

        for (int i = 0; i < budgets.size(); i++) {
            Budget budget = budgets.get(i);
            Log.d(TAG, "Budget #" + (i+1) + ": category=" + budget.getCategoryId() +
                    ", amount=" + budget.getAmountLimit());

            BudgetStatus status = budgetDAO.getBudgetStatus(
                    currentUserId, budget.getCategoryId(), currentMonth, currentYear);
            if (status != null) {
                budgetStatuses.add(status);
                Log.d(TAG, "  Status: spent=" + status.spent + ", percentage=" + status.percentage);
            } else {
                Log.e(TAG, "  Status is NULL!");
            }
        }

        Log.d(TAG, "Total statuses: " + budgetStatuses.size());

        // Update adapter
        if (budgetAdapter == null) {
            Log.d(TAG, "Creating new adapter");
            budgetAdapter = new BudgetAdapter(this, budgetStatuses, this::onBudgetItemClick);
            budgetListView.setAdapter(budgetAdapter);
        } else {
            Log.d(TAG, "Updating existing adapter");
            budgetAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, "Adapter count: " + (budgetAdapter != null ? budgetAdapter.getCount() : "null"));
        Log.d(TAG, "==================================");

        // Check for budget alerts
        notificationManager.checkAllBudgets(currentUserId, currentMonth, currentYear);
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Budget");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etThreshold = dialogView.findViewById(R.id.etThreshold);

        // Load categories
        List<Category> categories = categoryDAO.getAllActiveCategories();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        builder.setPositiveButton("Set Budget", (dialog, which) -> {
            String amountStr = etAmount.getText().toString();
            String thresholdStr = etThreshold.getText().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                double threshold = thresholdStr.isEmpty() ? 0.8 :
                        Double.parseDouble(thresholdStr) / 100.0;

                int selectedPosition = spinnerCategory.getSelectedItemPosition();
                Category selectedCategory = (Category) spinnerCategory.getSelectedItem();


                createOrUpdateBudget(selectedCategory, amount, threshold);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void createOrUpdateBudget(Category category, double amount, double threshold) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "CREATE/UPDATE BUDGET");
        Log.d(TAG, "Category: " + category.getName() + " (" + category.getCategoryId() + ")");
        Log.d(TAG, "Amount: " + amount);
        Log.d(TAG, "Month/Year: " + currentMonth + "/" + currentYear);

        Budget existingBudget = budgetDAO.getCategoryBudget(
                currentUserId, category.getCategoryId(), currentMonth, currentYear);

        if (existingBudget != null) {
            Log.d(TAG, "Budget exists - Updating...");
            // Update existing budget
            existingBudget.setAmountLimit(amount);
            existingBudget.setWarningThreshold(threshold);
            int updated = budgetDAO.updateBudget(existingBudget);
            Log.d(TAG, "Update result: " + updated);

            Toast.makeText(this, "Budget updated for " + category.getName(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Budget does NOT exist - Creating new...");
            // Create new budget
            String budgetId = "budget_" + System.currentTimeMillis();
            Budget newBudget = new Budget(
                    budgetId,
                    currentUserId,
                    category.getCategoryId(),
                    amount,
                    currentMonth,
                    currentYear
            );
            newBudget.setWarningThreshold(threshold);

            long result = budgetDAO.createBudget(newBudget);
            Log.d(TAG, "Create result: " + result);

            // result > 0 means insert success, result == 1 means update success
            if (result > 0 || result == 1) {
                Toast.makeText(this, "Budget set for " + category.getName(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create budget",
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "CREATE FAILED!");
                Log.d(TAG, "==========================================");
                return; // Don't reload if failed
            }
        }

        Log.d(TAG, "Calling loadBudgets()...");
        loadBudgets();
        Log.d(TAG, "==========================================");
    }

    private void onBudgetItemClick(BudgetStatus status) {
        Category category = categoryDAO.getCategoryById(status.budget.getCategoryId());
        if (category == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(category.getName() + " Budget");

        String message = String.format(
                "Limit: %s\nSpent: %s\nRemaining: %s\nUsage: %.1f%%\n\nStatus: %s",
                currencyFormat.format(status.budget.getAmountLimit()),
                currencyFormat.format(status.spent),
                currencyFormat.format(status.remaining),
                status.percentage,
                status.isOverBudget() ? "OVER BUDGET" :
                        status.isNearLimit() ? "WARNING" : "OK"
        );

        builder.setMessage(message);
        builder.setPositiveButton("Edit", (dialog, which) -> showEditBudgetDialog(status));
        builder.setNegativeButton("Delete", (dialog, which) -> deleteBudget(status.budget));
        builder.setNeutralButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditBudgetDialog(BudgetStatus status) {
        Category category = categoryDAO.getCategoryById(status.budget.getCategoryId());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Budget: " + (category != null ? category.getName() : ""));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etThreshold = dialogView.findViewById(R.id.etThreshold);

        etAmount.setText(String.valueOf((int) status.budget.getAmountLimit()));
        etThreshold.setText(String.valueOf((int) (status.budget.getWarningThreshold() * 100)));

        builder.setPositiveButton("Update", (dialog, which) -> {
            try {
                double amount = Double.parseDouble(etAmount.getText().toString());
                double threshold = Double.parseDouble(etThreshold.getText().toString()) / 100.0;

                status.budget.setAmountLimit(amount);
                status.budget.setWarningThreshold(threshold);
                budgetDAO.updateBudget(status.budget);

                Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show();
                loadBudgets();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void deleteBudget(Budget budget) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    budgetDAO.deleteBudget(budget.getBudgetId());
                    Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show();
                    loadBudgets();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changeMonth(int delta) {
        currentMonth += delta;

        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        } else if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }

        updateMonthYearDisplay();
        loadBudgets();
    }

    private void updateMonthYearDisplay() {
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        tvMonthYear.setText(monthNames[currentMonth - 1] + " " + currentYear);
    }

    /**
     * Custom adapter for budget list
     */
    private static class BudgetAdapter extends android.widget.BaseAdapter {
        private BudgetActivity activity;
        private List<BudgetStatus> budgetStatuses;
        private BudgetClickListener clickListener;

        interface BudgetClickListener {
            void onClick(BudgetStatus status);
        }

        public BudgetAdapter(BudgetActivity activity, List<BudgetStatus> budgetStatuses,
                             BudgetClickListener listener) {
            this.activity = activity;
            this.budgetStatuses = budgetStatuses;
            this.clickListener = listener;
        }

        @Override
        public int getCount() {
            return budgetStatuses.size();
        }

        @Override
        public BudgetStatus getItem(int position) {
            return budgetStatuses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.item_budget, parent, false);
            }

            BudgetStatus status = getItem(position);
            Category category = activity.categoryDAO.getCategoryById(
                    status.budget.getCategoryId());

            TextView tvCategoryName = convertView.findViewById(R.id.tvCategoryName);
            TextView tvBudgetAmount = convertView.findViewById(R.id.tvBudgetAmount);
            TextView tvSpentAmount = convertView.findViewById(R.id.tvSpentAmount);
            TextView tvPercentage = convertView.findViewById(R.id.tvPercentage);
            ProgressBar progressBar = convertView.findViewById(R.id.progressBar);
            View statusIndicator = convertView.findViewById(R.id.statusIndicator);

            if (category != null) {
                tvCategoryName.setText(category.getIcon() + " " + category.getName());
            }

            tvBudgetAmount.setText(activity.currencyFormat.format(status.budget.getAmountLimit()));
            tvSpentAmount.setText("Spent: " + activity.currencyFormat.format(status.spent));
            tvPercentage.setText(String.format("%.0f%%", status.percentage));

            int progress = (int) Math.min(status.percentage, 100);
            progressBar.setProgress(progress);

            // Set status indicator color
            if (status.isOverBudget()) {
                statusIndicator.setBackgroundColor(0xFFFF0000); // Red
            } else if (status.isNearLimit()) {
                statusIndicator.setBackgroundColor(0xFFFFA500); // Orange
            } else {
                statusIndicator.setBackgroundColor(0xFF00FF00); // Green
            }

            convertView.setOnClickListener(v -> clickListener.onClick(status));

            return convertView;
        }
    }
}