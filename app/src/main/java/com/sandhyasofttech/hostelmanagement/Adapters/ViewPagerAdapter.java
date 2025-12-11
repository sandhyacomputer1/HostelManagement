package com.sandhyasofttech.hostelmanagement.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.sandhyasofttech.hostelmanagement.Fragment.DashboardFragment;
import com.sandhyasofttech.hostelmanagement.Fragment.SettingsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_COUNT = 2;
    public static final int TAB_DASHBOARD = 0;
    public static final int TAB_SETTINGS = 1;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_DASHBOARD:
                return new DashboardFragment();
            case TAB_SETTINGS:
                return new SettingsFragment();
            default:
                return new DashboardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
