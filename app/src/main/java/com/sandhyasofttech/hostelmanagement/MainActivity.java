package com.sandhyasofttech.hostelmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Activities.AddStudentActivity;
import com.sandhyasofttech.hostelmanagement.Activities.CollectFeesActivity;
import com.sandhyasofttech.hostelmanagement.Activities.ExpenseListActivity;
import com.sandhyasofttech.hostelmanagement.Activities.FeeHistoryActivity;
import com.sandhyasofttech.hostelmanagement.Activities.ProfileActivity;
import com.sandhyasofttech.hostelmanagement.Activities.RoomsActivity;
import com.sandhyasofttech.hostelmanagement.Activities.StudentListActivity;
import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView ivToolbarLogo;
    private TextView tvToolbarHostelName, tvCurrentDate;
    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
    private TextView tvOccupiedRooms, tvAvailableRooms, tvTotalRevenue, tvPendingStudents;
    private CardView cardAddStudent, cardCollectFees, cardFeeHistory, cardManageRooms;

    private DatabaseReference rootRef;
    private String safeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check Login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        initFirebaseRefs();
        setCurrentDate();
        loadHostelDetails();
        loadDashboardStats();
        setupClickListeners();
        setupNavHeader();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ivToolbarLogo = findViewById(R.id.ivToolbarLogo);
        tvToolbarHostelName = findViewById(R.id.tvToolbarHostelName);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);

        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvTotalStudentsDashboard = findViewById(R.id.tvTotalStudentsDashboard);
        tvPendingFees = findViewById(R.id.tvPendingFees);
        tvOccupiedRooms = findViewById(R.id.tvOccupiedRooms);
        tvAvailableRooms = findViewById(R.id.tvAvailableRooms);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvPendingStudents = findViewById(R.id.tvPendingStudents);

        cardAddStudent = findViewById(R.id.cardAddStudent);
        cardCollectFees = findViewById(R.id.cardCollectFees);
        cardFeeHistory = findViewById(R.id.cardFeeHistory);
        cardManageRooms = findViewById(R.id.cardManageRooms);
    }

    private void initFirebaseRefs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String email = user.getEmail();
        if (email == null) return;

        safeEmail = email.replace(".", ",");

        rootRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail);
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvCurrentDate.setText(currentDate);
    }

    private void setupClickListeners() {
        ivToolbarLogo.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        cardAddStudent.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddStudentActivity.class)));

        cardCollectFees.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class)));

        cardFeeHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class)));

        cardManageRooms.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StudentListActivity.class)));
    }

    private void setupNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);
        ImageView ivProfileImage = headerView.findViewById(R.id.ivProfileImage);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Admin");
            tvUserEmail.setText(user.getEmail());

            // Load profile photo if available
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(ivProfileImage);
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.menuAddStudent) {
                startActivity(new Intent(MainActivity.this, AddStudentActivity.class));
            }
            if (item.getItemId() == R.id.menuStudents) {
                startActivity(new Intent(MainActivity.this, StudentListActivity.class));
            }
            if (item.getItemId() == R.id.menuCollectFees) {
                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class));
            }
            if (item.getItemId() == R.id.menuCollectFeesHistory) {
                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class));
            }
            if (item.getItemId() == R.id.menuExpenses) {
                startActivity(new Intent(MainActivity.this, ExpenseListActivity.class));
            }
            if (item.getItemId() == R.id.menuRooms) {
                startActivity(new Intent(MainActivity.this, RoomsActivity.class));
            }
            if (item.getItemId() == R.id.menuLogout) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void loadHostelDetails() {
        if (rootRef == null) return;

        rootRef.child("ownerInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String hostelName = snapshot.child("hostelName").getValue(String.class);
                String logoUrl = snapshot.child("logoUrl").getValue(String.class);
                String rooms = snapshot.child("rooms").getValue(String.class);

                if (hostelName != null)
                    tvToolbarHostelName.setText(hostelName);

                if (rooms != null)
                    tvTotalRooms.setText(rooms);

                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Glide.with(MainActivity.this)
                            .load(logoUrl)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .circleCrop()
                            .into(ivToolbarLogo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadDashboardStats() {
        if (rootRef == null) return;

        rootRef.child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalStudents = 0;
                int totalRemaining = 0;
                int studentsWithPending = 0;
                int totalRevenue = 0;
                HashSet<String> occupiedRoomsSet = new HashSet<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    StudentModel s = ds.getValue(StudentModel.class);
                    if (s != null && s.isActive()) {
                        totalStudents++;

                        // Calculate pending fees
                        int remaining = s.getRemainingFee();
                        totalRemaining += remaining;
                        if (remaining > 0) {
                            studentsWithPending++;
                        }

//                        // Calculate total revenue (total fee - remaining fee)
//                        totalRevenue += (s.getTotalFee() - remaining);
//
//                        // Track occupied rooms
//                        if (s.getRoomNo() != null && !s.getRoomNo().isEmpty()) {
//                            occupiedRoomsSet.add(s.getRoomNo());
//                        }
                    }
                }

                // Update UI
                tvTotalStudentsDashboard.setText(String.valueOf(totalStudents));
                tvPendingFees.setText("₹ " + totalRemaining);
                tvPendingStudents.setText(studentsWithPending + " students");
                tvTotalRevenue.setText("₹ " + totalRevenue);

                // Update occupied and available rooms
                int occupiedRooms = occupiedRoomsSet.size();
                tvOccupiedRooms.setText(String.valueOf(occupiedRooms));

                // Calculate available rooms
                String totalRoomsStr = tvTotalRooms.getText().toString();
                try {
                    int totalRooms = Integer.parseInt(totalRoomsStr);
                    int availableRooms = totalRooms - occupiedRooms;
                    tvAvailableRooms.setText(String.valueOf(Math.max(0, availableRooms)));
                } catch (NumberFormatException e) {
                    tvAvailableRooms.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadHostelDetails();
        loadDashboardStats();
    }
}