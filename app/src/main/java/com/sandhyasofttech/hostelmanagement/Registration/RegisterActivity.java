package com.sandhyasofttech.hostelmanagement.Registration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.sandhyasofttech.hostelmanagement.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail, etPassword, etConfirmPassword;
    private EditText etHostelName, etAddress, etRooms;

    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private CircleImageView ivLogoPreview;
    private Uri logoUri;
    private TextView tvUploadLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        etHostelName = findViewById(R.id.etHostelName);
        etAddress = findViewById(R.id.etAddress);
        etRooms = findViewById(R.id.etRooms);
        ivLogoPreview = findViewById(R.id.ivLogoPreview);
        tvUploadLogo = findViewById(R.id.tvUploadLogo);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvUploadLogo.setOnClickListener(v -> pickLogo());

        tvBackToLogin.setOnClickListener(v -> {
            finish(); // back to LoginActivity
        });
    }
    private void pickLogo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            logoUri = data.getData();
            ivLogoPreview.setImageURI(logoUri);
        }
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String hostelName = etHostelName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String rooms = etRooms.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter name");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Enter phone");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters required");
            return;
        }
        if (TextUtils.isEmpty(hostelName)) {
            etHostelName.setError("Enter hostel name");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Enter hostel address");
            return;
        }

        if (TextUtils.isEmpty(rooms)) {
            etRooms.setError("Enter total rooms");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {

                        // Firebase key cannot contain "."
                        String safeKey = email.replace(".", ",");

                        DatabaseReference ref =
                                FirebaseDatabase.getInstance().getReference("HostelManagement");

                        HashMap<String, Object> ownerData = new HashMap<>();
                        ownerData.put("email", email);           // REAL MAIL
                        ownerData.put("name", name);
                        ownerData.put("phone", phone);
                        ownerData.put("password", password);     // Storing Password
                        ownerData.put("status", true);
                        ownerData.put("hostelName", hostelName);
                        ownerData.put("hostelAddress", address);
                        ownerData.put("rooms", rooms);
                        ownerData.put("createdOn", System.currentTimeMillis());


                        if (logoUri != null) {
                            FirebaseStorage.getInstance()
                                    .getReference("HostelLogos")
                                    .child(safeKey + ".jpg")
                                    .putFile(logoUri)
                                    .addOnSuccessListener(taskSnap -> {
                                        FirebaseStorage.getInstance()
                                                .getReference("HostelLogos")
                                                .child(safeKey + ".jpg")
                                                .getDownloadUrl()
                                                .addOnSuccessListener(url -> {
                                                    ownerData.put("logoUrl", url.toString());

                                                    saveToDatabase(ref, safeKey, ownerData);
                                                });
                                    });
                        } else {
                            saveToDatabase(ref, safeKey, ownerData);
                        }

                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Registration Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveToDatabase(DatabaseReference ref, String safeKey, HashMap<String, Object> ownerData) {
        ref.child(safeKey)
                .child("ownerInfo")
                .setValue(ownerData)
                .addOnCompleteListener(done -> {
                    if (done.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration Successful!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Failed to save user data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
