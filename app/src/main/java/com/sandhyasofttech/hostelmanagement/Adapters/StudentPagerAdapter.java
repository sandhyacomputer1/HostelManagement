package com.sandhyasofttech.hostelmanagement.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.util.ArrayList;
import android.content.Context;

public class StudentPagerAdapter extends RecyclerView.Adapter<StudentPagerAdapter.PageHolder> {

    Context context;
    ArrayList<StudentModel> activeList;
    ArrayList<StudentModel> leaveList;

    public StudentPagerAdapter(Context ctx, ArrayList<StudentModel> active, ArrayList<StudentModel> leave) {
        this.context = ctx;
        this.activeList = active;
        this.leaveList = leave;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        if (viewType == 0) {
            v = LayoutInflater.from(context).inflate(R.layout.active_students_page, parent, false);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.leave_students_page, parent, false);
        }

        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int pos) {

        RecyclerView rv = holder.itemView.findViewById(pos == 0 ? R.id.rvActive : R.id.rvLeave);
        rv.setLayoutManager(new LinearLayoutManager(context));

        StudentAdapter adapter = new StudentAdapter(context, pos == 0 ? activeList : leaveList);
        rv.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return 2; // Active, Leave
    }

    @Override
    public int getItemViewType(int position) {
        return position; // 0=Active, 1=Leave
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        public PageHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
