package com.sandhyasofttech.hostelmanagement.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.hostelmanagement.Adapters.RoomAdapter;
import com.sandhyasofttech.hostelmanagement.Models.Room;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Utils.PrefManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomsActivity extends AppCompatActivity implements RoomAdapter.RoomListener {

    private RecyclerView rvRooms;
    private SearchView searchView;
    private FloatingActionButton fabAddRoom;
    private MaterialToolbar toolbar;

    private RoomAdapter adapter;
    private final List<Room> roomList = new ArrayList<>();
    private DatabaseReference roomsRef;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        initToolbar();
        initViews();
        initFirebase();
        setupRecycler();
        setupSearch();
        setupFab();

        loadRooms();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Rooms");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_backk);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        rvRooms = findViewById(R.id.rvRooms);
        searchView = findViewById(R.id.searchView);
        fabAddRoom = findViewById(R.id.fabAddRoom);
    }

    private void initFirebase() {
        prefManager = new PrefManager(this);
        String email = prefManager.getUserEmail();
        if (email == null) {
            Toast.makeText(this, "Login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String safeEmail = email.replace(".", ",");

        roomsRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Rooms");
    }

    private void setupRecycler() {
        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RoomAdapter(this, roomList, this);
        rvRooms.setAdapter(adapter);
        rvRooms.setHasFixedSize(true);
    }

    private void setupSearch() {
        searchView.setQueryHint("Search by room no or type");
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setupFab() {
        fabAddRoom.setOnClickListener(v -> {
            Intent i = new Intent(RoomsActivity.this, AddRoomActivity.class);
            startActivity(i);
        });
    }

    private void loadRooms() {
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Room r = ds.getValue(Room.class);
                    if (r != null) roomList.add(r);
                }
                Collections.sort(roomList, (a, b) -> a.roomNo.compareToIgnoreCase(b.roomNo));
                adapter.updateList(roomList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomsActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEdit(Room room) {
        showEditDialog(room);
    }

    @Override
    public void onDelete(Room room) {
        if (room.occupied > 0) {
            Toast.makeText(this,
                    "Cannot delete. Room has allocated students.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Room")
                .setMessage("Delete room " + room.roomNo + " ?")
                .setPositiveButton("Delete", (dialog, which) ->
                        roomsRef.child(room.roomId).removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(RoomsActivity.this,
                                                "Deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(RoomsActivity.this,
                                                "Failed", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Room room) {
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_room, null);
        EditText etRoomNo = v.findViewById(R.id.etEditRoomNo);
        EditText etCapacity = v.findViewById(R.id.etEditCapacity);
        Spinner spType = v.findViewById(R.id.spEditType);

        etRoomNo.setText(room.roomNo);
        etCapacity.setText(String.valueOf(room.capacity));

        ArrayAdapter<String> adapterSp = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{"ROOM", "DORMITORY"}
        );
        spType.setAdapter(adapterSp);
        if ("DORMITORY".equals(room.type)) spType.setSelection(1);

        new AlertDialog.Builder(this)
                .setTitle("Edit Room")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newNo = etRoomNo.getText().toString().trim();
                    int newCap = Integer.parseInt(etCapacity.getText().toString().trim());
                    String newType = spType.getSelectedItem().toString();

                    if (newCap < room.occupied) {
                        Toast.makeText(this,
                                "Capacity cannot be less than occupied (" + room.occupied + ")",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("roomNo", newNo);
                    updates.put("type", newType);
                    updates.put("capacity", newCap);
                    updates.put("available", newCap - room.occupied);

                    roomsRef.child(room.roomId).updateChildren(updates)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this,
                                            "Room updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Update failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // in case you use default home button handling
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
