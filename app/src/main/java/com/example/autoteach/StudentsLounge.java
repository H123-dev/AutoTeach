package com.example.autoteach;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class StudentsLounge extends AppCompatActivity implements View.OnClickListener {
    Button joinClass;
    String studentId;
    FirebaseDatabase db;
    HashMap<String, Double> allGrades; // classID , grade
    Student crrStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_students_lounge);
        db = FirebaseDatabase.getInstance();
        studentId = getIntent().getStringExtra("studentID");
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Error: Student ID is missing.", Toast.LENGTH_LONG).show();
            finish();
        }
        DatabaseReference studentRef = db.getReference("Users").child("Students").child(studentId);
        studentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                crrStudent = task.getResult().getValue(Student.class);
                if (crrStudent != null && crrStudent.submissions != null) {
                    allGrades = crrStudent.submissions;
                } else {
                    allGrades = new HashMap<>();
                }
            } else {
                allGrades = new HashMap<>();
            }

            LinearLayout classesContainer = findViewById(R.id.classes_container);
            for (String classId : allGrades.keySet()) {
                addClassButtonToUI(classId, classesContainer);
            }
        });
        joinClass = findViewById(R.id.join);
        joinClass.setOnClickListener(this);
    }

    public void addClassButtonToUI(String classId, LinearLayout container) {
        Button classButton = new Button(this);
        classButton.setText(classId);
        classButton.setTextSize(18);
        classButton.setTextColor(Color.parseColor("#3949AB"));
        classButton.setBackgroundColor(Color.parseColor("#E8EAF6"));
        classButton.setPadding(12, 12, 12, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        classButton.setLayoutParams(params);
        classButton.setOnClickListener(this);
        container.addView(classButton);
    }

    @Override
    public void onClick(View view) {
        if(view==joinClass)
        {
            Intent intent = new Intent(this, JoinClass.class);
            intent.putExtra("studentID", studentId);
            startActivity(intent);
        }
    }
}