package com.sandhyasofttech.hostelmanagement.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Utils.PrefManager;
import com.sandhyasofttech.hostelmanagement.Models.Room;

public class AddRoomActivity extends AppCompatActivity {

    private EditText etRoomNo, etCapacity;
    private Spinner spType;
    private Button btnCreate;
    private ProgressBar progressBar;
    private DatabaseReference roomsRef;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        prefManager = new PrefManager(this);
        String email = prefManager.getUserEmail();
        if (email == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String safeEmail = email.replace(".", ",");

        roomsRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Rooms");

        etRoomNo = findViewById(R.id.etRoomNo);
        etCapacity = findViewById(R.id.etCapacity);
        spType = findViewById(R.id.spRoomType);
        btnCreate = findViewById(R.id.btnCreateRoom);
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"ROOM","DORMITORY"});
        spType.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> createRoom());
    }

    private void createRoom() {
        String roomNo = etRoomNo.getText().toString().trim();
        String capStr = etCapacity.getText().toString().trim();
        String type = spType.getSelectedItem().toString();

        if (TextUtils.isEmpty(roomNo)) { etRoomNo.setError("Required"); return; }
        if (TextUtils.isEmpty(capStr)) { etCapacity.setError("Required"); return; }

        int capacity;
        try { capacity = Integer.parseInt(capStr); }
        catch (Exception e) { etCapacity.setError("Invalid"); return; }

        // Duplicate check by roomNo (case-insensitive)
        progressBar.setVisibility(View.VISIBLE);
        roomsRef.orderByChild("roomNo")
                .equalTo(roomNo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddRoomActivity.this, "Room already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            String roomId = roomsRef.push().getKey();
                            long ts = System.currentTimeMillis();
                            Room room = new Room(roomId, roomNo, type, capacity, 0, capacity, ts);
                            roomsRef.child(roomId).setValue(room)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(AddRoomActivity.this, "Room created", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(AddRoomActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddRoomActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
