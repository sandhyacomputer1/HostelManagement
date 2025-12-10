package com.sandhyasofttech.hostelmanagement.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttech.hostelmanagement.Activities.UpdateStudentActivity;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    Context context;
    ArrayList<StudentModel> list;

    public StudentAdapter(Context context, ArrayList<StudentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StudentViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.row_student, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder h, int pos) {
        StudentModel m = list.get(pos);

        h.tvName.setText(m.getName());
        h.tvRoom.setText("Room: " + m.getRoom());
        h.tvPhone.setText(m.getPhone());

        Glide.with(context)
                .load(m.getPhotoUrl())
                .placeholder(R.drawable.ic_user)
                .into(h.ivPhoto);

        h.ivMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, h.ivMenu);
            menu.getMenu().add("Edit");
            menu.getMenu().add("Delete");
            menu.getMenu().add(m.isActive() ? "Mark as Leave" : "Mark as Active");

            menu.setOnMenuItemClickListener(item -> {

                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if(email == null) return false;

                String safeEmail = email.replace(".", ",");

                if (item.getTitle().equals("Edit")) {

                    Intent i = new Intent(context, UpdateStudentActivity.class);
                    i.putExtra("id", m.getId());
                    context.startActivity(i);

                }
                else if (item.getTitle().equals("Delete")) {

                    FirebaseDatabase.getInstance().getReference("HostelManagement")
                            .child(safeEmail)
                            .child("Students")
                            .child(m.getId())
                            .removeValue();

                }
                else {

                    boolean newStatus = !m.isActive(); // toggle
                    m.setActive(newStatus); // VERY IMPORTANT 🔥

                    FirebaseDatabase.getInstance().getReference("HostelManagement")
                            .child(safeEmail)
                            .child("Students")
                            .child(m.getId())
                            .child("active")
                            .setValue(newStatus);
                }

                return true;
            });


            menu.show();
        });
    }
    public void updateList(ArrayList<StudentModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() { return list.size(); }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto, ivMenu;
        TextView tvName, tvRoom, tvPhone;

        public StudentViewHolder(@NonNull View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.ivStudentPhoto);
            ivMenu = v.findViewById(R.id.ivMenu);
            tvName = v.findViewById(R.id.tvStudentName);
            tvRoom = v.findViewById(R.id.tvStudentRoom);
            tvPhone = v.findViewById(R.id.tvStudentPhone);
        }
    }
}
