package com.sandhyasofttech.hostelmanagement.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Models.ExpenseModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseEntryActivity extends AppCompatActivity {
    private TextInputEditText etAmount, etDescription;
    private Spinner spCategory;
    private Button btnSaveExpense;
    private DatabaseReference expensesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_entry);

        initViews();
        setupToolbar();
        setupCategories();
        initFirebase();

        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void initViews() {
        etAmount = findViewById(R.id.etExpenseAmount);
        etDescription = findViewById(R.id.etExpenseDescription);
        spCategory = findViewById(R.id.spExpenseCategory);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Expense");
        }
    }

    private void setupCategories() {
        String[] categories = {
                "Food",
                "Electricity",
                "Water",
                "Maintenance",
                "Staff Salary",
                "Cleaning",
                "Rent",
                "Internet",
                "Gas",
                "Others"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void initFirebase() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String safeEmail = email.replace(".", ",");
        expensesRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Expenses");
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        // Validation
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);

            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }

            // Disable button to prevent double submission
            btnSaveExpense.setEnabled(false);
            btnSaveExpense.setText("Saving...");

            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            String expenseId = expensesRef.push().getKey();
            if (expenseId == null) {
                Toast.makeText(this, "Failed to generate expense ID", Toast.LENGTH_SHORT).show();
                btnSaveExpense.setEnabled(true);
                btnSaveExpense.setText("Save Expense");
                return;
            }

            ExpenseModel expense = new ExpenseModel(expenseId, category, amount, description, date, time);

            expensesRef.child(expenseId)
                    .setValue(expense)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "✓ Expense Saved: ₹" + amount, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                        btnSaveExpense.setEnabled(true);
                        btnSaveExpense.setText("Save Expense");
                    });

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}