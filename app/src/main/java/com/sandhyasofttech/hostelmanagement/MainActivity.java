package com.sandhyasofttech.hostelmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.sandhyasofttech.hostelmanagement.Activities.*;
import com.sandhyasofttech.hostelmanagement.Adapters.ViewPagerAdapter;
import com.sandhyasofttech.hostelmanagement.Registration.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    private ViewPagerAdapter viewPagerAdapter;
    private boolean isUserInitiatedScroll = true;

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
        setupViewPager();
        setupBottomNavAndFAB();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        viewPager = findViewById(R.id.viewpager);
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

    private void setupViewPager() {
        // Create and attach adapter to ViewPager2
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Set page transformer for smooth animations
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull android.view.View page, float position) {
                // Optional: Add parallax or other effects
                page.setAlpha(1 - (Math.abs(position) * 0.3f));
            }
        });

        // Listen to page changes and sync with bottom navigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isUserInitiatedScroll) {
                    // Update bottom nav without triggering ViewPager2 callback
                    isUserInitiatedScroll = false;
                    updateBottomNav(position);
                    updateToolbarTitle(position);
                    isUserInitiatedScroll = true;
                }
            }
        });
    }

    private void setupBottomNavAndFAB() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                isUserInitiatedScroll = false;
                viewPager.setCurrentItem(ViewPagerAdapter.TAB_DASHBOARD, true);
                isUserInitiatedScroll = true;
                return true;
            } else if (id == R.id.nav_settings) {
                isUserInitiatedScroll = false;
                viewPager.setCurrentItem(ViewPagerAdapter.TAB_SETTINGS, true);
                isUserInitiatedScroll = true;
                return true;
            }
            return false;
        });

        // FAB → Add Student
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddStudentActivity.class)));
    }

    private void updateBottomNav(int position) {
        switch (position) {
            case ViewPagerAdapter.TAB_DASHBOARD:
                bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
                break;
            case ViewPagerAdapter.TAB_SETTINGS:
                bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                break;
        }
    }

    private void updateToolbarTitle(int position) {
        switch (position) {
            case ViewPagerAdapter.TAB_DASHBOARD:
                getSupportActionBar().setTitle("Dashboard");
                break;
            case ViewPagerAdapter.TAB_SETTINGS:
                getSupportActionBar().setTitle("Settings");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (viewPager.getCurrentItem() != ViewPagerAdapter.TAB_DASHBOARD) {
            viewPager.setCurrentItem(ViewPagerAdapter.TAB_DASHBOARD, true);
        } else {
            super.onBackPressed();
        }
    }

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
