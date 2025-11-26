package com.budgetwise.ad;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    private String currentUserId; // Sửa: không gán ở đây
    private int currentMonth;
    private int currentYear;
    private List<BudgetStatus> budgetStatuses;
    private BudgetAdapter budgetAdapter;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Lấy userId sau khi context đã có
        currentUserId = UserSession.getCurrentUserId(this);
        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

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

        for (Budget budget : budgets) {
            BudgetStatus status = budgetDAO.getBudgetStatus(
                    currentUserId, budget.getCategoryId(), currentMonth, currentYear);

            if (status != null) {
                budgetStatuses.add(status);
            }
        }

        // Sort: Over budget first → Near limit → Normal
        budgetStatuses.sort((a, b) -> {
            if (a.isOverBudget() && !b.isOverBudget()) return -1;
            if (!a.isOverBudget() && b.isOverBudget()) return 1;
            if (a.isNearLimit() && !b.isNearLimit()) return -1;
            if (!a.isNearLimit() && b.isNearLimit()) return 1;
            return Double.compare(b.percentage, a.percentage); // High usage first
        });

        if (budgetAdapter == null) {
            budgetAdapter = new BudgetAdapter(this, budgetStatuses, this::onBudgetItemClick);
            budgetListView.setAdapter(budgetAdapter);
        } else {
            budgetAdapter.notifyDataSetChanged();
        }

        // Kiểm tra cảnh báo ngân sách
        notificationManager.checkAllBudgets(currentUserId, currentMonth, currentYear);
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thiết lập ngân sách");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etThreshold = dialogView.findViewById(R.id.etThreshold);

        List<Category> categories = categoryDAO.getAllActiveCategories();
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new Exception();
            } catch (Exception e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            double threshold = 0.8;
            String thresholdStr = etThreshold.getText().toString().trim();
            if (!thresholdStr.isEmpty()) {
                try {
                    threshold = Double.parseDouble(thresholdStr) / 100.0;
                    if (threshold < 0.1 || threshold > 1.0) threshold = 0.8;
                } catch (Exception ignored) {}
            }

            Category category = (Category) spinnerCategory.getSelectedItem();
            if (category == null) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            createOrUpdateBudget(category, amount, threshold);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void createOrUpdateBudget(Category category, double amount, double threshold) {
        Budget existing = budgetDAO.getCategoryBudget(currentUserId, category.getCategoryId(), currentMonth, currentYear);

        if (existing != null) {
            existing.setAmountLimit(amount);
            existing.setWarningThreshold(threshold);
            budgetDAO.updateBudget(existing);
            Toast.makeText(this, "Đã cập nhật ngân sách: " + category.getName(), Toast.LENGTH_SHORT).show();
        } else {
            String budgetId = "budget_" + System.currentTimeMillis();
            Budget budget = new Budget(budgetId, currentUserId, category.getCategoryId(), amount, currentMonth, currentYear);
            budget.setWarningThreshold(threshold);
            long result = budgetDAO.createBudget(budget);
            if (result > 0) {
                Toast.makeText(this, "Đã thiết lập ngân sách: " + category.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi lưu ngân sách", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        loadBudgets();
    }

    private void onBudgetItemClick(BudgetStatus status) {
        Category category = categoryDAO.getCategoryById(status.budget.getCategoryId());
        String catName = category != null ? category.getName() : "Unknown";

        String message = String.format(Locale.getDefault(),
                "Ngân sách: %s\nĐã chi: %s\nCòn lại: %s\nTỷ lệ: %.1f%%\n\nTrạng thái: %s",
                currencyFormat.format(status.budget.getAmountLimit()),
                currencyFormat.format(status.spent),
                currencyFormat.format(status.remaining),
                status.percentage,
                status.isOverBudget() ? "VƯỢT NGÂN SÁCH" :
                        status.isNearLimit() ? "SẮP HẾT" : "AN TOÀN"
        );

        new AlertDialog.Builder(this)
                .setTitle(catName + " - Ngân sách")
                .setMessage(message)
                .setPositiveButton("Sửa", (d, w) -> showEditBudgetDialog(status))
                .setNegativeButton("Xóa", (d, w) -> deleteBudget(status.budget))
                .setNeutralButton("Đóng", null)
                .show();
    }

    private void showEditBudgetDialog(BudgetStatus status) {
        Category category = categoryDAO.getCategoryById(status.budget.getCategoryId());
        if (category == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa ngân sách: " + category.getName());

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_budget, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etThreshold = view.findViewById(R.id.etThreshold);

        etAmount.setText(String.valueOf((long) status.budget.getAmountLimit()));
        etThreshold.setText(String.valueOf((int) (status.budget.getWarningThreshold() * 100)));

        builder.setPositiveButton("Cập nhật", (d, w) -> {
            try {
                double amount = Double.parseDouble(etAmount.getText().toString());
                double threshold = Double.parseDouble(etThreshold.getText().toString()) / 100.0;

                status.budget.setAmountLimit(amount);
                status.budget.setWarningThreshold(threshold);
                budgetDAO.updateBudget(status.budget);

                Toast.makeText(this, "Đã cập nhật ngân sách", Toast.LENGTH_SHORT).show();
                loadBudgets();
            } catch (Exception e) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void deleteBudget(Budget budget) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ngân sách")
                .setMessage("Bạn có chắc muốn xóa ngân sách này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    budgetDAO.deleteBudget(budget.getBudgetId());
                    Toast.makeText(this, "Đã xóa ngân sách", Toast.LENGTH_SHORT).show();
                    loadBudgets();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void changeMonth(int delta) {
        currentMonth += delta;
        if (currentMonth > 12) { currentMonth = 1; currentYear++; }
        else if (currentMonth < 1) { currentMonth = 12; currentYear--; }
        updateMonthYearDisplay();
        loadBudgets();
    }

    private void updateMonthYearDisplay() {
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        tvMonthYear.setText(months[currentMonth - 1] + " " + currentYear);
    }

    // Adapter giữ nguyên – đã ổn
    private static class BudgetAdapter extends android.widget.BaseAdapter {
        private final BudgetActivity activity;
        private final List<BudgetStatus> budgetStatuses;
        private final BudgetClickListener listener;

        interface BudgetClickListener { void onClick(BudgetStatus status); }

        public BudgetAdapter(BudgetActivity activity, List<BudgetStatus> budgetStatuses, BudgetClickListener listener) {
            this.activity = activity;
            this.budgetStatuses = budgetStatuses;
            this.listener = listener;
        }

        @Override public int getCount() { return budgetStatuses.size(); }
        @Override public BudgetStatus getItem(int p) { return budgetStatuses.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int p, View v, android.view.ViewGroup parent) {
            if (v == null) {
                v = activity.getLayoutInflater().inflate(R.layout.item_budget, parent, false);
            }

            BudgetStatus s = getItem(p);
            Category c = activity.categoryDAO.getCategoryById(s.budget.getCategoryId());

            TextView tvName = v.findViewById(R.id.tvCategoryName);
            TextView tvLimit = v.findViewById(R.id.tvBudgetAmount);
            TextView tvSpent = v.findViewById(R.id.tvSpentAmount);
            TextView tvPercent = v.findViewById(R.id.tvPercentage);
            ProgressBar pb = v.findViewById(R.id.progressBar);
            View indicator = v.findViewById(R.id.statusIndicator);

            tvName.setText(c != null ? c.getIcon() + " " + c.getName() : "Unknown");
            tvLimit.setText(activity.currencyFormat.format(s.budget.getAmountLimit()));
            tvSpent.setText("Đã chi: " + activity.currencyFormat.format(s.spent));
            tvPercent.setText(String.format("%.0f%%", s.percentage));

            int progress = (int) Math.min(s.percentage, 100);
            pb.setProgress(progress);

            if (s.isOverBudget()) indicator.setBackgroundColor(0xFFFF0000);
            else if (s.isNearLimit()) indicator.setBackgroundColor(0xFFFFA500);
            else indicator.setBackgroundColor(0xFF00FF00);

            v.setOnClickListener(view -> listener.onClick(s));
            return v;
        }
    }
}