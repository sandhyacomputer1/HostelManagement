//package com.sandhyasofttech.hostelmanagement;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//import androidx.drawerlayout.widget.DrawerLayout;
//
//import com.bumptech.glide.Glide;
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.navigation.NavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.*;
//import com.sandhyasofttech.hostelmanagement.Activities.*;
//import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
//import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//
//    private TextView tvToolbarHostelName, tvCurrentDate;
//    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
//    private TextView tvOccupiedRooms, tvAvailableRooms, tvTotalRevenue, tvPendingStudents;
//    private ImageView ivToolbarLogo;
//    private CardView cardAddStudent, cardCollectFees, cardFeeHistory, cardManageRooms;
//
//    private DrawerLayout drawerLayout;
//    private NavigationView navigationView;
//    private MaterialToolbar toolbar;
//
//    private DatabaseReference rootRef;
//    private String safeEmail;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        initViews();
//        setupToolbarDrawer();
//        setupFirebase();
//        setCurrentDate();
//        setupClickListeners();
//        loadHostelDetails();
//        loadDashboardStats();
//        setupDrawerMenuClicks();
//    }
//
//    private void initViews() {
//        toolbar = findViewById(R.id.toolbar);
//        drawerLayout = findViewById(R.id.drawer_layout);
//        navigationView = findViewById(R.id.navigationView);
//
//        ivToolbarLogo = findViewById(R.id.ivToolbarLogo);
//        tvToolbarHostelName = findViewById(R.id.tvToolbarHostelName);
//        tvCurrentDate = findViewById(R.id.tvCurrentDate);
//
//        tvTotalRooms = findViewById(R.id.tvTotalRooms);
//        tvTotalStudentsDashboard = findViewById(R.id.tvTotalStudentsDashboard);
//        tvPendingFees = findViewById(R.id.tvPendingFees);
//        tvOccupiedRooms = findViewById(R.id.tvOccupiedRooms);
//        tvAvailableRooms = findViewById(R.id.tvAvailableRooms);
//        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
//        tvPendingStudents = findViewById(R.id.tvPendingStudents);
//
//        cardAddStudent = findViewById(R.id.cardAddStudent);
//        cardCollectFees = findViewById(R.id.cardCollectFees);
//        cardFeeHistory = findViewById(R.id.cardFeeHistory);
//        cardManageRooms = findViewById(R.id.cardManageRooms);
//    }
//
//    private void setupToolbarDrawer() {
//        setSupportActionBar(toolbar);
//
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this,
//                drawerLayout,
//                toolbar,
//                R.string.navigation_drawer_open,
//                R.string.navigation_drawer_close
//        );
//
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//    }
//
//    private void setupFirebase() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) return;
//
//        safeEmail = user.getEmail().replace(".", ",");
//        rootRef = FirebaseDatabase.getInstance()
//                .getReference("HostelManagement")
//                .child(safeEmail);
//    }
//
//    private void setCurrentDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
//        tvCurrentDate.setText(sdf.format(new Date()));
//    }
//
//    private void setupClickListeners() {
//        ivToolbarLogo.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
//
//        cardAddStudent.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, AddStudentActivity.class)));
//
//        cardCollectFees.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class)));
//
//        cardFeeHistory.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class)));
//
//        cardManageRooms.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, StudentListActivity.class)));
//    }
//
//    private void setupDrawerMenuClicks() {
//        navigationView.setNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.menuAddStudent) {
//                startActivity(new Intent(MainActivity.this, AddStudentActivity.class));
//            }
//            else if (id == R.id.menuStudents) {
//                startActivity(new Intent(MainActivity.this, StudentListActivity.class));
//            }
//            else if (id == R.id.menuCollectFees) {
//                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class));
//            }
//            else if (id == R.id.menuCollectFeesHistory) {
//                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class));
//            }
//            else if (id == R.id.menuExpenses) {
//                startActivity(new Intent(MainActivity.this, ExpenseEntryActivity.class));
//            }
//            else if (id == R.id.menuRooms) {
//                startActivity(new Intent(MainActivity.this, RoomsActivity.class));
//            }
//            else if (id == R.id.menuLogout) {
//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                finish();
//            }
//
//            drawerLayout.closeDrawers();
//            return true;
//        });
//    }
//
//    private void loadHostelDetails() {
//        rootRef.child("ownerInfo").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                String name = snapshot.child("hostelName").getValue(String.class);
//                String logo = snapshot.child("logoUrl").getValue(String.class);
//                String rooms = snapshot.child("rooms").getValue(String.class);
//
//                if (name != null) tvToolbarHostelName.setText(name);
//                if (rooms != null) tvTotalRooms.setText(rooms);
//
//                if (logo != null && !logo.isEmpty()) {
//                    Glide.with(MainActivity.this)
//                            .load(logo)
//                            .circleCrop()
//                            .into(ivToolbarLogo);
//                }
//            }
//
//            @Override public void onCancelled(@NonNull DatabaseError error) {}
//        });
//    }
//
//    private void loadDashboardStats() {
//        rootRef.child("Students").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                int total = 0, pendingAmount = 0, pendingCount = 0, revenue = 0;
//                HashSet<String> occupiedRooms = new HashSet<>();
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    StudentModel s = ds.getValue(StudentModel.class);
//                    if (s != null && s.isActive()) {
//
//                        total++;
//                        pendingAmount += s.getRemainingFee();
//
//                        if (s.getRemainingFee() > 0) pendingCount++;
//                        revenue += s.getPaidFee();
//
//                        String room = s.getRoom();
//                        if (room != null && !room.trim().isEmpty()) {
//                            occupiedRooms.add(room.trim());
//                        }
//                    }
//                }
//
//                tvTotalStudentsDashboard.setText(String.valueOf(total));
//                tvPendingFees.setText("₹ " + pendingAmount);
//                tvPendingStudents.setText(pendingCount + " students");
//                tvTotalRevenue.setText("₹ " + revenue);
//                tvOccupiedRooms.setText(String.valueOf(occupiedRooms.size()));
//
//                try {
//                    int totalR = Integer.parseInt(tvTotalRooms.getText().toString());
//                    int avail = totalR - occupiedRooms.size();
//                    tvAvailableRooms.setText(String.valueOf(Math.max(avail, 0)));
//                } catch (Exception e) {
//                    tvAvailableRooms.setText("0");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });
//    }
//}



package com.sandhyasofttech.hostelmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import androidx.cardview.widget.CardView;

import com.sandhyasofttech.hostelmanagement.Activities.*;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvToolbarHostelName, tvCurrentDate;
    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
    private TextView tvOccupiedRooms, tvAvailableRooms, tvTotalRevenue, tvPendingStudents;

    private ImageView ivToolbarLogo;
    private CardView cardAddStudent, cardCollectFees, cardFeeHistory, cardManageRooms;

    private DatabaseReference rootRef;
    private String safeEmail;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbarAndDrawer();
        setupFirebase();
        setCurrentDate();

        setupClickListeners();
        loadHostelDetails();
        loadDashboardStats();
        setupDrawerClicks();
        setupFabButton();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
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

    private void setupToolbarAndDrawer() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        safeEmail = user.getEmail().replace(".", ",");
        rootRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail);
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        tvCurrentDate.setText(sdf.format(new Date()));
    }

    private void setupClickListeners() {
        ivToolbarLogo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        cardAddStudent.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddStudentActivity.class)));

        cardCollectFees.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class)));

        cardFeeHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class)));

        cardManageRooms.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StudentListActivity.class)));
    }

    private void setupFabButton() {
        FloatingActionButton fab = findViewById(R.id.fab_quick);
        fab.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddStudentActivity.class))
        );
    }

    private void setupDrawerClicks() {
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menuAddStudent) {
                startActivity(new Intent(MainActivity.this, AddStudentActivity.class));

            } else if (id == R.id.menuStudents) {
                startActivity(new Intent(MainActivity.this, StudentListActivity.class));

            } else if (id == R.id.menuCollectFees) {
                startActivity(new Intent(MainActivity.this, CollectFeesActivity.class));

            } else if (id == R.id.menuCollectFeesHistory) {
                startActivity(new Intent(MainActivity.this, FeeHistoryActivity.class));

            } else if (id == R.id.menuExpenses) {
                startActivity(new Intent(MainActivity.this, ExpenseEntryActivity.class));

            } else if (id == R.id.menuRooms) {
                startActivity(new Intent(MainActivity.this, RoomsActivity.class));

            } else if (id == R.id.menuLogout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void loadHostelDetails() {
        rootRef.child("ownerInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("hostelName").getValue(String.class);
                String logo = snapshot.child("logoUrl").getValue(String.class);
                String rooms = snapshot.child("rooms").getValue(String.class);

                if (name != null) tvToolbarHostelName.setText(name);

                if (rooms != null && !rooms.trim().isEmpty())
                    tvTotalRooms.setText(rooms);

                if (logo != null && !logo.isEmpty()) {
                    Glide.with(MainActivity.this)
                            .load(logo)
                            .circleCrop()
                            .into(ivToolbarLogo);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadDashboardStats() {
        rootRef.child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int total = 0, pendingAmount = 0, pendingCount = 0, revenue = 0;
                HashSet<String> occupiedRooms = new HashSet<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    StudentModel s = ds.getValue(StudentModel.class);

                    if (s != null && s.isActive()) {
                        total++;

                        int remaining = s.getRemainingFee();
                        pendingAmount += remaining;

                        if (remaining > 0) pendingCount++;

                        revenue += s.getPaidFee();

                        String room = s.getRoom();
                        if (room != null && !room.trim().isEmpty()) {
                            occupiedRooms.add(room.trim());
                        }
                    }
                }

                tvTotalStudentsDashboard.setText(String.valueOf(total));
                tvPendingFees.setText("₹ " + pendingAmount);
                tvPendingStudents.setText(pendingCount + " students");
                tvTotalRevenue.setText("₹ " + revenue);
                tvOccupiedRooms.setText(String.valueOf(occupiedRooms.size()));

                updateAvailableRooms(occupiedRooms.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateAvailableRooms(int occupiedCount) {
        String totalRoomStr = tvTotalRooms.getText().toString().trim();
        int totalR = 0;

        try {
            totalR = Integer.parseInt(totalRoomStr);
        } catch (Exception ignored) {}

        int avail = totalR - occupiedCount;
        if (avail < 0) avail = 0;

        tvAvailableRooms.setText(String.valueOf(avail));
    }
}
