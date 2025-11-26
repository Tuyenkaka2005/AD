// OverviewActivity.java
package com.budgetwise.ad;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.Calendar;

public class OverviewActivity extends AppCompatActivity {

    private TextView tvTotalSpent, tvTotalBudget, tvRemaining;
    private RecyclerView rvCategorySummary;
    private CategoryBudgetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Quan trọng: Sinh các khoản định kỳ trước khi tính tổng
        OverviewHelper.generateMissedRecurringExpenses(this);

        initViews();
        loadMonthlySummary();
    }

    private void initViews() {
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvRemaining = findViewById(R.id.tvRemaining);
        rvCategorySummary = findViewById(R.id.rvCategorySummary);

        adapter = new CategoryBudgetAdapter(this);
        rvCategorySummary.setLayoutManager(new LinearLayoutManager(this));
        rvCategorySummary.setAdapter(adapter);
    }

    private void loadMonthlySummary() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        MonthlySummary summary = OverviewHelper.getMonthlySummary(this, month, year);

        DecimalFormat formatter = new DecimalFormat("#,### đ");
        tvTotalSpent.setText(formatter.format(summary.totalSpent));
        tvTotalBudget.setText(formatter.format(summary.totalBudget));
        tvRemaining.setText(formatter.format(summary.remaining));

        adapter.updateData(summary.categorySummaries);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMonthlySummary(); // Refresh khi quay lại
    }
}