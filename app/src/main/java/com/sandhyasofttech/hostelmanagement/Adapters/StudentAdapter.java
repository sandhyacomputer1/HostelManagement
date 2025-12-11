package com.sandhyasofttech.hostelmanagement.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sandhyasofttech.hostelmanagement.Activities.StudentDetailActivity;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<StudentModel> studentList;
    private final boolean isActiveList;

    public StudentAdapter(Context context, ArrayList<StudentModel> studentList, boolean isActiveList) {
        this.context = context;
        this.studentList = studentList;
        this.isActiveList = isActiveList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        StudentModel student = studentList.get(position);

        holder.tvName.setText(student.getName());
        holder.tvRoom.setText("Room " + student.getRoom());
        holder.tvClass.setText(student.getStudentClass());
        holder.tvStatus.setText(student.isActive() ? "Active" : "Leave");
        holder.tvFeeSummary.setText("Paid: " + student.getPaidFee() + " / " + student.getAnnualFee());

        Glide.with(context)
                .load(student.getPhotoUrl())
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(holder.ivPhoto);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentDetailActivity.class);
            intent.putExtra("student_id", student.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return studentList == null ? 0 : studentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        TextView tvName, tvRoom, tvClass, tvStatus, tvFeeSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPhoto = itemView.findViewById(R.id.ivStudentPhoto);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvFeeSummary = itemView.findViewById(R.id.tvFeeSummary);
        }
    }
}
