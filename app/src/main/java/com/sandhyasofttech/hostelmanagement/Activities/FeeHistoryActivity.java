package com.sandhyasofttech.hostelmanagement.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.sandhyasofttech.hostelmanagement.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.sandhyasofttech.hostelmanagement.Adapters.FeeHistoryAdapter;
import com.sandhyasofttech.hostelmanagement.Models.StudentModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeeHistoryActivity extends AppCompatActivity {

    private RecyclerView rvFees;
    private EditText etSearch;
    private Button btnExportPdf;
    private TextView tvTotalCollected, tvTotalRemaining, tvTotalStudents, tvEmpty;

    private DatabaseReference rootRef;
    private String safeEmail;

    private ArrayList<StudentModel> list = new ArrayList<>();
    private FeeHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_fee_history);

        rvFees = findViewById(R.id.rvFees);
        etSearch = findViewById(R.id.etSearch);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        tvTotalCollected = findViewById(R.id.tvTotalCollected);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvFees.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeeHistoryAdapter(list, this::exportSingleStudentPdf);
        rvFees.setAdapter(adapter);


        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        safeEmail = email.replace(".", ",");
        rootRef = FirebaseDatabase.getInstance()
                .getReference("HostelManagement")
                .child(safeEmail);

        loadStudentsWithFees();
        setupSearch();
        btnExportPdf.setOnClickListener(v -> exportCurrentListToPdf());
    }

    private void loadStudentsWithFees() {
        rootRef.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
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

                // UPDATED: pass listener that calls exportSingleStudentPdf
                adapter = new FeeHistoryAdapter(list, FeeHistoryActivity.this::exportSingleStudentPdf);
                rvFees.setAdapter(adapter);

                tvTotalStudents.setText(String.valueOf(list.size()));
                tvTotalCollected.setText("₹ " + totalCollected);
                tvTotalRemaining.setText("₹ " + totalRemaining);

                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onCancelled(DatabaseError error) { }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
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

            int pageWidth = 595;   // A4 approx
            int pageHeight = 842;

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();

            int y = 40;

            // Title
            paint.setTextSize(18);
            paint.setFakeBoldText(true);
            canvas.drawText("Hostel Fee Report", 40, y, paint);

            // Date
            y += 22;
            paint.setTextSize(11);
            paint.setFakeBoldText(false);
            String date = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(new Date());
            canvas.drawText("Generated on: " + date, 40, y, paint);

            // Header row
            y += 26;
            paint.setFakeBoldText(true);
            canvas.drawText("Name", 40, y, paint);
            canvas.drawText("Room", 180, y, paint);
            canvas.drawText("Total", 260, y, paint);
            canvas.drawText("Paid", 340, y, paint);
            canvas.drawText("Remain", 420, y, paint);

            paint.setFakeBoldText(false);
            y += 16;

            for (StudentModel s : data) {
                if (y > pageHeight - 40) break; // single page

                String name = s.getName() == null ? "" : s.getName();
                String room = s.getRoom() == null ? "" : s.getRoom();

                canvas.drawText(name, 40, y, paint);
                canvas.drawText(room, 180, y, paint);
                canvas.drawText(String.valueOf(s.getAnnualFee()), 260, y, paint);
                canvas.drawText(String.valueOf(s.getPaidFee()), 340, y, paint);
                canvas.drawText(String.valueOf(s.getRemainingFee()), 420, y, paint);

                y += 16;
            }

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

            // Open with PDF viewer
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open Fee Report"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create/open PDF", Toast.LENGTH_SHORT).show();
        } finally {
            if (pdf != null) pdf.close();
            if (fos != null) {
                try { fos.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void exportSingleStudentPdf(StudentModel s) {
        try {
            android.graphics.pdf.PdfDocument pdf = new android.graphics.pdf.PdfDocument();
            android.graphics.Paint paint = new android.graphics.Paint();

            int pageWidth = 595;
            int pageHeight = 842;

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();

            int y = 60;

            // Title
            paint.setTextSize(20);
            paint.setFakeBoldText(true);
            canvas.drawText("Student Fee Report", 40, y, paint);

            // Basic info section
            y += 30;
            paint.setTextSize(12);
            paint.setFakeBoldText(false);

            canvas.drawText("Name: " + safeText(s.getName()), 40, y, paint);
            y += 18;
            canvas.drawText("Room: " + safeText(s.getRoom()), 40, y, paint);
            y += 18;
            canvas.drawText("Class: " + safeText(s.getStudentClass()), 40, y, paint);
            y += 18;
            canvas.drawText("Joining Date: " + safeText(s.getJoiningDate()), 40, y, paint);
            y += 24;

            // Fee box
            paint.setFakeBoldText(true);
            canvas.drawText("Fee Details", 40, y, paint);
            paint.setFakeBoldText(false);
            y += 20;

            canvas.drawText("Total Fee: ₹ " + s.getAnnualFee(), 60, y, paint);
            y += 18;
            canvas.drawText("Total Paid: ₹ " + s.getPaidFee(), 60, y, paint);
            y += 18;
            canvas.drawText("Remaining: ₹ " + s.getRemainingFee(), 60, y, paint);
            y += 24;

            // Footer
            String date = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(new Date());
            paint.setTextSize(10);
            canvas.drawText("Generated on: " + date, 40, pageHeight - 40, paint);

            pdf.finishPage(page);

            String fileName = "Student_Fee_" + safeText(s.getName()).replace(" ", "_")
                    + "_" + System.currentTimeMillis() + ".pdf";
            java.io.File dir = getExternalFilesDir(null);
            java.io.File file = new java.io.File(dir, fileName);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            pdf.writeTo(fos);
            pdf.close();
            fos.close();

            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open Student Report"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create student PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private String safeText(String s) {
        return s == null ? "" : s;
    }

}
