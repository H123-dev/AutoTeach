package com.example.autoteach;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CreateClass extends AppCompatActivity implements View.OnClickListener {

    //דף זה אחראי על יצירת כיתה חדשה ע"י המורה, הוא מקבל את מזהה המורה מהמסך הקודם ומאפשר לו להכניס 3  TESTCASEים  , בנוסף הוא מאפשר למורה להכניס שם לכיתה החדשה. לאחר מכן הוא יוצר את הכיתה במסד הנתונים ומעדכן את רשימת הכיתות של המורה.
    EditText tc1,tc2,tc3 , extc1, extc2, extc3 ,name;
    Button create , back;
    FirebaseDatabase db;
    String teacherID;
    Teacher crrteacher;
    DatabaseReference teacherRef;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_class);
        db = FirebaseDatabase.getInstance();
        teacherID = getIntent().getStringExtra("teacherID");
        if (teacherID == null || teacherID.isEmpty()) {
            Toast.makeText(this, "Error: Teacher ID missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        teacherRef = db.getReference("Users").child("Teachers").child(teacherID);
        tc1 = findViewById(R.id.tc1);
        tc2 = findViewById(R.id.tc2);
        tc3 = findViewById(R.id.tc3);
        extc1 = findViewById(R.id.extc1);
        extc2 = findViewById(R.id.extc2);
        extc3 = findViewById(R.id.extc3);
        name = findViewById(R.id.name);
        create = findViewById(R.id.create);
        back = findViewById(R.id.back);
        create.setOnClickListener(this);
        back.setOnClickListener(this);
        teacherRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                crrteacher = task.getResult().getValue(Teacher.class);
                if (crrteacher == null) {
                    Toast.makeText(this, "Error: Teacher not found", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Error loading teacher 1", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view==back)
        {
            finish();
        }
        else if(view==create)
        {
            if (crrteacher == null) {
                Toast.makeText(this, "Teacher data not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(crrteacher.classIDs == null) {
                crrteacher.classIDs = new java.util.ArrayList<>();
            }

            String tc1Str = tc1.getText().toString().trim();
            String extc1Str = extc1.getText().toString().trim();
            String tc2Str = tc2.getText().toString().trim();
            String extc2Str = extc2.getText().toString().trim();
            String tc3Str = tc3.getText().toString().trim();
            String extc3Str = extc3.getText().toString().trim();
            String Aname = name.getText().toString().trim();


            if(tc1Str.isEmpty() || extc1Str.isEmpty() ||
               tc2Str.isEmpty() || extc2Str.isEmpty() ||
               tc3Str.isEmpty() || extc3Str.isEmpty() || Aname.isEmpty()) { {
                Toast.makeText(this, "Please fill in all test cases and expected outputs.", Toast.LENGTH_LONG).show();
                return;
            }}

            TestCase t1 = new TestCase(tc1Str, extc1Str);
            TestCase t2 = new TestCase(tc2Str, extc2Str);
            TestCase t3 = new TestCase(tc3Str, extc3Str);
            ArrayList<TestCase> testCases = new ArrayList<>();
            testCases.add(t1);
            testCases.add(t2);
            testCases.add(t3);
            Classroom newClass = new Classroom(teacherID, testCases,Aname);


            String classId = newClass.classID;
            if (classId == null || classId.isEmpty()) {
                Toast.makeText(this, "Error: Class ID is invalid.", Toast.LENGTH_LONG).show();
                return;
            }

            crrteacher.classIDs.add(classId);
            DatabaseReference teacherRef = db.getReference("Users").child("Teachers").child(teacherID);
            DatabaseReference classRef = db.getReference("Classes");
            DatabaseReference newClassRef = classRef.child(classId);

            newClassRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Toast.makeText(this, "Class ID already exists. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    newClassRef.setValue(newClass)
                            .addOnSuccessListener(aVoid -> {
                                teacherRef.setValue(crrteacher)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(this, "Class Created", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, TeachersLounge.class);
                                            intent.putExtra("teacherID", teacherID);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error updating teacher: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error creating class: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                }
            });

        }
    }
}