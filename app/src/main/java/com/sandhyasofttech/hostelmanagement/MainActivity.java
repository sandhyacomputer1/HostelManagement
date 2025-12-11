package com.sandhyasofttech.hostelmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.sandhyasofttech.hostelmanagement.Activities.*;
import com.sandhyasofttech.hostelmanagement.Fragment.DashboardFragment;
import com.sandhyasofttech.hostelmanagement.Fragment.SettingsFragment;
import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupToolbarAndDrawer();
        setupBottomNavAndFAB();

        // Load default fragment
        replaceFragment(new DashboardFragment());
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);                    // Fixed: Correct ID
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
    }

    private void setupToolbarAndDrawer() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Hamburger Menu (Drawer Toggle)
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation Drawer Menu Click Listener
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menuAddStudent) {
                startActivity(new Intent(this, AddStudentActivity.class));
            } else if (id == R.id.menuStudents) {
                startActivity(new Intent(this, StudentListActivity.class));
            } else if (id == R.id.menuCollectFees) {
                startActivity(new Intent(this, CollectFeesActivity.class));
            } else if (id == R.id.menuCollectFeesHistory) {
                startActivity(new Intent(this, FeeHistoryActivity.class));
            } else if (id == R.id.menuExpenses) {
                startActivity(new Intent(this, ExpenseListActivity.class));
            } else if (id == R.id.menuRooms) {
                startActivity(new Intent(this, RoomsActivity.class));
            } else if (id == R.id.menuLogout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupBottomNavAndFAB() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                replaceFragment(new DashboardFragment());
                getSupportActionBar().setTitle("Dashboard");
            } else if (id == R.id.nav_settings) {
                replaceFragment(new SettingsFragment());
                getSupportActionBar().setTitle("Settings");
            }
            return true;
        });

        // FAB → Add Student
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddStudentActivity.class)));
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setReorderingAllowed(true)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (bottomNavigationView.getSelectedItemId() != R.id.nav_dashboard) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        } else {
            super.onBackPressed();
        }
    }

    // Optional: Handle drawer open/close with hamburger icon animation
    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }
}