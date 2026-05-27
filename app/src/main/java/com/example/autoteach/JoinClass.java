package com.example.autoteach;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import android.os.Handler;
import android.os.Looper;

//המחלקה הזו אחראית על כל הלוגיקה של הצטרפות לכיתה, כולל צילום תמונה או בחירת תמונה מהגלריה, הרצת OCR על התמונה, תיקון הקוד עם AI, חישוב הציון, והעלאת התוצאה ל-Firebase.

public class JoinClass extends AppCompatActivity implements View.OnClickListener {


    FirebaseDatabase db;
    DatabaseReference classesRef;
    DatabaseReference studentsRef;

    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    Uri photoUri;

    TextView test;
    EditText classCode;

    Button cameraButton, galleryButton, backButton;

    String StudetnID;

    // רישום ל-ActivityResultLauncher עבור בחירת תמונה מהגלריה
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            try {
                                Toast.makeText(this, "Image selected, running OCR...", Toast.LENGTH_SHORT).show();
                                Bitmap bitmap = uriToBitmap(this, uri);
                                processOCR(uri, bitmap);
                            } catch (Exception e) {
                                Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

    // רישום ל-ActivityResultLauncher עבור צילום תמונה עם המצלמה
    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> {
                        try {
                            Toast.makeText(this, "Photo captured, running OCR...", Toast.LENGTH_SHORT).show();
                            Bitmap bitmap = uriToBitmap(this, photoUri);
                            processOCR(photoUri, bitmap);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to process photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_join_class);

        // Firebase init ONLY ONCE
        db = FirebaseDatabase.getInstance();
        classesRef = db.getReference("Classes");
        studentsRef = db.getReference("Users").child("Students");

        classCode = findViewById(R.id.classCode);

        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        backButton = findViewById(R.id.backButton);

        cameraButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        StudetnID = getIntent().getStringExtra("studentID");

        if (StudetnID == null || StudetnID.isEmpty()) {
            Toast.makeText(this, "Error: Student ID missing", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onClick(View view) {

        if (view == backButton) {

            finish();

        } else if (view == cameraButton) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestCameraPermission();
                return;
            }

            try {

                File photoFile = createImageFile();

                photoUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        photoFile
                );

                takePicture.launch(photoUri);

            } catch (IOException e) {

                Toast.makeText(this, "Failed to create image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } else if (view == galleryButton) {

            pickImage.launch("image/*");
        }
    }

    // בקשת הרשאת מצלמה מהמשתמש

    private void requestCameraPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    100
            );
        }
    }

    // יצירת קובץ תמונה זמני לאחסון התמונה שצולמה לפני עיבוד OCR
    private File createImageFile() throws IOException {

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile("photo_", ".jpg", storageDir);
    }


    // המרה של URI לתמונה מסוג Bitmap כדי שניתן יהיה להריץ עליה OCR

    private Bitmap uriToBitmap(Context context, Uri uri) throws IOException {

        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        inputStream.close();

        return bitmap;
    }

    // הפונקציה המרכזית שמטפלת בכל התהליך: מריצה OCR על התמונה, שולחת את הטקסט ל-AI לתיקון, מחשבת את הציון, ומעלה את התוצאה ל-Firebase.
    private void processOCR(Uri uri, Bitmap bitmap) {

        Toast.makeText(this, "Starting OCR scan...", Toast.LENGTH_SHORT).show();

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        String classCodeStr = classCode.getText().toString().trim();

        if (classCodeStr.isEmpty()) {

            Toast.makeText(this, "Please enter the class code.", Toast.LENGTH_LONG).show();
            return;
        }

        recognizer.process(image)

                .addOnSuccessListener(new OnSuccessListener<Text>() {

                    // כאשר ה-OCR מצליח, שולחים את הטקסט ל-AI לתיקון, ואז מחשבים את הציון ומעלים ל-Firebase
                    @Override
                    public void onSuccess(Text visionText) {

                        Toast.makeText(JoinClass.this, "OCR complete. Sending to AI fixer...", Toast.LENGTH_SHORT).show();

                        StringBuilder sb = new StringBuilder();

                        sb.append(visionText.getText()).append("\n");

                        String ocrText = sb.toString();

                        String prompt =
                                "You are a Java 17 compiler-level code fixer.\n" +
                                        "\n" +
                                        "INPUT:\n" +
                                        "Java source code extracted from OCR. It may contain mistakes like extra spaces, missing semicolons, or other OCR artifacts.\n" +
                                        "\n" +
                                        "TASK:\n" +
                                        "Fix ALL Java syntax and compilation errors so it compiles under Java 17.\n" +
                                        "\n" +
                                        "RULES (MUST FOLLOW):\n" +
                                        "1. Output ONLY valid Java source code.\n" +
                                        "2. Do NOT include explanations, comments, markdown, or formatting.\n" +
                                        "3. Do NOT include ``` or ''' or any quotes.\n" +
                                        "4. Do NOT include the word 'java' or any language header.\n" +
                                        "5. The FIRST and ONLY public class must be named Main.\n" +
                                        "6. Do NOT place static members inside non-static inner classes.\n" +
                                        "7. Preserve original logic, variable names, and structure unless changes are required to compile.\n" +
                                        "8. Inner classes must be properly wrapped with opening '{' and closing '}' braces.\n" +
                                        "   If necessary, make inner classes static or top-level to ensure compilation.\n" +
                                        "\n" +
                                        "OUTPUT:\n" +
                                        "Return ONLY the corrected Java code. Nothing else.\n" +
                                        "REMEMBEER DO NOT EVEN TRY TO LOFICLY FIX THE CODE, JUST FIX SYNTAX AND COMPILATION ERRORS. DO NOT CHANGE VARIABLE NAMES OR LOGIC. DO NOT ADD ANYTHING ELSE TO THE CODE.\n" +
                                        "\n" +
                                        "CODE TO FIX:\n" +
                                        ocrText + "\n";

                        AIHELPER.runAIModel(JoinClass.this, prompt, new Listener() {

                            @Override
                            public void onSuccess(String result) {

                                Toast.makeText(JoinClass.this, "AI fix complete. Calculating grade...", Toast.LENGTH_SHORT).show();

                                CodeSnippit c = new CodeSnippit(ocrText, result);

                                c.calculateGrade(JoinClass.this, classCodeStr, new Listener() {

                                    // כאשר חישוב הציון מסתיים, מעלים את התוצאה ל-Firebase
                                    @Override
                                    public void onSuccess(String result) {

                                        Toast.makeText(JoinClass.this, "Grade calculated: " + c.grade + ". Uploading...", Toast.LENGTH_LONG).show();

                                        upload(classCodeStr, c);
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {

                                        Toast.makeText(JoinClass.this, "Grading failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                                Toast.makeText(JoinClass.this, "AI fixer failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })

                .addOnFailureListener(e ->
                        Toast.makeText(JoinClass.this, "OCR failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // הפונקציה שמעלה את הקוד המתוקן והציון ל-Firebase תחת הכיתה והסטודנט המתאימים
    private void upload(String classCode, CodeSnippit codeSnippet) {


        classesRef.child(classCode).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                DataSnapshot snapshot = task.getResult();

                if (snapshot.exists()) {

                    Classroom classRoom = snapshot.getValue(Classroom.class);

                    if (classRoom != null) {

                        if (classRoom.studentsSubmissions == null) {

                            classRoom.studentsSubmissions = new HashMap<>();
                        }

                        classRoom.studentsSubmissions.put(StudetnID, codeSnippet);

                        classesRef.child(classCode).setValue(classRoom)

                                .addOnSuccessListener(unused -> {


                                    uploadGrade(classCode, codeSnippet);
                                })

                                .addOnFailureListener(e ->
                                        Toast.makeText(JoinClass.this, "Failed to upload submission: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }

                } else {

                    Toast.makeText(this, "Class not found", Toast.LENGTH_LONG).show();
                }

            } else {

                Toast.makeText(this, "Failed to fetch class: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // הפונקציה שמעלה את הציון של הסטודנט ל-Firebase תחת הנתיב של הסטודנט עצמו, כדי שיהיה נגיש גם מהפרופיל שלו וגם מהכיתה
    private void uploadGrade(String classCode, CodeSnippit codeSnippet) {

        DatabaseReference studentRef = studentsRef.child(StudetnID);

        studentRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                DataSnapshot snapshot = task.getResult();

                if (snapshot.exists()) {

                    Student student = snapshot.getValue(Student.class);

                    if (student != null) {

                        if (student.submissions == null) {

                            student.submissions = new HashMap<>();
                        }

                        if(student.submissions.containsKey(classCode)){

                            Toast.makeText(this, "Grade already exists for this class. Overwriting...", Toast.LENGTH_SHORT).show();
                        }

                        student.submissions.put(classCode, codeSnippet.grade);

                        studentRef.setValue(student)
                                .addOnSuccessListener(unused -> {
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 3000);
                                })

                                .addOnFailureListener(e ->

                                        Toast.makeText(JoinClass.this, "Failed to save grade: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }

                } else {

                    Toast.makeText(JoinClass.this, "Student not found", Toast.LENGTH_LONG).show();
                }

            } else {

                Toast.makeText(JoinClass.this, "Failed to fetch student: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}