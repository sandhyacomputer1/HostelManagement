package com.sandhyasofttech.hostelmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.hostelmanagement.Adapters.ExpenseAdapter;
import com.sandhyasofttech.hostelmanagement.R;
import com.sandhyasofttech.hostelmanagement.Models.ExpenseModel;
import java.util.ArrayList;

public class ExpenseListActivity extends AppCompatActivity {
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;
    private ArrayList<ExpenseModel> expenseList;
    private DatabaseReference expensesRef;
    private Button btnAddExpense, btnProfitLoss;
    private TextView tvTotalExpenses;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        initViews();
        setupToolbar();
        setupRecyclerView();
        initFirebase();
        loadExpenses();

        btnAddExpense.setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseEntryActivity.class)));

        btnProfitLoss.setOnClickListener(v ->
                Toast.makeText(this, "Profit/Loss Report Coming Soon!", Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        rvExpenses = findViewById(R.id.rvExpenses);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnProfitLoss = findViewById(R.id.btnProfitLoss);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);  // ✅ TextView
        layoutEmpty = findViewById(R.id.layoutEmpty);          // ✅ Empty state

        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(expenseList);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarExpenseList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Expenses");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);
    }

    private void initFirebase() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;

        String safeEmail = email.replace(".", ",");
        expensesRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail)
                .child("Expenses");
    }

    private void loadExpenses() {
        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                expenseList.clear();
                int totalExpenses = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ExpenseModel expense = ds.getValue(ExpenseModel.class);
                    if (expense != null) {
                        expenseList.add(expense);
                        totalExpenses += expense.getAmount();
                    }
                }

                adapter.notifyDataSetChanged();

                // ✅ FIXED: Cast to TextView
                tvTotalExpenses.setText("₹ " + totalExpenses);

                // Empty state handling
                if (expenseList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvExpenses.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvExpenses.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ExpenseListActivity.this, "Load failed!", Toast.LENGTH_SHORT).show();
            }
        });
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
