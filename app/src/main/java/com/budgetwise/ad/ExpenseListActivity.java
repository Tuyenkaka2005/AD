// ExpenseListActivity.java - ĐÃ SỬA HOÀN HẢO CHO LAYOUT MỚI
package com.budgetwise.ad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseListActivity extends AppCompatActivity {

    private RecyclerView recyclerExpenseList;
    private ExtendedFloatingActionButton fabAddExpense;
    private TextView tvTotalSpent;
    private View layoutEmpty;
    private MaterialToolbar toolbar;

    private ExpenseDAO expenseDAO;
    private ExpenseAdapter adapter;
    private List<Expense> expenses;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        userId = UserSession.getCurrentUserId(this);
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadData();
    }

    private void initViews() {
        recyclerExpenseList = findViewById(R.id.recyclerExpenseList);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        toolbar = findViewById(R.id.toolbar);

        recyclerExpenseList.setLayoutManager(new LinearLayoutManager(this));

        fabAddExpense.setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseActivity.class)));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiêu tháng này");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadData() {
        expenseDAO = new ExpenseDAO(this);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        expenses = expenseDAO.getUserExpenses(userId, month, year);

        // Tính tổng chi tiêu
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        tvTotalSpent.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                .format(total));

        // Hiển thị danh sách hoặc empty state
        if (expenses.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerExpenseList.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerExpenseList.setVisibility(View.VISIBLE);

            adapter = new ExpenseAdapter(this, expenses);
            recyclerExpenseList.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Tự động refresh khi thêm/sửa/xóa
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}