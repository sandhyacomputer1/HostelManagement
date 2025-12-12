//package com.sandhyasofttech.hostelmanagement.Activities;
//
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.widget.*;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.*;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
//import com.sandhyasofttech.hostelmanagement.R;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class CollectFeesActivity extends AppCompatActivity {
//
//    Spinner spStudents, spPaymentType;
//    TextView tvTotalFee, tvPaidFee, tvRemainingFee;
//    EditText etAmount, etRemarks;
//    Button btnCollect;
//    ImageView ivQR;
//    TextView tvEmptyState;
//    StudentModel selectedStudent;
//    ArrayList<StudentModel> studentList = new ArrayList<>();
//
//    DatabaseReference rootRef;
//    String safeEmail;
//
//    static final int PICK_QR = 200;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_collect_fees);
//
//        initViews();
//        loadStudents();
//        loadQR();
//
//        spPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override public void onItemSelected(AdapterView<?> adapterView, android.view.View view, int pos, long id) {
//                String mode = spPaymentType.getSelectedItem().toString();
//                if (mode.equals("Online"))
//                    ivQR.setVisibility(android.view.View.VISIBLE);
//                else
//                    ivQR.setVisibility(android.view.View.GONE);
//            }
//            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
//        });
//
//        btnCollect.setOnClickListener(v -> collectFees());
//
//        // Long press QR -> change QR
//        ivQR.setOnLongClickListener(v -> {
//            Intent pick = new Intent(Intent.ACTION_PICK);
//            pick.setType("image/*");
//            startActivityForResult(pick, PICK_QR);
//            return true;
//        });
//    }
//
//    private void initViews() {
//        spStudents = findViewById(R.id.spStudents);
//        spPaymentType = findViewById(R.id.spPaymentType);
//        tvTotalFee = findViewById(R.id.tvTotalFee);
//        tvPaidFee = findViewById(R.id.tvPaidFee);
//        tvRemainingFee = findViewById(R.id.tvRemainingFee);
//        etAmount = findViewById(R.id.etAmount);
//        etRemarks = findViewById(R.id.etRemarks);
//        btnCollect = findViewById(R.id.btnCollectFee);
//        ivQR = findViewById(R.id.ivQR);
//        ivQR.setVisibility(android.view.View.GONE);
//        tvEmptyState = findViewById(R.id.tvEmptyState);
//
//        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        safeEmail = email.replace(".", ",");
//
//        rootRef = FirebaseDatabase.getInstance()
//                .getReference("HostelManagement")
//                .child(safeEmail);
//    }
//
//    private void loadStudents() {
//        rootRef.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//
//                studentList.clear();
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    try {
//                        StudentModel m = ds.getValue(StudentModel.class);
//                        if (m != null) {
//                            m.setId(ds.getKey());
//                            studentList.add(m);
//                        }
//                    } catch (com.google.firebase.database.DatabaseException e) {
//                        android.util.Log.e("CollectFees", "Failed to parse student: " + ds.getKey(), e);
//                        continue;  // Skip corrupted record
//                    }
//                }
//
//                // EMPTY STATE HANDLING
//                if (studentList.isEmpty()) {
//                    // show message, disable controls
//                    tvEmptyState.setVisibility(android.view.View.VISIBLE);
//                    spStudents.setEnabled(false);
//                    etAmount.setEnabled(false);
//                    spPaymentType.setEnabled(false);
//                    btnCollect.setEnabled(false);
//                    tvTotalFee.setText("Total Fee: ₹ 0");
//                    tvPaidFee.setText("Paid: ₹ 0");
//                    tvRemainingFee.setText("Remaining: ₹ 0");
//                    selectedStudent = null;
//                    return;
//                } else {
//                    tvEmptyState.setVisibility(android.view.View.GONE);
//                    spStudents.setEnabled(true);
//                    etAmount.setEnabled(true);
//                    spPaymentType.setEnabled(true);
//                    btnCollect.setEnabled(true);
//                }
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                        CollectFeesActivity.this,
//                        android.R.layout.simple_spinner_dropdown_item,
//                        getStudentNames()
//                );
//
//                spStudents.setAdapter(adapter);
//
//                spStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
//                        selectedStudent = studentList.get(pos);
//                        updateFeeDisplayUI();
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) { }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) { }
//        });
//    }
//
//    private void loadQR() {
//        rootRef.child("QRImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override public void onDataChange(DataSnapshot snapshot) {
//                String qr = snapshot.getValue(String.class);
//                if(qr != null && !qr.isEmpty()) {
//                    Glide.with(CollectFeesActivity.this).load(qr).into(ivQR);
//                }
//            }
//
//            @Override public void onCancelled(DatabaseError error) {}
//        });
//    }
//
//    private List<String> getStudentNames() {
//        List<String> names = new ArrayList<>();
//        for(StudentModel s: studentList) {
//            names.add(s.getName() + " ("+ s.getRoom() +")");
//        }
//        return names;
//    }
//
//    private void updateFeeDisplayUI() {
//        if(selectedStudent != null) {  // 🔥 SAFETY CHECK
//            tvTotalFee.setText("₹ " + selectedStudent.getAnnualFee());
//            tvPaidFee.setText("₹ " + selectedStudent.getPaidFee());
//            tvRemainingFee.setText("₹ " + selectedStudent.getRemainingFee());
//        }
//    }
//
//
//    private void collectFees() {
//
//        // 🔥 CRITICAL FIX: Null safety
//        if(selectedStudent == null) {
//            Toast.makeText(this, "Please select a student first", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if(etAmount.getText().toString().isEmpty()) {
//            etAmount.setError("Required");
//            return;
//        }
//
//        try {
//            int paidNow = Integer.parseInt(etAmount.getText().toString());
//            int oldPaid = selectedStudent.getPaidFee();
//            int oldRemain = selectedStudent.getRemainingFee();
//
//            if(paidNow <= 0) {
//                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if(paidNow > oldRemain) {
//                Toast.makeText(this, "Amount exceeds remaining fee", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            int newPaid = oldPaid + paidNow;
//            int newRemain = oldRemain - paidNow;
//
//            // UPDATE DATABASE
//            rootRef.child("Students")
//                    .child(selectedStudent.getId())
//                    .child("paidFee")
//                    .setValue(newPaid);
//
//            rootRef.child("Students")
//                    .child(selectedStudent.getId())
//                    .child("remainingFee")
//                    .setValue(newRemain);
//
//            saveFeeHistory(paidNow);
//
//            new AlertDialog.Builder(this)
//                    .setTitle("Fee Collected")
//                    .setMessage("₹" + paidNow + " collected successfully!\nRemaining: ₹" + newRemain)
//                    .setPositiveButton("OK", (d,w) -> finish())
//                    .show();
//
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Enter valid number", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void saveFeeHistory(int paidNow) {
//
//        String id = rootRef.child("FeeHistory")
//                .child(selectedStudent.getId()).push().getKey();
//
//        String method = spPaymentType.getSelectedItem().toString();
//
//        String date = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date());
//
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("paymentId", id);
//        map.put("amountPaid", paidNow);
//        map.put("method", method);
//        map.put("remarks", etRemarks.getText().toString());
//        map.put("date", date);
//
//        rootRef.child("FeeHistory")
//                .child(selectedStudent.getId())
//                .child(id)
//                .setValue(map);
//    }
//
//    @Override
//    protected void onActivityResult(int req, int res, @Nullable Intent data) {
//        super.onActivityResult(req, res, data);
//
//        if(req == PICK_QR && res == RESULT_OK && data != null) {
//            Uri uri = data.getData();
//            ivQR.setImageURI(uri);
//            uploadQRToFirebase(uri);
//        }
//    }
//
//    private void uploadQRToFirebase(Uri uri) {
//        StorageReference ref = FirebaseStorage.getInstance()
//                .getReference("Hostel_QR")
//                .child("qrImage.jpg");
//
//        ref.putFile(uri)
//                .addOnSuccessListener(task ->
//                        ref.getDownloadUrl().addOnSuccessListener(url -> {
//
//                            rootRef.child("QRImageUrl").setValue(url.toString());
//                            Toast.makeText(this, "QR Updated", Toast.LENGTH_SHORT).show();
//                        })
//                );
//    }
//}






package com.sandhyasofttech.hostelmanagement.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    // UI Components
    ImageView ivBack;
    Spinner spStudents, spPaymentType;
    TextView tvTotalFee, tvPaidFee, tvRemainingFee, tvEmptyState;
    EditText etAmount, etRemarks;
    Button btnCollect;
    ImageView ivQR;
    LinearLayout llFeeDetails;

    // Data
    StudentModel selectedStudent;
    ArrayList<StudentModel> studentList = new ArrayList<>();
    DatabaseReference rootRef;
    String safeEmail;
    ProgressDialog progressDialog;

    static final int PICK_QR = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_fees);

        initViews();
        setupProgressDialog();
        loadStudents();
        loadQR();
        setupListeners();
    }

    private void initViews() {
        // Header
        ivBack = findViewById(R.id.ivBack);

        // Spinners
        spStudents = findViewById(R.id.spStudents);
        spPaymentType = findViewById(R.id.spPaymentType);

        // Fee Display
        tvTotalFee = findViewById(R.id.tvTotalFee);
        tvPaidFee = findViewById(R.id.tvPaidFee);
        tvRemainingFee = findViewById(R.id.tvRemainingFee);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        llFeeDetails = findViewById(R.id.llFeeDetails);

        // Input Fields
        etAmount = findViewById(R.id.etAmount);
        etRemarks = findViewById(R.id.etRemarks);

        // Buttons & Images
        btnCollect = findViewById(R.id.btnCollectFee);
        ivQR = findViewById(R.id.ivQR);
        ivQR.setVisibility(android.view.View.GONE);

        // Firebase Setup
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        safeEmail = email.replace(".", ",");
        rootRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail);
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        // Back Button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Payment Type Selector
        spPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String mode = spPaymentType.getSelectedItem().toString();
                if (mode.equalsIgnoreCase("Online") || mode.equalsIgnoreCase("UPI")) {
                    ivQR.setVisibility(android.view.View.VISIBLE);
                } else {
                    ivQR.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Amount Input Validation
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && selectedStudent != null) {
                    try {
                        int amount = Integer.parseInt(s.toString());
                        int remaining = selectedStudent.getRemainingFee();

                        if (amount > remaining) {
                            etAmount.setError("Amount exceeds remaining fee");
                        } else if (amount <= 0) {
                            etAmount.setError("Enter valid amount");
                        }
                    } catch (NumberFormatException e) {
                        etAmount.setError("Invalid amount");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Collect Fee Button
        btnCollect.setOnClickListener(v -> validateAndCollectFees());

        // QR Long Press to Change
        ivQR.setOnLongClickListener(v -> {
            showQRChangeDialog();
            return true;
        });
    }

    private void loadStudents() {
        progressDialog.setMessage("Loading students...");
        progressDialog.show();

        rootRef.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();
                studentList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        StudentModel model = ds.getValue(StudentModel.class);
                        if (model != null) {
                            model.setId(ds.getKey());
                            studentList.add(model);
                        }
                    } catch (DatabaseException e) {
                        android.util.Log.e("CollectFees", "Parse error: " + ds.getKey(), e);
                    }
                }

                if (studentList.isEmpty()) {
                    showEmptyState();
                } else {
                    showStudentList();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(CollectFeesActivity.this,
                        "Failed to load students: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        llFeeDetails.setVisibility(android.view.View.GONE);
        spStudents.setEnabled(false);
        etAmount.setEnabled(false);
        etRemarks.setEnabled(false);
        spPaymentType.setEnabled(false);
        btnCollect.setEnabled(false);
        selectedStudent = null;
    }

    private void showStudentList() {
        tvEmptyState.setVisibility(android.view.View.GONE);
        llFeeDetails.setVisibility(android.view.View.VISIBLE);
        spStudents.setEnabled(true);
        etAmount.setEnabled(true);
        etRemarks.setEnabled(true);
        spPaymentType.setEnabled(true);
        btnCollect.setEnabled(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getStudentNames()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStudents.setAdapter(adapter);

        spStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedStudent = studentList.get(position);
                updateFeeDisplay();
                clearInputs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private List<String> getStudentNames() {
        List<String> names = new ArrayList<>();
        for (StudentModel student : studentList) {
            String display = student.getName() + " - Room " + student.getRoom();
            names.add(display);
        }
        return names;
    }

    private void updateFeeDisplay() {
        if (selectedStudent != null) {
            tvTotalFee.setText("₹ " + formatAmount(selectedStudent.getAnnualFee()));
            tvPaidFee.setText("₹ " + formatAmount(selectedStudent.getPaidFee()));
            tvRemainingFee.setText("₹ " + formatAmount(selectedStudent.getRemainingFee()));

            // Highlight remaining fee if unpaid
            if (selectedStudent.getRemainingFee() > 0) {
                tvRemainingFee.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvRemainingFee.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    }

    private String formatAmount(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    private void clearInputs() {
        etAmount.setText("");
        etRemarks.setText("");
        etAmount.setError(null);
    }

    private void loadQR() {
        rootRef.child("QRImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String qrUrl = snapshot.getValue(String.class);
                if (qrUrl != null && !qrUrl.isEmpty()) {
                    Glide.with(CollectFeesActivity.this)
                            .load(qrUrl)
                            .placeholder(R.drawable.default_qr)
                            .error(R.drawable.default_qr)
                            .into(ivQR);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void validateAndCollectFees() {
        // Validation checks
        if (selectedStudent == null) {
            Toast.makeText(this, "Please select a student first", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter valid number");
            etAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Amount must be greater than 0");
            etAmount.requestFocus();
            return;
        }

        int remaining = selectedStudent.getRemainingFee();
        if (amount > remaining) {
            etAmount.setError("Amount exceeds remaining fee (₹" + remaining + ")");
            etAmount.requestFocus();
            return;
        }

        // Show confirmation dialog
        showConfirmationDialog(amount);
    }

    private void showConfirmationDialog(int amount) {
        String studentName = selectedStudent.getName();
        String paymentMode = spPaymentType.getSelectedItem().toString();
        int newRemaining = selectedStudent.getRemainingFee() - amount;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Fee Collection");
        builder.setMessage(
                "Student: " + studentName + "\n" +
                        "Amount: ₹" + formatAmount(amount) + "\n" +
                        "Payment Mode: " + paymentMode + "\n" +
                        "Remaining After Payment: ₹" + formatAmount(newRemaining) + "\n\n" +
                        "Do you want to proceed?"
        );

        builder.setPositiveButton("Confirm", (dialog, which) -> processFeeCollection(amount));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void processFeeCollection(int amount) {
        progressDialog.setMessage("Processing payment...");
        progressDialog.show();

        int oldPaid = selectedStudent.getPaidFee();
        int oldRemaining = selectedStudent.getRemainingFee();
        int newPaid = oldPaid + amount;
        int newRemaining = oldRemaining - amount;

        // Update student fees in database
        Map<String, Object> updates = new HashMap<>();
        updates.put("paidFee", newPaid);
        updates.put("remainingFee", newRemaining);

        rootRef.child("Students")
                .child(selectedStudent.getId())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Save to fee history
                    saveFeeHistory(amount, () -> {
                        progressDialog.dismiss();
                        showSuccessDialog(amount, newRemaining);

                        // Update local model
                        selectedStudent.setPaidFee(newPaid);
                        selectedStudent.setRemainingFee(newRemaining);
                        updateFeeDisplay();
                        clearInputs();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update fees: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void saveFeeHistory(int amount, Runnable onSuccess) {
        String historyId = rootRef.child("FeeHistory")
                .child(selectedStudent.getId())
                .push()
                .getKey();

        if (historyId == null) {
            Toast.makeText(this, "Failed to generate payment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMode = spPaymentType.getSelectedItem().toString();
        String remarks = etRemarks.getText().toString().trim();
        String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date());

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("paymentId", historyId);
        historyData.put("studentId", selectedStudent.getId());
        historyData.put("studentName", selectedStudent.getName());
        historyData.put("room", selectedStudent.getRoom());
        historyData.put("amountPaid", amount);
        historyData.put("paymentMode", paymentMode);
        historyData.put("remarks", remarks.isEmpty() ? "N/A" : remarks);
        historyData.put("date", date);
        historyData.put("timestamp", System.currentTimeMillis());

        rootRef.child("FeeHistory")
                .child(selectedStudent.getId())
                .child(historyId)
                .setValue(historyData)
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog(int amount, int remaining) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✓ Payment Successful");
        builder.setMessage(
                "Amount Collected: ₹" + formatAmount(amount) + "\n" +
                        "Remaining Fee: ₹" + formatAmount(remaining) + "\n\n" +
                        (remaining == 0 ? "All fees have been paid!" : "")
        );
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Option to collect more or finish
            if (remaining > 0) {
                dialog.dismiss();
            } else {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showQRChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update QR Code");
        builder.setMessage("Do you want to change the payment QR code?");
        builder.setPositiveButton("Change", (dialog, which) -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, PICK_QR);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_QR && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadQRToFirebase(imageUri);
            }
        }
    }

    private void uploadQRToFirebase(Uri uri) {
        progressDialog.setMessage("Uploading QR code...");
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("Hostel_QR")
                .child(safeEmail)
                .child("qr_" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            rootRef.child("QRImageUrl").setValue(downloadUrl.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Glide.with(CollectFeesActivity.this)
                                                .load(downloadUrl)
                                                .into(ivQR);
                                        Toast.makeText(this, "QR code updated successfully",
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                )
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload QR: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}