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
import com.sandhyasofttech.hostelmanagement.Activities.StudentListActivity;
import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView ivToolbarLogo;
    private TextView tvToolbarHostelName;
    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
    private androidx.cardview.widget.CardView cardAddStudent, cardCollectFees, cardFeeHistory;

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
        loadHostelDetails();
        loadDashboardStats();

        ivToolbarLogo.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Quick actions
        cardAddStudent.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddStudentActivity.class)));

        cardCollectFees.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class)));

        cardFeeHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class)));


    }
    // Add this method to your MainActivity
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
                Glide.with(this).load(user.getPhotoUrl()).into(ivProfileImage);
            }
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ivToolbarLogo = findViewById(R.id.ivToolbarLogo);
        tvToolbarHostelName = findViewById(R.id.tvToolbarHostelName);

        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvTotalStudentsDashboard = findViewById(R.id.tvTotalStudentsDashboard);
        tvPendingFees = findViewById(R.id.tvPendingFees);

        cardAddStudent = findViewById(R.id.cardAddStudent);
        cardCollectFees = findViewById(R.id.cardCollectFees);
        cardFeeHistory = findViewById(R.id.cardFeeHistory);
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

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
// In MainActivity navigationView listener
            if (item.getItemId() == R.id.menuExpenses) {
                startActivity(new Intent(MainActivity.this, ExpenseListActivity.class));
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
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {

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
                            .into(ivToolbarLogo);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadDashboardStats() {
        if (rootRef == null) return;

        rootRef.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalStudents = 0;
                int totalRemaining = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    StudentModel s = ds.getValue(StudentModel.class);
                    if (s != null && s.isActive()) {
                        totalStudents++;
                        totalRemaining += s.getRemainingFee();
                    }
                }

                tvTotalStudentsDashboard.setText(String.valueOf(totalStudents));
                tvPendingFees.setText("₹ " + totalRemaining);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
