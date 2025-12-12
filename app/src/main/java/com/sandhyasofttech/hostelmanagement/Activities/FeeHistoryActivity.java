package com.sandhyasofttech.hostelmanagement.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Adapters.FeeHistoryAdapter;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;
import com.sandhyasofttech.hostelmanagement.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeeHistoryActivity extends AppCompatActivity {

    private RecyclerView rvFees;
    private TextInputEditText etSearch;
    private Button btnExportPdf;
    private TextView tvTotalCollected, tvTotalRemaining, tvTotalStudents, tvEmpty;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAddFee;

    private DatabaseReference rootRef;
    private String safeEmail;

    private ArrayList<StudentModel> list = new ArrayList<>();
    private FeeHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_history);

        initViews();
        setupToolbar();
        setupRecyclerView();
        initFirebase();
        loadStudentsWithFees();
        setupSearch();

        btnExportPdf.setOnClickListener(v -> exportCurrentListToPdf());
    }

    private void initViews() {
        rvFees = findViewById(R.id.rvFees);
        etSearch = findViewById(R.id.etSearch);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        tvTotalCollected = findViewById(R.id.tvTotalCollected);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddFee = findViewById(R.id.fabAddFee);

        // FAB click listener
        fabAddFee.setOnClickListener(v -> {
            Intent intent = new Intent(FeeHistoryActivity.this, CollectFeesActivity.class);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Fee History");
        }
    }

    private void setupRecyclerView() {
        rvFees.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeeHistoryAdapter(list, this::exportSingleStudentPdf);
        rvFees.setAdapter(adapter);
    }

    private void initFirebase() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        safeEmail = email.replace(".", ",");
        rootRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail);
    }

    private void loadStudentsWithFees() {
        rootRef.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                int totalCollected = 0;
                int totalRemaining = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    StudentModel s = ds.getValue(StudentModel.class);
                    if (s != null) {
                        s.setId(ds.getKey());
                        list.add(s);
                        totalCollected += s.getPaidFee();
                        totalRemaining += s.getRemainingFee();
                    }
                }

                // Update summary
                tvTotalStudents.setText(String.valueOf(list.size()));
                tvTotalCollected.setText("₹ " + formatAmount(totalCollected));
                tvTotalRemaining.setText("₹ " + formatAmount(totalRemaining));

                // Update adapter with new data
                adapter = new FeeHistoryAdapter(list, FeeHistoryActivity.this::exportSingleStudentPdf);
                rvFees.setAdapter(adapter);

                // Empty state handling
                if (list.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvFees.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvFees.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FeeHistoryActivity.this,
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void exportCurrentListToPdf() {
        List<StudentModel> data = adapter.getCurrentList();
        if (data == null || data.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        android.graphics.pdf.PdfDocument pdf = null;
        java.io.FileOutputStream fos = null;

        try {
            pdf = new android.graphics.pdf.PdfDocument();
            android.graphics.Paint paint = new android.graphics.Paint();
            android.graphics.Paint paintBold = new android.graphics.Paint();
            paintBold.setFakeBoldText(true);

            int pageWidth = 595;   // A4 width
            int pageHeight = 842;  // A4 height

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();

            int y = 50;

            // Header
            paintBold.setTextSize(22);
            canvas.drawText("Fee History Report", 40, y, paintBold);

            y += 30;
            paint.setTextSize(12);
            String date = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(new Date());
            canvas.drawText("Generated: " + date, 40, y, paint);

            y += 35;

            // Summary section
            paintBold.setTextSize(14);
            canvas.drawText("Summary", 40, y, paintBold);
            y += 20;

            paint.setTextSize(11);
            canvas.drawText("Total Students: " + data.size(), 50, y, paint);
            y += 16;

            int totalCollected = 0, totalRemaining = 0;
            for (StudentModel s : data) {
                totalCollected += s.getPaidFee();
                totalRemaining += s.getRemainingFee();
            }

            canvas.drawText("Total Collected: ₹ " + formatAmount(totalCollected), 50, y, paint);
            y += 16;
            canvas.drawText("Total Remaining: ₹ " + formatAmount(totalRemaining), 50, y, paint);

            y += 30;

            // Table Header
            paintBold.setTextSize(11);
            canvas.drawText("Name", 40, y, paintBold);
            canvas.drawText("Room", 180, y, paintBold);
            canvas.drawText("Total", 260, y, paintBold);
            canvas.drawText("Paid", 340, y, paintBold);
            canvas.drawText("Remaining", 420, y, paintBold);

            y += 18;

            // Draw header line
            canvas.drawLine(40, y, 520, y, paint);
            y += 12;

            // Table rows
            paint.setTextSize(10);
            for (StudentModel s : data) {
                if (y > pageHeight - 50) break; // Prevent overflow

                String name = truncateText(safeText(s.getName()), 20);
                String room = safeText(s.getRoom());

                canvas.drawText(name, 40, y, paint);
                canvas.drawText(room, 180, y, paint);
                canvas.drawText("₹" + s.getAnnualFee(), 260, y, paint);
                canvas.drawText("₹" + s.getPaidFee(), 340, y, paint);
                canvas.drawText("₹" + s.getRemainingFee(), 420, y, paint);

                y += 16;
            }

            // Footer
            paint.setTextSize(9);
            canvas.drawText("Hostel Management System", 40, pageHeight - 30, paint);

            pdf.finishPage(page);

            // Save file
            String fileName = "Fee_Report_" + System.currentTimeMillis() + ".pdf";
            java.io.File dir = getExternalFilesDir(null);
            if (dir == null) {
                Toast.makeText(this, "Storage not available", Toast.LENGTH_SHORT).show();
                return;
            }
            java.io.File file = new java.io.File(dir, fileName);
            fos = new java.io.FileOutputStream(file);
            pdf.writeTo(fos);

            Toast.makeText(this, "PDF exported successfully", Toast.LENGTH_SHORT).show();

            // Open with PDF viewer
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open Fee Report"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create PDF: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            if (pdf != null) pdf.close();
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ignored) {}
            }
        }
    }

    private void exportSingleStudentPdf(StudentModel s) {
        android.graphics.pdf.PdfDocument pdf = null;
        java.io.FileOutputStream fos = null;

        try {
            pdf = new android.graphics.pdf.PdfDocument();
            android.graphics.Paint paint = new android.graphics.Paint();
            android.graphics.Paint paintBold = new android.graphics.Paint();
            paintBold.setFakeBoldText(true);

            int pageWidth = 595;
            int pageHeight = 842;

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();

            int y = 60;

            // Title
            paintBold.setTextSize(24);
            canvas.drawText("Student Fee Report", 40, y, paintBold);

            y += 40;

            // Student Details Section
            paintBold.setTextSize(16);
            canvas.drawText("Student Information", 40, y, paintBold);
            y += 25;

            paint.setTextSize(13);
            canvas.drawText("Name: " + safeText(s.getName()), 50, y, paint);
            y += 20;
            canvas.drawText("Room: " + safeText(s.getRoom()), 50, y, paint);
            y += 20;
            canvas.drawText("Class: " + safeText(s.getStudentClass()), 50, y, paint);
            y += 20;
            canvas.drawText("Contact: " + safeText(s.getPhone()), 50, y, paint);
            y += 20;
            canvas.drawText("Joining Date: " + safeText(s.getJoiningDate()), 50, y, paint);

            y += 40;

            // Fee Details Section
            paintBold.setTextSize(16);
            canvas.drawText("Fee Details", 40, y, paintBold);
            y += 25;

            paint.setTextSize(14);
            canvas.drawText("Annual Fee: ₹ " + formatAmount(s.getAnnualFee()), 50, y, paint);
            y += 22;
            canvas.drawText("Total Paid: ₹ " + formatAmount(s.getPaidFee()), 50, y, paint);
            y += 22;
            paintBold.setTextSize(14);
            canvas.drawText("Remaining: ₹ " + formatAmount(s.getRemainingFee()), 50, y, paintBold);

            y += 40;

            // Payment Status
            String status = s.getRemainingFee() == 0 ? "Paid" : "Pending";
            paint.setTextSize(12);
            canvas.drawText("Status: " + status, 50, y, paint);

            // Footer
            paint.setTextSize(10);
            String date = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(new Date());
            canvas.drawText("Generated on: " + date, 40, pageHeight - 40, paint);
            canvas.drawText("Hostel Management System", 40, pageHeight - 25, paint);

            pdf.finishPage(page);

            String fileName = "Fee_" + safeText(s.getName()).replace(" ", "_")
                    + "_" + System.currentTimeMillis() + ".pdf";
            java.io.File dir = getExternalFilesDir(null);
            java.io.File file = new java.io.File(dir, fileName);
            fos = new java.io.FileOutputStream(file);
            pdf.writeTo(fos);

            Toast.makeText(this, "Student PDF exported", Toast.LENGTH_SHORT).show();

            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open Student Report"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create PDF: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            if (pdf != null) pdf.close();
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ignored) {}
            }
        }
    }

    private String safeText(String s) {
        return s == null || s.trim().isEmpty() ? "N/A" : s;
    }

    private String formatAmount(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
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