package com.budgetwise.ad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeUI();
    }

    private void initializeUI() {

        // Expense Tracking
//        findViewById(R.id.cardExpenseTracking).setOnClickListener(v ->
//                startActivity(new Intent(this, ExpenseActivity.class)));

        // Budget Setup
 //       findViewById(R.id.cardBudgetSetup).setOnClickListener(v ->
  //              startActivity(new Intent(this, BudgetActivity.class)));

//        // Expense Overview
//        findViewById(R.id.cardExpenseOverview).setOnClickListener(v ->
//                startActivity(new Intent(this, OverviewActivity.class)));

//        // Recurring Expenses
//        findViewById(R.id.cardRecurringExpenses).setOnClickListener(v ->
//                startActivity(new Intent(this, RecurringExpenseActivity.class)));

        // Expense Report
        View expenseReportCard = findViewById(R.id.cardExpenseReport);
        if (expenseReportCard != null) {
            android.widget.TextView featureTitle = expenseReportCard.findViewById(R.id.featureTitle);
            if (featureTitle != null) {
                featureTitle.setText("Báo cáo chi phí");
            }
            expenseReportCard.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportActivity.class)));
        }

        // Search & Filter
        View searchFilterCard = findViewById(R.id.cardSearchFilter);
        if (searchFilterCard != null) {
            android.widget.TextView featureTitle = searchFilterCard.findViewById(R.id.featureTitle);
            if (featureTitle != null) {
                featureTitle.setText("Tìm kiếm & Lọc Chi Phí");
            }
            searchFilterCard.setOnClickListener(v ->
                    startActivity(new Intent(this, SearchFilterActivity.class)));
        }
    }
}
