//package com.sandhyasofttech.hostelmanagement.Fragment;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.cardview.widget.CardView;
//import androidx.fragment.app.Fragment;
//import com.bumptech.glide.Glide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.*;
//import com.sandhyasofttech.hostelmanagement.Activities.*;
//import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
//import com.sandhyasofttech.hostelmanagement.R;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.Locale;
//
//public class DashboardFragment extends Fragment {
//
//    private TextView tvToolbarHostelName, tvCurrentDate;
//    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
//    private TextView tvOccupiedRooms, tvAvailableRooms, tvTotalRevenue, tvPendingStudents;
//    private ImageView ivToolbarLogo;
//    private CardView cardAddStudent, cardCollectFees, cardFeeHistory, cardManageRooms;
//
//    private DatabaseReference rootRef;
//    private String safeEmail;
//
//    // Listener references to remove later
//    private ValueEventListener hostelListener, statsListener;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
//
//        initViews(view);
//        setupFirebase();
//        setCurrentDate();
//        loadHostelDetails();
//        loadDashboardStats();
//        setupClickListeners();
//
//        return view;
//    }
//
//    private void initViews(View view) {
//        ivToolbarLogo = view.findViewById(R.id.ivToolbarLogo);
//        tvToolbarHostelName = view.findViewById(R.id.tvToolbarHostelName);
//        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
//
//        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
//        tvTotalStudentsDashboard = view.findViewById(R.id.tvTotalStudentsDashboard);
//        tvPendingFees = view.findViewById(R.id.tvPendingFees);
//        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
//        tvAvailableRooms = view.findViewById(R.id.tvAvailableRooms);
//        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
//        tvPendingStudents = view.findViewById(R.id.tvPendingStudents);
//
//        cardAddStudent = view.findViewById(R.id.cardAddStudent);
//        cardCollectFees = view.findViewById(R.id.cardCollectFees);
//        cardFeeHistory = view.findViewById(R.id.cardFeeHistory);
//        cardManageRooms = view.findViewById(R.id.cardManageRooms);
//    }
//
//    private void setupFirebase() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) return;
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
//        ivToolbarLogo.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
//        cardAddStudent.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddStudentActivity.class)));
//        cardCollectFees.setOnClickListener(v -> startActivity(new Intent(getActivity(), CollectFeesActivity.class)));
//        cardFeeHistory.setOnClickListener(v -> startActivity(new Intent(getActivity(), FeeHistoryActivity.class)));
//        cardManageRooms.setOnClickListener(v -> startActivity(new Intent(getActivity(), StudentListActivity.class)));
//    }
//
//    private void loadHostelDetails() {
//        if (rootRef == null) return;
//
//        hostelListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!isAdded()) return; // ← CRITICAL: Prevent crash when detached
//
//                String name = snapshot.child("hostelName").getValue(String.class);
//                String logo = snapshot.child("logoUrl").getValue(String.class);
//                String rooms = snapshot.child("rooms").getValue(String.class);
//
//                if (name != null) tvToolbarHostelName.setText(name);
//                if (rooms != null) tvTotalRooms.setText(rooms);
//
//                if (logo != null && !logo.isEmpty() && getContext() != null) {
//                    Glide.with(DashboardFragment.this)  // Safe way
//                            .load(logo)
//                            .circleCrop()
//                            .placeholder(R.drawable.ic_image_placeholder)
//                            .error(R.drawable.ic_image_placeholder)
//                            .into(ivToolbarLogo);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        };
//
//        rootRef.child("ownerInfo").addValueEventListener(hostelListener);
//    }
//
//    private void loadDashboardStats() {
//        if (rootRef == null) return;
//
//        statsListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!isAdded()) return; // ← CRITICAL: Prevent crash
//
//                int totalStudents = 0, pendingAmount = 0, pendingCount = 0, revenue = 0;
//                HashSet<String> occupiedRooms = new HashSet<>();
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    StudentModel s = ds.getValue(StudentModel.class);
//                    if (s != null && s.isActive()) {
//                        totalStudents++;
//                        int remaining = s.getRemainingFee();
//                        pendingAmount += remaining;
//                        if (remaining > 0) pendingCount++;
//                        revenue += s.getPaidFee();  // As we fixed earlier
//
//                        String room = s.getRoom();
//                        if (room != null && !room.trim().isEmpty()) {
//                            occupiedRooms.add(room.trim());
//                        }
//                    }
//                }
//
//                tvTotalStudentsDashboard.setText(String.valueOf(totalStudents));
//                tvPendingFees.setText("₹ " + pendingAmount);
//                tvPendingStudents.setText(pendingCount + " students");
//                tvTotalRevenue.setText("₹ " + revenue);
//                tvOccupiedRooms.setText(String.valueOf(occupiedRooms.size()));
//
//                try {
//                    int totalR = Integer.parseInt(tvTotalRooms.getText().toString());
//                    int avail = totalR - occupiedRooms.size();
//                    tvAvailableRooms.setText(String.valueOf(Math.max(0, avail)));
//                } catch (Exception e) {
//                    tvAvailableRooms.setText("0");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        };
//
//        rootRef.child("Students").addValueEventListener(statsListener);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Remove listeners to prevent memory leaks and crashes
//        if (rootRef != null) {
//            if (hostelListener != null) {
//                rootRef.child("ownerInfo").removeEventListener(hostelListener);
//            }
//            if (statsListener != null) {
//                rootRef.child("Students").removeEventListener(statsListener);
//            }
//        }
//    }
//}

package com.sandhyasofttech.hostelmanagement.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Activities.*;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvToolbarHostelName, tvCurrentDate;
    private TextView tvTotalRooms, tvTotalStudentsDashboard, tvPendingFees;
    private TextView tvOccupiedRooms, tvAvailableRooms, tvTotalRevenue, tvPendingStudents;
    private ImageView ivToolbarLogo;
    private CardView cardAddStudent, cardCollectFees, cardFeeHistory, cardManageRooms;

    private DatabaseReference rootRef;
    private String safeEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        setupFirebase();
        setCurrentDate();
        setupClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadHostelDetails();  // Safe here: View is ready, fragment is attached
    }

    @Override
    public void onStart() {
        super.onStart();
        loadDashboardStats();  // Safe: Listener added when fragment is visible
    }

    private void initViews(View view) {
        ivToolbarLogo = view.findViewById(R.id.ivToolbarLogo);
        tvToolbarHostelName = view.findViewById(R.id.tvToolbarHostelName);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);

        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvTotalStudentsDashboard = view.findViewById(R.id.tvTotalStudentsDashboard);
        tvPendingFees = view.findViewById(R.id.tvPendingFees);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvAvailableRooms = view.findViewById(R.id.tvAvailableRooms);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvPendingStudents = view.findViewById(R.id.tvPendingStudents);

        cardAddStudent = view.findViewById(R.id.cardAddStudent);
        cardCollectFees = view.findViewById(R.id.cardCollectFees);
        cardFeeHistory = view.findViewById(R.id.cardFeeHistory);
        cardManageRooms = view.findViewById(R.id.cardManageRooms);
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
        ivToolbarLogo.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
        cardAddStudent.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddStudentActivity.class)));
        cardCollectFees.setOnClickListener(v -> startActivity(new Intent(getActivity(), CollectFeesActivity.class)));
        cardFeeHistory.setOnClickListener(v -> startActivity(new Intent(getActivity(), FeeHistoryActivity.class)));
        cardManageRooms.setOnClickListener(v -> startActivity(new Intent(getActivity(), StudentListActivity.class)));
    }

    private void loadHostelDetails() {
        if (!isAdded()) return;  // Extra safety
        rootRef.child("ownerInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;  // Skip if detached
                String name = snapshot.child("hostelName").getValue(String.class);
                String logo = snapshot.child("logoUrl").getValue(String.class);
                String rooms = snapshot.child("rooms").getValue(String.class);

                if (name != null) tvToolbarHostelName.setText(name);
                if (rooms != null) tvTotalRooms.setText(rooms);
                if (logo != null && !logo.isEmpty()) {
                    Glide.with(requireContext()).load(logo).circleCrop().into(ivToolbarLogo);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadDashboardStats() {
        rootRef.child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;  // FIX: Skip if fragment detached → No crash!

                int totalStudents = 0, pendingAmount = 0, pendingCount = 0, revenue = 0;
                HashSet<String> occupiedRooms = new HashSet<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    StudentModel s = ds.getValue(StudentModel.class);
                    if (s != null && s.isActive()) {
                        totalStudents++;
                        int rem = s.getRemainingFee();
                        pendingAmount += rem;
                        if (rem > 0) pendingCount++;
                        revenue += s.getPaidFee();  // Using paidFee as discussed
                        String roomNo = s.getRoom();
                        if (roomNo != null && !roomNo.trim().isEmpty()) {
                            occupiedRooms.add(roomNo.trim());
                        }
                    }
                }

                tvTotalStudentsDashboard.setText(String.valueOf(totalStudents));
                tvPendingFees.setText("₹ " + pendingAmount);
                tvPendingStudents.setText(pendingCount + " students");
                tvTotalRevenue.setText("₹ " + revenue);
                tvOccupiedRooms.setText(String.valueOf(occupiedRooms.size()));

                try {
                    int totalR = Integer.parseInt(tvTotalRooms.getText().toString());
                    int avail = totalR - occupiedRooms.size();
                    tvAvailableRooms.setText(String.valueOf(Math.max(0, avail)));
                } catch (Exception e) {
                    tvAvailableRooms.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}