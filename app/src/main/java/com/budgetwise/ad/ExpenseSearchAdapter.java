package com.budgetwise.ad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseSearchAdapter extends RecyclerView.Adapter<ExpenseSearchAdapter.VH> {

    private List<Expense> data = new ArrayList<>();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void updateData(List<Expense> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_search, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {
        Expense e = data.get(p);
        Category c = new CategoryDAO(h.itemView.getContext()).getCategoryById(e.getCategoryId());

        h.tvTitle.setText((c != null ? c.getIcon() + " " : "") + e.getTitle());
        h.tvInfo.setText((c != null ? c.getName() : "Khác") + " • " + df.format(new Date(e.getDate())));
        h.tvAmount.setText(String.format(Locale.getDefault(), "-%,.0fđ", e.getAmount()));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle = itemView.findViewById(R.id.tv_expense_title);
        TextView tvInfo = itemView.findViewById(R.id.tv_category_date);
        TextView tvAmount = itemView.findViewById(R.id.tv_expense_amount);
        VH(View v) { super(v); }
    }
}