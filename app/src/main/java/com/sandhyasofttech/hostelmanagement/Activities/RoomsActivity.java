package com.sandhyasofttech.hostelmanagement.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Adapters.RoomAdapter;
import com.sandhyasofttech.hostelmanagement.Models.Room;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Utils.PrefManager;
import java.util.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RoomsActivity extends AppCompatActivity implements RoomAdapter.RoomListener {

    private RecyclerView rvRooms;
    private SearchView searchView;
    private FloatingActionButton fabAddRoom;

    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private DatabaseReference roomsRef;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        prefManager = new PrefManager(this);
        String email = prefManager.getUserEmail();
        if (email == null) { Toast.makeText(this, "Login first", Toast.LENGTH_SHORT).show(); finish(); return; }
        String safeEmail = email.replace(".", ",");

        roomsRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Rooms");

        rvRooms = findViewById(R.id.rvRooms);
        searchView = findViewById(R.id.searchView);
        fabAddRoom = findViewById(R.id.fabAddRoom);

        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RoomAdapter(this, roomList, this);
        rvRooms.setAdapter(adapter);

        loadRooms();
        fabAddRoom.setOnClickListener(v -> {
            // tumcha AddRoomActivity jya navacha aahe to vapra
            Intent i = new Intent(RoomsActivity.this, AddRoomActivity.class);
            startActivity(i);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { adapter.getFilter().filter(query); return false; }
            @Override public boolean onQueryTextChange(String newText) { adapter.getFilter().filter(newText); return false; }
        });
    }

    private void loadRooms() {
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Room r = ds.getValue(Room.class);
                    if (r != null) roomList.add(r);
                }
                Collections.sort(roomList, (a,b) -> a.roomNo.compareToIgnoreCase(b.roomNo));
                adapter.updateList(roomList);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomsActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEdit(Room room) {
        // Show edit dialog
        showEditDialog(room);
    }

    @Override
    public void onDelete(Room room) {
        if (room.occupied > 0) {
            Toast.makeText(this, "Cannot delete. Room has allocated students.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Room")
                .setMessage("Delete room " + room.roomNo + " ?")
                .setPositiveButton("Delete", (dialog, which) -> roomsRef.child(room.roomId).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(RoomsActivity.this, "Deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(RoomsActivity.this, "Failed", Toast.LENGTH_SHORT).show()))
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
        ArrayAdapter<String> adapterSp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"ROOM","DORMITORY"});
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
                        Toast.makeText(this, "Capacity cannot be less than occupied (" + room.occupied + ")", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String,Object> updates = new HashMap<>();
                    updates.put("roomNo", newNo);
                    updates.put("type", newType);
                    updates.put("capacity", newCap);
                    updates.put("available", newCap - room.occupied);

                    roomsRef.child(room.roomId).updateChildren(updates)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Room updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
