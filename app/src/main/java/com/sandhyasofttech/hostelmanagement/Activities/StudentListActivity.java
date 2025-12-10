package com.sandhyasofttech.hostelmanagement.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Adapters.StudentPagerAdapter;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.util.ArrayList;

public class StudentListActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    TabLayout tabLayout;
    ArrayList<StudentModel> activeList = new ArrayList<>();
    ArrayList<StudentModel> leaveList = new ArrayList<>();

    TextView tvCount, tvHeaderTitle;
    StudentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        viewPager = findViewById(R.id.viewPagerStudents);
        tabLayout = findViewById(R.id.tabLayoutStudents);

        tvCount = findViewById(R.id.tvCount);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        fetchStudents();
    }

    private void fetchStudents() {

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String safeEmail = email.replace(".", ",");

        FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Students")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {

                        activeList.clear();
                        leaveList.clear();

                        for (DataSnapshot snap : ds.getChildren()) {
                            try {
                                StudentModel model = snap.getValue(StudentModel.class);

                                if (model != null) {
                                    model.setId(snap.getKey());

                                    if(model.isActive())
                                        activeList.add(model);
                                    else
                                        leaveList.add(model);
                                }
                            } catch (com.google.firebase.database.DatabaseException e) {
                                Log.e("StudentList", "Failed to parse student: " + snap.getKey(), e);
                                // Skip this corrupted record
                                continue;
                            }
                        }

                        updatePager();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("StudentList", "Firebase cancelled", error.toException());
                    }
                });
    }

    private void updatePager() {

        pagerAdapter = new StudentPagerAdapter(this, activeList, leaveList);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if(position == 0)
                tab.setText("Active (" + activeList.size() + ")");
            else
                tab.setText("Leave (" + leaveList.size() + ")");
        }).attach();
    }
}
