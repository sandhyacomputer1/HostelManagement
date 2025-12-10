package com.sandhyasofttech.hostelmanagement.Activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Models.ExpenseModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseEntryActivity extends AppCompatActivity {
    private EditText etAmount, etDescription;
    private Spinner spCategory;
    private Button btnSaveExpense;
    private DatabaseReference expensesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_entry);

        initViews();
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

    private void setupCategories() {
        String[] categories = {"Food", "Electricity", "Water", "Maintenance", "Staff Salary", "Cleaning", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void initFirebase() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
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

        if (amountStr.isEmpty()) {
            etAmount.setError("Required");
            return;
        }

        int amount = Integer.parseInt(amountStr);
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        String expenseId = expensesRef.push().getKey();
        ExpenseModel expense = new ExpenseModel(expenseId, category, amount, description, date, time);

        expensesRef.child(expenseId)
                .setValue(expense)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Expense Saved ✔ ₹" + amount, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show());
    }
}
