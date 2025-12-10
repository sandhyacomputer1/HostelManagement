package com.sandhyasofttech.hostelmanagement.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class CollectFeesActivity extends AppCompatActivity {

    Spinner spStudents, spPaymentType;
    TextView tvTotalFee, tvPaidFee, tvRemainingFee;
    EditText etAmount, etRemarks;
    Button btnCollect;
    ImageView ivQR;
    TextView tvEmptyState;
    StudentModel selectedStudent;
    ArrayList<StudentModel> studentList = new ArrayList<>();

    DatabaseReference rootRef;
    String safeEmail;

    static final int PICK_QR = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_fees);

        initViews();
        loadStudents();
        loadQR();

        spPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, android.view.View view, int pos, long id) {
                String mode = spPaymentType.getSelectedItem().toString();
                if (mode.equals("Online"))
                    ivQR.setVisibility(android.view.View.VISIBLE);
                else
                    ivQR.setVisibility(android.view.View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        btnCollect.setOnClickListener(v -> collectFees());

        // Long press QR -> change QR
        ivQR.setOnLongClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setType("image/*");
            startActivityForResult(pick, PICK_QR);
            return true;
        });
    }

    private void initViews() {
        spStudents = findViewById(R.id.spStudents);
        spPaymentType = findViewById(R.id.spPaymentType);
        tvTotalFee = findViewById(R.id.tvTotalFee);
        tvPaidFee = findViewById(R.id.tvPaidFee);
        tvRemainingFee = findViewById(R.id.tvRemainingFee);
        etAmount = findViewById(R.id.etAmount);
        etRemarks = findViewById(R.id.etRemarks);
        btnCollect = findViewById(R.id.btnCollectFee);
        ivQR = findViewById(R.id.ivQR);
        ivQR.setVisibility(android.view.View.GONE);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        safeEmail = email.replace(".", ",");

        rootRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail);
    }

    private void loadStudents() {
        rootRef.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                studentList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        StudentModel m = ds.getValue(StudentModel.class);
                        if (m != null) {
                            m.setId(ds.getKey());
                            studentList.add(m);
                        }
                    } catch (com.google.firebase.database.DatabaseException e) {
                        android.util.Log.e("CollectFees", "Failed to parse student: " + ds.getKey(), e);
                        continue;  // Skip corrupted record
                    }
                }

                // EMPTY STATE HANDLING
                if (studentList.isEmpty()) {
                    // show message, disable controls
                    tvEmptyState.setVisibility(android.view.View.VISIBLE);
                    spStudents.setEnabled(false);
                    etAmount.setEnabled(false);
                    spPaymentType.setEnabled(false);
                    btnCollect.setEnabled(false);
                    tvTotalFee.setText("Total Fee: ₹ 0");
                    tvPaidFee.setText("Paid: ₹ 0");
                    tvRemainingFee.setText("Remaining: ₹ 0");
                    selectedStudent = null;
                    return;
                } else {
                    tvEmptyState.setVisibility(android.view.View.GONE);
                    spStudents.setEnabled(true);
                    etAmount.setEnabled(true);
                    spPaymentType.setEnabled(true);
                    btnCollect.setEnabled(true);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CollectFeesActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getStudentNames()
                );

                spStudents.setAdapter(adapter);

                spStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                        selectedStudent = studentList.get(pos);
                        updateFeeDisplayUI();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void loadQR() {
        rootRef.child("QRImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                String qr = snapshot.getValue(String.class);
                if(qr != null && !qr.isEmpty()) {
                    Glide.with(CollectFeesActivity.this).load(qr).into(ivQR);
                }
            }

            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private List<String> getStudentNames() {
        List<String> names = new ArrayList<>();
        for(StudentModel s: studentList) {
            names.add(s.getName() + " ("+ s.getRoom() +")");
        }
        return names;
    }

    private void updateFeeDisplayUI() {
        if(selectedStudent != null) {  // 🔥 SAFETY CHECK
            tvTotalFee.setText("₹ " + selectedStudent.getAnnualFee());
            tvPaidFee.setText("₹ " + selectedStudent.getPaidFee());
            tvRemainingFee.setText("₹ " + selectedStudent.getRemainingFee());
        }
    }


    private void collectFees() {

        // 🔥 CRITICAL FIX: Null safety
        if(selectedStudent == null) {
            Toast.makeText(this, "Please select a student first", Toast.LENGTH_SHORT).show();
            return;
        }

        if(etAmount.getText().toString().isEmpty()) {
            etAmount.setError("Required");
            return;
        }

        try {
            int paidNow = Integer.parseInt(etAmount.getText().toString());
            int oldPaid = selectedStudent.getPaidFee();
            int oldRemain = selectedStudent.getRemainingFee();

            if(paidNow <= 0) {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if(paidNow > oldRemain) {
                Toast.makeText(this, "Amount exceeds remaining fee", Toast.LENGTH_SHORT).show();
                return;
            }

            int newPaid = oldPaid + paidNow;
            int newRemain = oldRemain - paidNow;

            // UPDATE DATABASE
            rootRef.child("Students")
                    .child(selectedStudent.getId())
                    .child("paidFee")
                    .setValue(newPaid);

            rootRef.child("Students")
                    .child(selectedStudent.getId())
                    .child("remainingFee")
                    .setValue(newRemain);

            saveFeeHistory(paidNow);

            new AlertDialog.Builder(this)
                    .setTitle("Fee Collected")
                    .setMessage("₹" + paidNow + " collected successfully!\nRemaining: ₹" + newRemain)
                    .setPositiveButton("OK", (d,w) -> finish())
                    .show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFeeHistory(int paidNow) {

        String id = rootRef.child("FeeHistory")
                .child(selectedStudent.getId()).push().getKey();

        String method = spPaymentType.getSelectedItem().toString();

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date());

        HashMap<String,Object> map = new HashMap<>();
        map.put("paymentId", id);
        map.put("amountPaid", paidNow);
        map.put("method", method);
        map.put("remarks", etRemarks.getText().toString());
        map.put("date", date);

        rootRef.child("FeeHistory")
                .child(selectedStudent.getId())
                .child(id)
                .setValue(map);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if(req == PICK_QR && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ivQR.setImageURI(uri);
            uploadQRToFirebase(uri);
        }
    }

    private void uploadQRToFirebase(Uri uri) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("Hostel_QR")
                .child("qrImage.jpg");

        ref.putFile(uri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(url -> {

                            rootRef.child("QRImageUrl").setValue(url.toString());
                            Toast.makeText(this, "QR Updated", Toast.LENGTH_SHORT).show();
                        })
                );
    }
}
