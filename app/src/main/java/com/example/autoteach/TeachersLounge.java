package com.example.autoteach;

import android.annotation.SuppressLint;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

//דף הבית של המורה, שם הוא יכול לראות את כל הכיתות שלו וליצור כיתה חדשה

public class TeachersLounge extends AppCompatActivity implements View.OnClickListener {
    String teacherID;
    Teacher currentTeacher;
    FirebaseDatabase db;
    Button createClass;
    ArrayList<String> classIDs;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teachers_lounge);
        db = FirebaseDatabase.getInstance();
        teacherID = getIntent().getStringExtra("teacherID");

        if (teacherID == null || teacherID.isEmpty()) {
            Toast.makeText(this, "Error: Teacher ID is missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        DatabaseReference teacherRef = db.getReference("Users").child("Teachers").child(teacherID);
        teacherRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentTeacher = task.getResult().getValue(Teacher.class);
                if (currentTeacher != null && currentTeacher.classIDs != null) {
                    classIDs = currentTeacher.classIDs;
                } else {
                    classIDs = new ArrayList<>();
                }
            } else {
                classIDs = new ArrayList<>();
            }
            LinearLayout classesContainer = findViewById(R.id.classes_container);
            for(String classId : classIDs) {
                addClassButtonToUI(classId, classesContainer);
            }
        });
        createClass = findViewById(R.id.createClassButton);
        createClass.setOnClickListener(this);
    }

    public void addClassButtonToUI(String classId, LinearLayout container) {// פונקציה שמוסיפה כפתור לכל כיתה של המורה, הכפתור מוביל לדף של הכיתה
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
        if(view == createClass)
        {
            Intent intent = new Intent(this, CreateClass.class);
            intent.putExtra("teacherID", teacherID);
            startActivity(intent);
        }
        else
        {
            String buttonName = ((Button)view).getText().toString();
            Intent intent = new Intent(this, ClassView.class);
            intent.putExtra("ClassID", buttonName);
            startActivity(intent);
        }
    }
}