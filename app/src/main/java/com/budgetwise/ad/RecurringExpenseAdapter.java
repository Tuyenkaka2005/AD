// RecurringExpenseAdapter.java
package com.budgetwise.ad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.ViewHolder> {

    private Context context;
    private List<RecurringExpense> list;
    private Runnable onUpdate;

    public RecurringExpenseAdapter(Context context, List<RecurringExpense> list, Runnable onUpdate) {
        this.context = context;
        this.list = list;
        this.onUpdate = onUpdate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recurring_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        RecurringExpense re = list.get(p);
        CategoryDAO dao = new CategoryDAO(context);
        Category cat = dao.getCategoryById(re.categoryId);

        h.tvTitle.setText(re.title);
        h.tvAmount.setText(String.format(Locale.getDefault(), "%,.0f đ", re.amount));
        h.tvCategory.setText(cat != null ? cat.getName() : "Không xác định");
        h.tvInterval.setText("Mỗi " + re.interval.toLowerCase());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        h.tvNextRun.setText("Lần tới: " + sdf.format(new Date(re.nextRunDate)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvCategory, tvInterval, tvNextRun;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvRecurringTitle);
            tvAmount = v.findViewById(R.id.tvRecurringAmount);
            tvCategory = v.findViewById(R.id.tvRecurringCategory);
            tvInterval = v.findViewById(R.id.tvRecurringInterval);
            tvNextRun = v.findViewById(R.id.tvNextRun);
        }
    }
}