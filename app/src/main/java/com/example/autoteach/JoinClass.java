package com.example.autoteach;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.File;
import java.io.IOException;

public class JoinClass extends AppCompatActivity implements View.OnClickListener {
    private Uri photoUri;
    EditText classCode;
    Button cameraButton , galleryButton , backButton;
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            byte[] imageBytes = null;
                            try {
                                imageBytes = Helper.ImageUtils.uriToBytes(this, uri);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            uploadArrayBytes(imageBytes);
                        }
                    });

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> {
                        byte[] imageBytes = null;
                        try {
                            imageBytes = Helper.ImageUtils.uriToBytes(this, photoUri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (success) {
                            uploadArrayBytes(imageBytes);
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

    private void uploadArrayBytes(byte[] data) {
        String code = classCode.getText().toString().trim();
        if(code.isEmpty())
        {
            Toast.makeText(this, "Please enter class code", Toast.LENGTH_SHORT).show();
            return;
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
}