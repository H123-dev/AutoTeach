package com.example.autoteach;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JoinClass extends AppCompatActivity implements View.OnClickListener {
    FirebaseDatabase db;
    DatabaseReference classRef;
    DatabaseReference studentRef;
    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    Uri photoUri;
    TextView test;
    EditText classCode;
    Button cameraButton , galleryButton , backButton;
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            try {
                                Bitmap bitmap = uriToBitmap(this, uri);
                                processOCR(uri,bitmap);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }}
                    });

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> {
                        try {
                            Bitmap bitmap = uriToBitmap(this, photoUri);
                            processOCR(photoUri,bitmap);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
    String StudetnID;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_class);
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
        if(view==backButton)
        {
            finish();
        }
        else if(view==cameraButton)
        {
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
                e.printStackTrace();
            }
        }
        else if(view==galleryButton)
        {
            pickImage.launch("image/*");
        }
    }
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    100
            );
        }
    }

    private File createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("photo_", ".jpg", storageDir);
    }
    private Bitmap uriToBitmap(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    }
    private void processOCR(Uri uri , Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        byte[] imageBytes = null;
        try {
            imageBytes = Helper.ImageUtils.uriToBytes(this, uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64Image = Helper.ImageUtils.bytesToBase64(imageBytes);
        String classCodeStr = classCode.getText().toString().trim();
        if(classCodeStr.isEmpty())
        {
            Toast.makeText(this, "Please enter the class code.", Toast.LENGTH_LONG).show();
            return;
        }
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
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
                                        "\n" +
                                        "CODE TO FIX:\n" +
                                        ocrText + "\n";
                        AIHELPER.runAIModel(JoinClass.this, prompt, new Listener() {
                            @Override
                            public void onSuccess(String result) {
                                CodeSnippit c = new CodeSnippit(base64Image , ocrText , result );
                                c.calculateGrade(JoinClass.this,classCodeStr, new Listener() {
                                    @Override
                                    public void onSuccess(String result) {
                                        Toast.makeText(JoinClass.this, "Grade calculated: "+c.grade, Toast.LENGTH_LONG).show();
                                        upload(classCodeStr, c);
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {

                                    }
                                });
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(JoinClass.this, "Text recognition failed", Toast.LENGTH_SHORT).show());
    }
    private void upload(String classCode, CodeSnippit codeSnippet) {
        db = FirebaseDatabase.getInstance();
        classRef = db.getReference("Classes");
        classRef.child(classCode).get().addOnCompleteListener(task ->
                {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            Classroom classRoom = snapshot.getValue(Classroom.class);
                            if (classRoom != null) {
                                if(classRoom.studentsSubmissions!=null)
                                {
                                    classRoom.studentsSubmissions.put(StudetnID, codeSnippet);
                                    classRef.child(classCode).setValue(classRoom);
                                    uploadGrade(classCode, codeSnippet);
                                }
                                else
                                {
                                    classRoom.studentsSubmissions = new HashMap<>();
                                    classRoom.studentsSubmissions.put(StudetnID, codeSnippet);
                                    classRef.child(classCode).setValue(classRoom);
                                    uploadGrade(classCode, codeSnippet);
                                }

                            }
                        }
                    } else {
                        Toast.makeText(this, "Class not found", Toast.LENGTH_LONG).show();
                    }
                }
                );
    }
    private void uploadGrade(String classCode, CodeSnippit codeSnippet) {
        db = FirebaseDatabase.getInstance();
        studentRef = db.getReference("Users").child("Students").child(StudetnID);
        studentRef.get().addOnCompleteListener(task ->
                {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            Student student = snapshot.getValue(Student.class);
                            if (student != null) {
                                if(student.submissions!=null)
                                {
                                    student.submissions.put(classCode, codeSnippet.grade);
                                    studentRef.setValue(student);
                                }
                                else
                                {
                                    student.submissions = new HashMap<>();
                                    student.submissions.put(classCode, codeSnippet.grade);
                                    studentRef.setValue(student);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Student not found", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}