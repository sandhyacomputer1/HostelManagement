    package com.sandhyasofttech.hostelmanagement.Activities;

    import android.app.DatePickerDialog;
    import android.net.Uri;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.*;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;

    import com.bumptech.glide.Glide;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.*;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.sandhyasofttech.hostelmanagement.R;

    import java.util.Calendar;
    import java.util.HashMap;

    public class AddStudentActivity extends AppCompatActivity {
        private CheckBox cbShareWhatsapp;
        private LinearLayout llStep1Personal, llStep2Parent, llStep3Docs;
        private TextView tvStepTitle, tvStepSubtitle, tvStepIndicator;
        private Button btnNextStep1, btnBackStep2, btnNextStep2, btnBackStep3;
        private int currentStep = 1;

        private ImageView ivStudentImage, ivAddPhoto;
        private ImageView ivAadhaar, ivAddAadhaar;
        private ImageView ivPan, ivAddPan;

        private EditText etName, etPhone, etRoom, etAddress, etFees;
        private EditText etParentName, etParentPhone;
        private Spinner spClass;
        private TextView tvJoinDate;
        private Button btnSave;
        private ProgressBar progressBar;

        private Uri mainPhotoUri;
        private Uri aadhaarPhotoUri;
        private Uri panPhotoUri;

        private String selectedDate = "";

        private static final int PICK_MAIN_IMAGE = 101;
        private static final int PICK_AADHAAR_IMAGE = 102;
        private static final int PICK_PAN_IMAGE = 103;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_student);

            initViews();
            setupClasses();

            // All image pickers
            View.OnClickListener profileClick = v -> pickImage(PICK_MAIN_IMAGE);
            ivStudentImage.setOnClickListener(profileClick);
            ivAddPhoto.setOnClickListener(profileClick);

            View.OnClickListener aadhaarClick = v -> pickImage(PICK_AADHAAR_IMAGE);
            ivAadhaar.setOnClickListener(aadhaarClick);
            ivAddAadhaar.setOnClickListener(aadhaarClick);

            View.OnClickListener panClick = v -> pickImage(PICK_PAN_IMAGE);
            ivPan.setOnClickListener(panClick);
            ivAddPan.setOnClickListener(panClick);

            tvJoinDate.setOnClickListener(v -> selectJoiningDate());
            btnSave.setOnClickListener(v -> saveStudent());

            showStep(1);

            btnNextStep1.setOnClickListener(v -> {
                if (validateStep1()) {
                    showStep(2);
                }
            });

            btnBackStep2.setOnClickListener(v -> showStep(1));
            btnNextStep2.setOnClickListener(v -> {
                if (validateStep2()) {
                    showStep(3);
                }
            });

            btnBackStep3.setOnClickListener(v -> showStep(2));


        }
        private void showStep(int step) {
            currentStep = step;

            llStep1Personal.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
            llStep2Parent.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
            llStep3Docs.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

            if (step == 1) {
                tvStepTitle.setText("Step 1 of 3 - Personal Details");
                tvStepSubtitle.setText("Fill student's basic information");
                tvStepIndicator.setText("●  ○  ○");
            } else if (step == 2) {
                tvStepTitle.setText("Step 2 of 3 - Parent Information");
                tvStepSubtitle.setText("Enter parent contact and address");
                tvStepIndicator.setText("○  ●  ○");
            } else {
                tvStepTitle.setText("Step 3 of 3 - Fee & Documents");
                tvStepSubtitle.setText("Confirm fee and upload documents");
                tvStepIndicator.setText("○  ○  ●");
            }
        }
        private boolean validateStep1() {
            if (etName.getText().toString().isEmpty()) { etName.setError("Required"); return false; }
            if (etPhone.getText().toString().isEmpty()) { etPhone.setError("Required"); return false; }
            if (etRoom.getText().toString().isEmpty()) { etRoom.setError("Required"); return false; }
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Select joining date", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        private boolean validateStep2() {
            if (etParentName.getText().toString().isEmpty()) { etParentName.setError("Required"); return false; }
            if (etParentPhone.getText().toString().isEmpty()) { etParentPhone.setError("Required"); return false; }
            if (etAddress.getText().toString().isEmpty()) { etAddress.setError("Required"); return false; }
            return true;
        }


        private void initViews() {
            ivStudentImage = findViewById(R.id.ivStudentImage);
            ivAddPhoto = findViewById(R.id.ivAddPhoto);
            ivAadhaar = findViewById(R.id.ivAadhaar);
            ivAddAadhaar = findViewById(R.id.ivAddAadhaar);
            ivPan = findViewById(R.id.ivPan);
            ivAddPan = findViewById(R.id.ivAddPan);
            cbShareWhatsapp = findViewById(R.id.cbShareWhatsapp);

            etName = findViewById(R.id.etStudentName);
            etPhone = findViewById(R.id.etStudentPhone);
            etRoom = findViewById(R.id.etStudentRoom);
            etAddress = findViewById(R.id.etStudentAddress);
            etFees = findViewById(R.id.etStudentFees);
            etParentName = findViewById(R.id.etParentName);
            etParentPhone = findViewById(R.id.etParentPhone);
            spClass = findViewById(R.id.spStudentClass);
            tvJoinDate = findViewById(R.id.tvJoiningDate);
            btnSave = findViewById(R.id.btnSaveStudent);
            progressBar = findViewById(R.id.progressBarAdd);

            tvStepTitle = findViewById(R.id.tvStepTitle);
            tvStepSubtitle = findViewById(R.id.tvStepSubtitle);
            tvStepIndicator = findViewById(R.id.tvStepIndicator);

            llStep1Personal = findViewById(R.id.llStep1Personal);
            llStep2Parent = findViewById(R.id.llStep2Parent);
            llStep3Docs = findViewById(R.id.llStep3Docs);

            btnNextStep1 = findViewById(R.id.btnNextStep1);
            btnBackStep2 = findViewById(R.id.btnBackStep2);
            btnNextStep2 = findViewById(R.id.btnNextStep2);
            btnBackStep3 = findViewById(R.id.btnBackStep3);

        }

        private void setupClasses() {
            String[] classes = {"8th","9th","10th","11th","12th","Diploma","ITI","College First Year"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classes);
            spClass.setAdapter(adapter);
        }

        private void pickImage(int code) {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, code);
        }

        @Override
        protected void onActivityResult(int req, int res, @Nullable Intent data) {
            super.onActivityResult(req, res, data);
            if (res != RESULT_OK || data == null) return;

            Uri uri = data.getData();
            if (uri == null) return;

            if (req == PICK_MAIN_IMAGE) {
                mainPhotoUri = uri;
                Glide.with(this).load(mainPhotoUri).into(ivStudentImage);
            } else if (req == PICK_AADHAAR_IMAGE) {
                aadhaarPhotoUri = uri;
                Glide.with(this).load(aadhaarPhotoUri).into(ivAadhaar);
            } else if (req == PICK_PAN_IMAGE) {
                panPhotoUri = uri;
                Glide.with(this).load(panPhotoUri).into(ivPan);
            }
        }

        private void selectJoiningDate() {
            Calendar c = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        selectedDate = d + "/" + (m + 1) + "/" + y;
                        tvJoinDate.setText(selectedDate);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        }

        private void saveStudent() {
            if (!validateFields()) return;
            showLoader(true);

            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String safeEmail = email.replace(".", ",");

            DatabaseReference studentsRef = FirebaseDatabase.getInstance()
                    .getReference("HostelManagement")
                    .child(safeEmail)
                    .child("Students");

            String studentId = studentsRef.push().getKey();

            // 1) Save student immediately with empty URLs
            saveStudentData(studentId, studentsRef,
                    "", "", "");   // profile, aadhaar, pan URLs empty now

            // 2) Upload images in background and update URLs when done
            if (mainPhotoUri != null) {
                uploadPhotoAndUpdateField(studentId, studentsRef,
                        mainPhotoUri,
                        "StudentsProfile",
                        "photoUrl");
            }

            if (aadhaarPhotoUri != null) {
                uploadPhotoAndUpdateField(studentId, studentsRef,
                        aadhaarPhotoUri,
                        "StudentsDocs/Aadhaar",
                        "aadhaarPhotoUrl");
            }

            if (panPhotoUri != null) {
                uploadPhotoAndUpdateField(studentId, studentsRef,
                        panPhotoUri,
                        "StudentsDocs/Pan",
                        "panPhotoUrl");
            }
        }

        private void uploadPhotoAndUpdateField(String studentId,
                                               DatabaseReference studentsRef,
                                               Uri uri,
                                               String storageFolder,
                                               String dbFieldName) {

            StorageReference photoRef = FirebaseStorage.getInstance()
                    .getReference(storageFolder)
                    .child(studentId + "_" + dbFieldName + ".jpg");

            photoRef.putFile(uri)
                    .addOnSuccessListener(task ->
                            photoRef.getDownloadUrl().addOnSuccessListener(url -> {
                                studentsRef.child(studentId)
                                        .child(dbFieldName)
                                        .setValue(url.toString());
                            })
                    );
        }

        private void saveStudentData(String studentId,
                                     DatabaseReference ref,
                                     String profileUrl,
                                     String aadhaarUrl,
                                     String panUrl) {

            int totalFee = Integer.parseInt(etFees.getText().toString());

            HashMap<String,Object> map = new HashMap<>();
            map.put("id", studentId);
            map.put("name", etName.getText().toString());
            map.put("phone", etPhone.getText().toString());
            map.put("room", etRoom.getText().toString());
            map.put("address", etAddress.getText().toString());
            map.put("joiningDate", selectedDate);

            map.put("annualFee", totalFee);
            map.put("paidFee", 0);
            map.put("remainingFee", totalFee);

            map.put("parentName", etParentName.getText().toString());
            map.put("parentPhone", etParentPhone.getText().toString());
            map.put("studentClass", spClass.getSelectedItem().toString());
            map.put("photoUrl", profileUrl);
            map.put("aadhaarPhotoUrl", aadhaarUrl);
            map.put("panPhotoUrl", panUrl);
            map.put("active", true);

            ref.child(studentId)
                    .setValue(map)
                    .addOnSuccessListener(done -> {
                        Toast.makeText(this, "Student Saved ✔", Toast.LENGTH_SHORT).show();
                        showLoader(false);

                        if (cbShareWhatsapp.isChecked()) {
                            createAndShareAdmissionPdf(studentId, map);
                        }

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed !", Toast.LENGTH_SHORT).show();
                        showLoader(false);
                    });


        }
        private void createAndShareAdmissionPdf(String studentId, HashMap<String, Object> data) {
            android.graphics.pdf.PdfDocument pdf = null;
            java.io.FileOutputStream fos = null;

            try {
                pdf = new android.graphics.pdf.PdfDocument();
                android.graphics.Paint paint = new android.graphics.Paint();

                int pageWidth = 595;  // A4 approx
                int pageHeight = 842;

                android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                        new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
                android.graphics.Canvas canvas = page.getCanvas();

                int y = 70;

                // Header
                paint.setTextSize(20);
                paint.setFakeBoldText(true);
                canvas.drawText("Hostel Admission Confirmation", 60, y, paint);

                y += 30;
                paint.setTextSize(12);
                paint.setFakeBoldText(false);

                // Basic details
                String name        = String.valueOf(data.get("name"));
                String room        = String.valueOf(data.get("room"));
                String cls         = String.valueOf(data.get("studentClass"));
                String join        = String.valueOf(data.get("joiningDate"));
                String phone       = String.valueOf(data.get("phone"));
                String parentName  = String.valueOf(data.get("parentName"));
                String parentPhoneRaw = String.valueOf(data.get("parentPhone"));
                String address     = String.valueOf(data.get("address"));
                int annualFee      = (int) data.get("annualFee");

                canvas.drawText("Student ID : " + studentId, 60, y, paint);   y += 18;
                canvas.drawText("Name       : " + name,        60, y, paint); y += 18;
                canvas.drawText("Class      : " + cls,         60, y, paint); y += 18;
                canvas.drawText("Room       : " + room,        60, y, paint); y += 18;
                canvas.drawText("Phone      : " + phone,       60, y, paint); y += 18;
                canvas.drawText("Address    : " + address,     60, y, paint); y += 24;

                canvas.drawText("Parent Name  : " + parentName, 60, y, paint); y += 18;
                canvas.drawText("Parent Phone : " + parentPhoneRaw,60, y, paint); y += 24;

                // Fee block
                paint.setFakeBoldText(true);
                canvas.drawText("Fee Details", 60, y, paint);
                paint.setFakeBoldText(false);
                y += 20;

                canvas.drawText("Annual Fee      : ₹ " + annualFee, 80, y, paint); y += 18;
                canvas.drawText("Admission Status: CONFIRMED",      80, y, paint); y += 30;

                // Footer date
                String date = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.US)
                        .format(new java.util.Date());
                paint.setTextSize(10);
                canvas.drawText("Generated on: " + date, 60, pageHeight - 50, paint);

                pdf.finishPage(page);

                // Save PDF (user will attach manually)
                String fileName = "Admission_" + name.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";
                java.io.File dir = getExternalFilesDir(null);
                if (dir == null) return;
                java.io.File file = new java.io.File(dir, fileName);
                fos = new java.io.FileOutputStream(file);
                pdf.writeTo(fos);

                // CLEAN PARENT PHONE FOR wa.me
                // Example:
                //  input: "+91 9876543210" -> "919876543210"
                //  input: "09876543210"   -> "9876543210" (you must ensure you already include country code)
                String parentPhone = parentPhoneRaw.trim()
                        .replace(" ", "")
                        .replace("+", "");

                // If you always store Indian numbers as "9876543210", prepend country code:
                if (!parentPhone.startsWith("91") && parentPhone.length() == 10) {
                    parentPhone = "91" + parentPhone;
                }

                String encodedMsg = java.net.URLEncoder.encode(
                        "Hostel admission confirmation for " + name + ".", "UTF-8");
                String url = "https://wa.me/" + parentPhone + "?text=" + encodedMsg;

                // Share directly to parent's WhatsApp with PDF attached
                Uri pdfUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        file
                );

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("application/pdf");
                sendIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hostel admission confirmation for " + name + ".");
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.setPackage("com.whatsapp");

// 🔥 Direct chat:
                sendIntent.putExtra("jid", parentPhone + "@s.whatsapp.net");

                try {
                    startActivity(sendIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "WhatsApp not installed or JID unsupported", Toast.LENGTH_SHORT).show();
                }



            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to create admission PDF", Toast.LENGTH_SHORT).show();
            } finally {
                if (pdf != null) pdf.close();
                if (fos != null) {
                    try { fos.close(); } catch (Exception ignored) {}
                }
            }
        }

        private boolean validateFields() {

            if (etName.getText().toString().isEmpty()) { etName.setError("Required"); return false; }
            if (etPhone.getText().toString().isEmpty()) { etPhone.setError("Required"); return false; }
            if (etRoom.getText().toString().isEmpty()) { etRoom.setError("Required"); return false; }
            if (etAddress.getText().toString().isEmpty()) { etAddress.setError("Required"); return false; }
            if (etFees.getText().toString().isEmpty()) { etFees.setError("Required"); return false; }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Select joining date", Toast.LENGTH_SHORT).show();
                return false;
            }

            // All photos optional
            return true;
        }

        private void showLoader(boolean show) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!show);
            ivStudentImage.setEnabled(!show);
            ivAddPhoto.setEnabled(!show);
            ivAadhaar.setEnabled(!show);
            ivAddAadhaar.setEnabled(!show);
            ivPan.setEnabled(!show);
            ivAddPan.setEnabled(!show);
        }
    }
