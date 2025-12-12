package com.sandhyasofttech.hostelmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.sandhyasofttech.hostelmanagement.Activities.*;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // UI Components
    private TextView tvToolbarHostelName, tvCurrentDate;
    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
    private TextView tvOccupiedRooms, tvAvailableRooms, tvTotalRevenue, tvPendingStudents;
    private ImageView ivToolbarLogo;
    private CardView cardAddStudent, cardCollectFees, cardFeeHistory, cardManageRooms;
    private FloatingActionButton fabQuick;

    // Navigation Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ActionBarDrawerToggle toggle;

    // Firebase
    private DatabaseReference rootRef;
    private String safeEmail;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }

        // Initialize all views
        initViews();

        // Setup toolbar and drawer
        setupToolbarAndDrawer();

        // Setup Firebase
        setupFirebase();

        // Set current date
        setCurrentDate();

        // Setup click listeners
        setupClickListeners();

        // Load data
        loadHostelDetails();
        loadDashboardStats();
    }

    private void initViews() {
        // Toolbar and Drawer
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

        // Welcome Card Views
        ivToolbarLogo = findViewById(R.id.ivToolbarLogo);
        tvToolbarHostelName = findViewById(R.id.tvToolbarHostelName);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);

        // Stats Views
        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvTotalStudentsDashboard = findViewById(R.id.tvTotalStudentsDashboard);
        tvPendingFees = findViewById(R.id.tvPendingFees);
        tvOccupiedRooms = findViewById(R.id.tvOccupiedRooms);
        tvAvailableRooms = findViewById(R.id.tvAvailableRooms);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvPendingStudents = findViewById(R.id.tvPendingStudents);

        // Action Cards
        cardAddStudent = findViewById(R.id.cardAddStudent);
        cardCollectFees = findViewById(R.id.cardCollectFees);
        cardFeeHistory = findViewById(R.id.cardFeeHistory);
        cardManageRooms = findViewById(R.id.cardManageRooms);

        // FAB
        fabQuick = findViewById(R.id.fab_quick);
    }

    private void setupToolbarAndDrawer() {
        // Set toolbar as action bar
        setSupportActionBar(toolbar);

        // Create drawer toggle
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set navigation item listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupFirebase() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        String email = user.getEmail();
        if (email != null) {
            safeEmail = email.replace(".", ",");
            rootRef = FirebaseDatabase.getInstance()
                    .getReference("HostelManagement")
                    .child(safeEmail);
        } else {
            Toast.makeText(this, "Error: User email not found", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void setCurrentDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String currentDate = sdf.format(new Date());
            tvCurrentDate.setText(currentDate);
        } catch (Exception e) {
            tvCurrentDate.setText("Today");
        }
    }

    private void setupClickListeners() {
        // Logo click - navigate to profile
        ivToolbarLogo.setOnClickListener(v ->
                navigateToActivity(ProfileActivity.class));

        // Quick action cards
        cardAddStudent.setOnClickListener(v ->
                navigateToActivity(AddStudentActivity.class));

        cardCollectFees.setOnClickListener(v ->
                navigateToActivity(CollectFeesActivity.class));

        cardFeeHistory.setOnClickListener(v ->
                navigateToActivity(FeeHistoryActivity.class));

        cardManageRooms.setOnClickListener(v ->
                navigateToActivity(StudentListActivity.class));

        // FAB click
        fabQuick.setOnClickListener(v ->
                navigateToActivity(AddStudentActivity.class));
    }

    private void loadHostelDetails() {
        if (rootRef == null) return;

        // Load owner info (hostel name, logo)
        rootRef.child("ownerInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    // Load hostel name
                    String hostelName = snapshot.child("hostelName").getValue(String.class);
                    if (hostelName != null && !hostelName.trim().isEmpty()) {
                        tvToolbarHostelName.setText(hostelName);
                    } else {
                        tvToolbarHostelName.setText("Your Hostel");
                    }

                    // Load logo
                    String logoUrl = snapshot.child("logoUrl").getValue(String.class);
                    if (logoUrl != null && !logoUrl.isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(logoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(ivToolbarLogo);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Error loading hostel details",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to load hostel details: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardStats() {
        if (rootRef == null) return;

        DatabaseReference roomsRef = rootRef.child("Rooms");
        DatabaseReference studentsRef = rootRef.child("Students");

        // Use single listener on a combined reference for real-time updates
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot roomsSnapshot = snapshot.child("Rooms");
                DataSnapshot studentsSnapshot = snapshot.child("Students");

                calculateDashboardStats(roomsSnapshot, studentsSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to load dashboard data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateDashboardStats(DataSnapshot roomsSnapshot, DataSnapshot studentsSnapshot) {
        int totalStudents = 0;
        int totalPendingAmount = 0;
        int pendingStudentCount = 0;
        int totalRevenue = 0;
        HashSet<String> occupiedRooms = new HashSet<>();

        // Count active students and occupied rooms
        for (DataSnapshot studentSnapshot : studentsSnapshot.getChildren()) {
            StudentModel student = studentSnapshot.getValue(StudentModel.class);
            if (student != null && student.isActive()) {
                totalStudents++;

                // Calculate pending fees
                int remainingFee = student.getRemainingFee();
                if (remainingFee > 0) {
                    totalPendingAmount += remainingFee;
                    pendingStudentCount++;
                }

                // Calculate total revenue
                int paidFee = student.getPaidFee();
                totalRevenue += paidFee;

                // Track occupied rooms (unique room numbers)
                String roomNumber = student.getRoom();
                if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                    occupiedRooms.add(roomNumber.trim());
                }
            }
        }

        // Get total rooms from Rooms snapshot
        int totalRoomsCount = (int) roomsSnapshot.getChildrenCount();
        int occupiedRoomsCount = occupiedRooms.size();
        int availableRoomsCount = Math.max(0, totalRoomsCount - occupiedRoomsCount);

        // FINAL VARIABLES for lambda - copy calculated values
        final int finalTotalStudents = totalStudents;
        final int finalPendingAmount = totalPendingAmount;
        final int finalPendingCount = pendingStudentCount;
        final int finalRevenue = totalRevenue;
        final int finalOccupiedCount = occupiedRoomsCount;
        final int finalAvailableCount = availableRoomsCount;
        final int finalTotalRoomsCount = totalRoomsCount;

        // Update UI on main thread
        runOnUiThread(() -> updateDashboardUI(
                finalTotalStudents,
                finalPendingAmount,
                finalPendingCount,
                finalRevenue,
                finalOccupiedCount,
                finalAvailableCount,
                finalTotalRoomsCount
        ));
    }


    private void updateDashboardUI(int totalStudents, int pendingAmount, int pendingCount,
                                   int revenue, int occupiedCount, int availableCount, int totalRoomsCount) {
        // Update all dashboard stats
        tvTotalRooms.setText(String.valueOf(totalRoomsCount));
        tvTotalStudentsDashboard.setText(String.valueOf(totalStudents));
        tvPendingFees.setText("₹ " + formatNumber(pendingAmount));
        tvPendingStudents.setText(pendingCount + " students pending");
        tvTotalRevenue.setText("₹ " + formatNumber(revenue));
        tvOccupiedRooms.setText(String.valueOf(occupiedCount));
        tvAvailableRooms.setText(String.valueOf(availableCount));
    }

    private String formatNumber(int number) {
        // Format numbers with commas for better readability
        return String.format(Locale.getDefault(), "%,d", number);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Handle navigation menu item clicks
        if (itemId == R.id.menuDashboard) {
            Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show();

        } else if (itemId == R.id.menuAddStudent) {
            navigateToActivity(AddStudentActivity.class);

        } else if (itemId == R.id.menuStudents) {
            navigateToActivity(StudentListActivity.class);

        } else if (itemId == R.id.menuRooms) {
            navigateToActivity(RoomsActivity.class);

        } else if (itemId == R.id.menuCollectFees) {
            navigateToActivity(CollectFeesActivity.class);

        } else if (itemId == R.id.menuCollectFeesHistory) {
            navigateToActivity(FeeHistoryActivity.class);

        } else if (itemId == R.id.menuExpenses) {
            navigateToActivity(ExpenseListActivity.class);

        } else if (itemId == R.id.menuReports) {
            Toast.makeText(this, "Reports - Coming Soon", Toast.LENGTH_SHORT).show();

        } else if (itemId == R.id.menuAnalytics) {
            Toast.makeText(this, "Analytics - Coming Soon", Toast.LENGTH_SHORT).show();

        } else if (itemId == R.id.menuSettings) {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();

        } else if (itemId == R.id.menuProfile) {
            navigateToActivity(ProfileActivity.class);

        } else if (itemId == R.id.menuAbout) {
            Toast.makeText(this, "About - Coming Soon", Toast.LENGTH_SHORT).show();

        } else if (itemId == R.id.menuLogout) {
            logoutUser();
        }

        // Close drawer after item selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToActivity(Class<?> activityClass) {
        try {
            Intent intent = new Intent(MainActivity.this, activityClass);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to open " + activityClass.getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        try {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        } catch (Exception e) {
            Toast.makeText(this, "Error logging out", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Close drawer if open, otherwise exit
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        if (auth.getCurrentUser() != null && rootRef != null) {
            loadHostelDetails();
            loadDashboardStats();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners if needed
    }
}
