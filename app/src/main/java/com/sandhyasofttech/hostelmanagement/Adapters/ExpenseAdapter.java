package com.sandhyasofttech.hostelmanagement.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Models.ExpenseModel;
import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private ArrayList<ExpenseModel> expenseList;

    public ExpenseAdapter(ArrayList<ExpenseModel> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseModel expense = expenseList.get(position);

        holder.tvCategory.setText(expense.getCategory());
        holder.tvAmount.setText("₹" + expense.getAmount());
        holder.tvDescription.setText(expense.getDescription());
        holder.tvDateTime.setText(expense.getDate() + " " + expense.getTime());

        // ✅ FIXED: Java 11 compatible switch
        Context context = holder.itemView.getContext();
        int color = getCategoryColor(expense.getCategory(), context);
        holder.tvCategory.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDescription, tvDateTime;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvDateTime = itemView.findViewById(R.id.tvExpenseDateTime);
        }
    }

    // ✅ FIXED: Traditional switch statement (Java 11 compatible)
    private int getCategoryColor(String category, Context context) {
        switch (category) {
            case "Food":
                return context.getResources().getColor(android.R.color.holo_orange_dark);
            case "Electricity":
                return context.getResources().getColor(android.R.color.holo_blue_dark);
            case "Water":
                return context.getResources().getColor(android.R.color.holo_green_dark);
            case "Maintenance":
                return context.getResources().getColor(android.R.color.holo_red_dark);
            case "Staff Salary":
                return context.getResources().getColor(android.R.color.holo_purple);
            case "Cleaning":
                return context.getResources().getColor(android.R.color.holo_green_light);
            default:
                return context.getResources().getColor(android.R.color.darker_gray);
        }
    }
}
