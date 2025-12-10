package com.sandhyasofttech.hostelmanagement.Activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.sandhyasofttech.hostelmanagement.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileLogo;
    private TextView tvHostelName, tvEmail, tvPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfileLogo = findViewById(R.id.ivProfileLogo);
        tvHostelName = findViewById(R.id.tvProfileHostelName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvPhone = findViewById(R.id.tvProfilePhone);

        loadData();
    }

    private void loadData() {

        String safeEmail = FirebaseAuth.getInstance().getCurrentUser()
                .getEmail()
                .replace(".", ",");

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("ownerInfo");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                tvHostelName.setText(snapshot.child("hostelName").getValue(String.class));
                tvEmail.setText(snapshot.child("email").getValue(String.class));
                tvPhone.setText(snapshot.child("phone").getValue(String.class));

                String logoUrl = snapshot.child("logoUrl").getValue(String.class);

                Glide.with(ProfileActivity.this)
                        .load(logoUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(ivProfileLogo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
