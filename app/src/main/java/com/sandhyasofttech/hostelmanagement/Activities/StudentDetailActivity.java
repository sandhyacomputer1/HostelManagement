package com.sandhyasofttech.hostelmanagement.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

public class StudentDetailActivity extends AppCompatActivity {

    TextView tvName, tvPhone, tvParentName, tvParentPhone, tvAddress, tvRoom,
            tvStudentClass, tvJoiningDate, tvAnnualFee, tvPaidFee, tvRemainingFee;
    Chip chipStatus;
    ImageView ivStudentPhoto, ivAadhaar, ivPan;
    CardView cardAadhaar, cardPan;
    View phoneCallIcon, parentPhoneCallIcon;

    String studentId, safeEmail;
    DatabaseReference studentRef;
    StudentModel currentStudent;
    ValueEventListener studentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        studentId = getIntent().getStringExtra("student_id");
        safeEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        init();
        setupToolbar();
        loadStudent();
    }

    private void init() {
        tvName = findViewById(R.id.tvStudentName);
        tvPhone = findViewById(R.id.tvPhone);
        tvParentName = findViewById(R.id.tvParentName);
        tvParentPhone = findViewById(R.id.tvParentPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvRoom = findViewById(R.id.tvRoom);
        tvStudentClass = findViewById(R.id.tvStudentClass);
        tvJoiningDate = findViewById(R.id.tvJoiningDate);
        tvAnnualFee = findViewById(R.id.tvAnnualFee);
        tvPaidFee = findViewById(R.id.tvPaidFee);
        tvRemainingFee = findViewById(R.id.tvRemainingFee);
        chipStatus = findViewById(R.id.chipStatus);

        ivStudentPhoto = findViewById(R.id.ivStudentPhoto);
        ivAadhaar = findViewById(R.id.ivAadhaar);
        ivPan = findViewById(R.id.ivPan);

        cardAadhaar = findViewById(R.id.cardAadhaar);
        cardPan = findViewById(R.id.cardPan);

        phoneCallIcon = findViewById(R.id.phoneCallIcon);
        parentPhoneCallIcon = findViewById(R.id.parentPhoneCallIcon);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadStudent() {
        studentRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Students")
                .child(studentId);

        studentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentStudent = snapshot.getValue(StudentModel.class);
                if (currentStudent != null) {
                    currentStudent.setId(studentId);
                    setData(currentStudent);
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentDetail", error.getMessage());
                Toast.makeText(StudentDetailActivity.this, "Failed to load student data", Toast.LENGTH_SHORT).show();
            }
        };

        studentRef.addValueEventListener(studentListener);
    }

    private void setData(StudentModel s) {
        tvName.setText(s.getName());
        tvPhone.setText(s.getPhone());
        tvParentName.setText(s.getParentName());
        tvParentPhone.setText(s.getParentPhone());
        tvAddress.setText(s.getAddress());
        tvRoom.setText(s.getRoom());
        tvStudentClass.setText(s.getStudentClass());
        tvJoiningDate.setText(s.getJoiningDate());
        tvAnnualFee.setText("₹" + s.getAnnualFee());
        tvPaidFee.setText("₹" + s.getPaidFee());
        tvRemainingFee.setText("₹" + s.getRemainingFee());

        // Status chip styling
        if (s.isActive()) {
            chipStatus.setText("Active");
            chipStatus.setChipBackgroundColorResource(R.color.status_active_bg);
            chipStatus.setTextColor(Color.parseColor("#1B5E20"));
        } else {
            chipStatus.setText("On Leave");
            chipStatus.setChipBackgroundColorResource(R.color.status_leave_bg);
            chipStatus.setTextColor(Color.parseColor("#B71C1C"));
        }

        // Load images
        Glide.with(this)
                .load(s.getPhotoUrl())
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_person_placeholder)
                .into(ivStudentPhoto);

        Glide.with(this)
                .load(s.getAadhaarPhotoUrl())
                .placeholder(R.drawable.ic_document_placeholder)
                .into(ivAadhaar);

        Glide.with(this)
                .load(s.getPanPhotoUrl())
                .placeholder(R.drawable.ic_document_placeholder)
                .into(ivPan);

        // Click listeners for phone calls
        phoneCallIcon.setOnClickListener(v -> makePhoneCall(s.getPhone()));
        parentPhoneCallIcon.setOnClickListener(v -> makePhoneCall(s.getParentPhone()));

        // Click listeners for document viewing
        cardAadhaar.setOnClickListener(v -> viewDocument(s.getAadhaarPhotoUrl(), "Aadhaar Card"));
        cardPan.setOnClickListener(v -> viewDocument(s.getPanPhotoUrl(), "PAN Card"));
    }

    private void makePhoneCall(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewDocument(String imageUrl, String title) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // TODO: Implement full-screen image viewer or open in gallery
            Toast.makeText(this, "Opening " + title, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Document not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_detail, menu);

        MenuItem leave = menu.findItem(R.id.action_leave);
        if (currentStudent != null && !currentStudent.isActive()) {
            leave.setTitle("Mark Active");
            leave.setIcon(R.drawable.ic_check);
        } else {
            leave.setTitle("Mark Leave");
            leave.setIcon(R.drawable.ic_leave);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (currentStudent == null) return true;

        int id = item.getItemId();

        if (id == R.id.action_edit) {
            // TODO: Navigate to EditStudentActivity
            Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_leave) {
            updateStatus();
            return true;

        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateStatus() {
        boolean newState = !currentStudent.isActive();
        String statusText = newState ? "active" : "on leave";

        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setMessage("Mark " + currentStudent.getName() + " as " + statusText + "?")
                .setPositiveButton("Confirm", (d, i) -> {
                    studentRef.child("active").setValue(newState)
                            .addOnSuccessListener(a -> {
                                currentStudent.setActive(newState);
                                setData(currentStudent);
                                invalidateOptionsMenu();
                                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete " + currentStudent.getName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (d, i) -> {
                    studentRef.removeValue()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete student", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (studentRef != null && studentListener != null) {
            studentRef.removeEventListener(studentListener);
        }
    }
}