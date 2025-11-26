// RecurringExpenseActivity.java
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

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

        // Set mặc định cho interval nếu chưa có
        if (spinnerInterval.getSelectedItem() == null) {
            spinnerInterval.setSelection(0);
        }
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
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryId = ((Category) spinnerCategory.getSelectedItem()).getCategoryId();
        String interval = spinnerInterval.getSelectedItem().toString();

        long startDate = System.currentTimeMillis();
        long nextRun = RecurringHelper.calculateNextRunDate(interval, startDate);

        RecurringExpense recurring = new RecurringExpense(
                java.util.UUID.randomUUID().toString(),
                "user_demo",                    // ĐÃ SỬA: dùng user demo
                categoryId,
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
            OverviewHelper.generateMissedRecurringExpenses(this); // Tự động sinh expense nếu cần
        } else {
            Toast.makeText(this, "Lỗi khi thêm, thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etTitle.setText("");
        etAmount.setText("");
        etNote.setText("");
        spinnerCategory.setSelection(0);
    }

    private void loadRecurringExpenses() {
        recurringList.clear();
        recurringList.addAll(RecurringHelper.getAllRecurringExpenses(this));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecurringExpenses(); // Refresh khi quay lại
    }
}