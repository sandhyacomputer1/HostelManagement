package com.sandhyasofttech.hostelmanagement.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.util.Calendar;

public class UpdateStudentActivity extends AppCompatActivity {

    private EditText etName, etPhone, etRoom, etAddress, etFees, etDate;
    private EditText etParentName, etParentPhone;
    private Spinner spClass;
    private ImageView ivPhoto, ivCall;
    private Button btnUpdate;

    private Uri selectedImageUri;
    private String studentId = "";
    private String safeEmail = "";
    private String existingImageUrl = "";

    private StorageReference storageRef;
    private DatabaseReference studentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_student);

        studentId = getIntent().getStringExtra("id");

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Invalid Student", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClasses();

        safeEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        studentRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Students")
                .child(studentId);

        storageRef = FirebaseStorage.getInstance().getReference("StudentPhotos");

        loadStudentDetails();

        ivPhoto.setOnClickListener(v -> pickImage());
        btnUpdate.setOnClickListener(v -> saveUpdatedDetails());
        etDate.setOnClickListener(v -> openDatePicker());

        handleOneTapCall();
    }

    private void initViews() {
        ivPhoto = findViewById(R.id.ivUpdateStudentPhoto);
        ivCall = findViewById(R.id.ivCall);

        etName = findViewById(R.id.etUpdateName);
        etPhone = findViewById(R.id.etUpdatePhone);
        etRoom = findViewById(R.id.etUpdateRoom);
        etAddress = findViewById(R.id.etUpdateAddress);
        etFees = findViewById(R.id.etUpdateFees);
        etDate = findViewById(R.id.etUpdateJoiningDate);

        etParentName = findViewById(R.id.etUpdateParentName);
        etParentPhone = findViewById(R.id.etUpdateParentPhone);
        spClass = findViewById(R.id.spUpdateClass);

        btnUpdate = findViewById(R.id.btnUpdateStudent);
    }

    private void setupClasses() {
        String[] classes = {"8th","9th","10th","11th","12th","Diploma","ITI","College First Year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, classes);
        spClass.setAdapter(adapter);
    }

    private void loadStudentDetails() {
        studentRef.get().addOnSuccessListener(snapshot -> {
            StudentModel model = snapshot.getValue(StudentModel.class);

            if (model != null) {
                etName.setText(model.getName());
                etPhone.setText(model.getPhone());
                etRoom.setText(model.getRoom());
                etAddress.setText(model.getAddress());
                etFees.setText(String.valueOf(model.getAnnualFee()));
                etDate.setText(model.getJoiningDate());

                etParentName.setText(model.getParentName());
                etParentPhone.setText(model.getParentPhone());

                // Set spinner selection to current class
                String currentClass = model.getStudentClass();
                if (currentClass != null) {
                    for (int i = 0; i < spClass.getCount(); i++) {
                        if (currentClass.equals(spClass.getItemAtPosition(i))) {
                            spClass.setSelection(i);
                            break;
                        }
                    }
                }

                existingImageUrl = model.getPhotoUrl();

                Glide.with(this)
                        .load(existingImageUrl)
                        .placeholder(R.drawable.ic_user)
                        .into(ivPhoto);
            }
        });
    }

    private void saveUpdatedDetails() {

        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Enter Name");
            return;
        }

        btnUpdate.setText("Saving...");
        btnUpdate.setEnabled(false);
        btnUpdate.setAlpha(.6f);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Saving...");
        dialog.show();

        studentRef.child("name").setValue(etName.getText().toString());
        studentRef.child("phone").setValue(etPhone.getText().toString());
        studentRef.child("room").setValue(etRoom.getText().toString());
        studentRef.child("address").setValue(etAddress.getText().toString());

        int newFee = Integer.parseInt(etFees.getText().toString());
        studentRef.child("annualFee").setValue(newFee);
        studentRef.child("joiningDate").setValue(etDate.getText().toString());

        studentRef.child("parentName").setValue(etParentName.getText().toString());
        studentRef.child("parentPhone").setValue(etParentPhone.getText().toString());
        studentRef.child("studentClass").setValue(spClass.getSelectedItem().toString());

        dialog.dismiss();

        Snackbar.make(findViewById(android.R.id.content),
                "Student Updated Successfully",
                Snackbar.LENGTH_LONG).show();

        btnUpdate.setText("Update Student");
        btnUpdate.setEnabled(true);
        btnUpdate.setAlpha(1f);

        finish();
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, 5001);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == 5001 && res == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            animateImageUpdate();
            deleteOldAndUploadNew();
        }
    }

    private void animateImageUpdate() {
        ivPhoto.setAlpha(.4f);

        ScaleAnimation anim = new ScaleAnimation(
                0.85f, 1f, 0.85f, 1f,
                Animation.RELATIVE_TO_SELF, .5f,
                Animation.RELATIVE_TO_SELF, .5f
        );
        anim.setDuration(300);

        ivPhoto.startAnimation(anim);
    }

    private void deleteOldAndUploadNew() {
        if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl)
                    .delete()
                    .addOnSuccessListener(unused -> uploadNewImage())
                    .addOnFailureListener(e -> uploadNewImage());
        } else uploadNewImage();
    }

    private void uploadNewImage() {
        storageRef.child(studentId + ".jpg")
                .putFile(selectedImageUri)
                .addOnSuccessListener(task -> {

                    storageRef.child(studentId + ".jpg")
                            .getDownloadUrl()
                            .addOnSuccessListener(url -> {

                                studentRef.child("photoUrl").setValue(url.toString());

                                Glide.with(this)
                                        .load(url)
                                        .into(ivPhoto);

                                ivPhoto.setAlpha(1f);
                            });
                });
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        etDate.setText(day + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        picker.show();
    }

    private void handleOneTapCall() {
        ivCall.setOnClickListener(v -> {

            String phone = etPhone.getText().toString();
            if (phone.isEmpty()) return;

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phone));

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        4004);

            } else {
                startActivity(intent);
            }
        });
    }
}
