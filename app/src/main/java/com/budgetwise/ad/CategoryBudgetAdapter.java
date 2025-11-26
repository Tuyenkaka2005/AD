package com.budgetwise.ad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder> {

    private Context context;
    private List<CategorySummary> data = new ArrayList<>();

    public CategoryBudgetAdapter(Context context) {
        this.context = context;
    }

    public void updateData(List<CategorySummary> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_category_budget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        CategorySummary item = data.get(p);
        DecimalFormat f = new DecimalFormat("#,### đ");

        h.tvName.setText(item.categoryName);
        h.tvSpent.setText(f.format(item.spent));
        h.tvBudget.setText(f.format(item.budget));
        h.progressBar.setProgress((int) item.percent);

        int color = android.graphics.Color.parseColor(item.color != null && !item.color.isEmpty() ? item.color : "#95A5A6");
        h.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpent, tvBudget;
        ProgressBar progressBar;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCategoryName);
            tvSpent = v.findViewById(R.id.tvSpent);
            tvBudget = v.findViewById(R.id.tvBudget);
            progressBar = v.findViewById(R.id.progressCategory);
        }
    }
}