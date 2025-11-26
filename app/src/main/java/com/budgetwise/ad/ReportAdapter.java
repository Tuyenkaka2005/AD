package com.budgetwise.ad;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.ad.CategoryReportItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<CategoryReportItem> mList;
    private Context mContext;

    public ReportAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    public void setData(List<CategoryReportItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }


    @NonNull @Override
    public ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( @NonNull ViewHolder holder, int position) {
        CategoryReportItem item = mList.get(position);

        // 1. Set Icon và Tên
        holder.tvIcon.setText(item.getCategoryIcon());
        holder.tvName.setText(item.getCategoryName());

        // 2. Format số tiền (Ví dụ: 1,000,000 đ)
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvAmount.setText(formatter.format(item.getTotalAmount()));

        // 3. Set Phần trăm và Progress Bar
        holder.tvPercentage.setText(String.format("%.1f%%", item.getPercentage()));
        holder.progressBar.setProgress((int) item.getPercentage());
        
        // Đổi màu progress bar theo màu category (nếu có logic màu)
        try {
             holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(item.getCategoryColor())));
        } catch (Exception e) {
            // Mặc định nếu lỗi màu
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvAmount, tvPercentage;
        ProgressBar progressBar;

        public ViewHolder( @NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvCategoryAmount);
            tvPercentage = itemView.findViewById(R.id.tvCategoryPercentage);
            progressBar = itemView.findViewById(R.id.progressBarCategory);
        }
    }
}