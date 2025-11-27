package com.budgetwise.ad;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.Calendar;
import java.util.List;

public class SearchFilterActivity extends AppCompatActivity {

    private ExpenseDAO dao;
    private ExpenseSearchAdapter adapter;
    private String keyword = "";
    private String catId = null;
    private Integer month = null, year = null;
    private Double minAmount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_expense);  // ĐÃ FIX: file phải tên đúng là activity_search_filter.xml

        dao = new ExpenseDAO(this);
        adapter = new ExpenseSearchAdapter();

        RecyclerView rv = findViewById(R.id.rv_search_results);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Tìm kiếm realtime - ĐÃ FIX TextWatcher
        ((EditText) findViewById(R.id.et_search_input)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                keyword = s.toString().trim();
                search();
            }
        });

        setupChips();
        search();
    }

    private void setupChips() {
        ChipGroup group = findViewById(R.id.chip_group_filter);

        // Tháng này
        addChip(group, "Tháng này", () -> {
            Calendar c = Calendar.getInstance();
            month = c.get(Calendar.MONTH) + 1;
            year = c.get(Calendar.YEAR);
        }, () -> { month = null; year = null; });

        // Tất cả danh mục (tự động)
        new CategoryDAO(this).getAllActiveCategories().forEach(cat -> {
            addChip(group, cat.getIcon() + " " + cat.getName(), () -> catId = cat.getCategoryId(),
                    () -> { if (catId != null && catId.equals(cat.getCategoryId())) catId = null; });
        });

        // > 100k
        addChip(group, "> 100k", () -> minAmount = 100000.0, () -> minAmount = null);

        // Bạn có thể thêm thoải mái: >50k, <10k, Tuần này, Hôm qua...
    }

    private void addChip(ChipGroup g, String text, Runnable onSelect, Runnable onUnselect) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setOnCheckedChangeListener((v, checked) -> {
            if (checked) onSelect.run();
            else onUnselect.run();
            search();
        });
        g.addView(chip);
    }

    private void search() {
        List<Expense> list = dao.searchExpenses("user_demo",
                keyword.isEmpty() ? null : keyword, catId, month, year, minAmount);

        adapter.updateData(list);

        // ĐÃ FIX lỗi View.VISIBLE / View.GONE
        findViewById(R.id.rv_search_results).setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.layout_empty_state).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}