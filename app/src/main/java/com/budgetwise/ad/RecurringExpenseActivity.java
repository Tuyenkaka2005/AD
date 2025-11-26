// RecurringExpenseActivity.java - ĐÃ SỬA HOÀN HẢO, ĐẸP, ỔN ĐỊNH
package com.budgetwise.ad;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etNote;
    private Spinner spinnerCategory, spinnerInterval;
    private Button btnAddRecurring;
    private RecyclerView rvRecurring;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringList;

    private String currentUserId; // Thêm biến này

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        // Lấy userId ngay khi có context
        currentUserId = UserSession.getCurrentUserId(this);
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        loadRecurringExpenses();

        btnAddRecurring.setOnClickListener(v -> addRecurringExpense());
    }

    private void initViews() {
        etTitle = findViewById(R.id.etRecurringTitle);
        etAmount = findViewById(R.id.etRecurringAmount);
        etNote = findViewById(R.id.etRecurringNote);
        spinnerCategory = findViewById(R.id.spinnerRecurringCategory);
        spinnerInterval = findViewById(R.id.spinnerRecurringInterval);
        btnAddRecurring = findViewById(R.id.btnAddRecurring);
        rvRecurring = findViewById(R.id.rvRecurringExpenses);

        recurringList = new ArrayList<>();
        adapter = new RecurringExpenseAdapter(this, recurringList, this::loadRecurringExpenses);
        rvRecurring.setLayoutManager(new LinearLayoutManager(this));
        rvRecurring.setAdapter(adapter);

        // Load danh mục vào Spinner
        CategoryHelper.loadCategoriesIntoSpinner(this, spinnerCategory);

        // Gán adapter cho spinnerInterval (bắt buộc phải có!)
        String[] intervals = {"Hàng tuần", "2 tuần", "Hàng tháng", "Hàng năm"};
        spinnerInterval.setAdapter(new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, intervals));
        spinnerInterval.setSelection(2); // Mặc định là "Hàng tháng"
    }

    private void addRecurringExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Chưa có danh mục nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.replace(",", ""));
            if (amount <= 0) throw new Exception();
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        String interval = spinnerInterval.getSelectedItem().toString();

        long startDate = System.currentTimeMillis();
        long nextRun = RecurringHelper.calculateNextRunDate(interval, startDate);

        RecurringExpense recurring = new RecurringExpense(
                java.util.UUID.randomUUID().toString(),
                currentUserId,  // ĐÃ SỬA: dùng biến đã khai báo
                selectedCategory.getCategoryId(),
                title,
                amount,
                note.isEmpty() ? null : note,
                interval,
                startDate,
                nextRun
        );

        if (RecurringHelper.insertRecurringExpense(this, recurring)) {
            Toast.makeText(this, "Đã thêm chi phí định kỳ thành công!", Toast.LENGTH_SHORT).show();
            clearForm();
            loadRecurringExpenses();
            OverviewHelper.generateMissedRecurringExpenses(this); // Tự động tạo expense nếu cần
        } else {
            Toast.makeText(this, "Lỗi khi thêm, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etTitle.setText("");
        etAmount.setText("");
        etNote.setText("");
        spinnerCategory.setSelection(0);
        spinnerInterval.setSelection(2);
    }

    private void loadRecurringExpenses() {
        recurringList.clear();
        recurringList.addAll(RecurringHelper.getAllRecurringExpenses(this));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecurringExpenses(); // Tự động refresh
    }
}