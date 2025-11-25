package com.budgetwise.ad;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.ad.CategoryReportItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvGrandTotal;
    private PieChart pieChart;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;

    private Button btnThisMonth, btnLastMonth, btnThisYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // 1. Khởi tạo Views
        initViews();

        // 2. Khởi tạo Database & Adapter
        dbHelper = DatabaseHelper.getInstance(this);
        adapter = new ReportAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 3. Setup sự kiện click cho các nút lọc
        setupEvents();

        // 4. Mặc định load dữ liệu tháng này khi mở lên
        loadDataForThisMonth();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarReport);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Nút back

        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        pieChart = findViewById(R.id.pieChart);
        recyclerView = findViewById(R.id.rvReportDetails);
        
        btnThisMonth = findViewById(R.id.btnThisMonth);
        btnLastMonth = findViewById(R.id.btnLastMonth);
        btnThisYear = findViewById(R.id.btnThisYear);
    }

    private void setupEvents() {
        btnThisMonth.setOnClickListener(v -> loadDataForThisMonth());
        
        btnLastMonth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1); // Trừ 1 tháng
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            long start = getStartOfDay(calendar);

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            long end = getEndOfDay(calendar);
            
            updateButtonStyle(btnLastMonth);
            loadData(start, end);
        });

        btnThisYear.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, 1); // Ngày đầu năm
            long start = getStartOfDay(calendar);

            calendar.set(Calendar.MONTH, 11); // Tháng 12
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            long end = getEndOfDay(calendar);
            
            updateButtonStyle(btnThisYear);
            loadData(start, end);
        });
    }

    private void loadDataForThisMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long start = getStartOfDay(calendar);

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        long end = getEndOfDay(calendar);
        
        updateButtonStyle(btnThisMonth);
        loadData(start, end);
    }

    private void loadData(long startDate, long endDate) {
        // Giả sử User ID là "user_01" (Bạn cần thay bằng ID thật khi có login)
        String userId = "user_01"; 

        // Lấy dữ liệu từ DB
        double total = dbHelper.getTotalExpenseForPeriod(userId, startDate, endDate);
        List<CategoryReportItem> items = dbHelper.getCategoryReport(userId, startDate, endDate);

        // Hiển thị Tổng tiền
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvGrandTotal.setText(formatter.format(total));

        // Cập nhật list
        if (total > 0) {
            for (CategoryReportItem item : items) {

                item.setPercentage((float) ((item.getTotalAmount() / total) * 100));
            }
            setupPieChart(items, (float) total);
        } else {
            pieChart.clear(); // Xóa biểu đồ nếu ko có data
            pieChart.setNoDataText("Không có dữ liệu chi tiêu");
        }
        
        adapter.setData(items);
    }

    private void setupPieChart(List<CategoryReportItem> items, float total) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (CategoryReportItem item : items) {
            // Chỉ hiển thị mục > 3% lên biểu đồ cho đẹp
            if (item.getPercentage() > 3.0) {
                entries.add(new PieEntry((float) item.getTotalAmount(), item.getCategoryName()));
                try {
                    colors.add(Color.parseColor(item.getCategoryColor()));
                } catch (Exception e) {
                    colors.add(Color.GRAY);
                }
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDescription(null);
        pieChart.setCenterText("Chi tiêu");
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000);
        pieChart.invalidate(); // Vẽ lại
    }

    // Helper: Reset màu nút bấm
    private void updateButtonStyle(Button activeBtn) {
        // Code đơn giản để đổi màu nút đang chọn (bạn có thể tùy biến thêm)
        btnThisMonth.setAlpha(0.5f);
        btnLastMonth.setAlpha(0.5f);
        btnThisYear.setAlpha(0.5f);
        activeBtn.setAlpha(1.0f);
    }

    // Helper: Lấy thời gian bắt đầu ngày (00:00:00)
    private long getStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    // Helper: Lấy thời gian cuối ngày (23:59:59)
    private long getEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }
}