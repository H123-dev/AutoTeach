package com.example.autoteach;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener {
    EditText name , password, id;
    Button send , login;
    CheckBox student , teacher;
    FirebaseDatabase db;
    DatabaseReference StudentRef, TeacherRef , UserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        name = findViewById(R.id.name);
        password = findViewById(R.id.pass);
        id = findViewById(R.id.id);
        send = findViewById(R.id.send);
        db = FirebaseDatabase.getInstance();
        UserRef = db.getReference("Users");
        StudentRef = UserRef.child("Students");
        TeacherRef = UserRef.child("Teachers");
        login = findViewById(R.id.login);
        student = findViewById(R.id.student);
        teacher = findViewById(R.id.teacher);
        send.setOnClickListener(this);
        login.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view==login)
        {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        else
        {
            String Name = name.getText().toString();
            String Password = password.getText().toString();
            String Id = id.getText().toString();
            if(!Helper.checkPass(Password))
            {
                Toast.makeText(this, "password must contain lower case upper case and numbers", Toast.LENGTH_LONG).show();
                return;
            }
            if(!Helper.checkID(Id))
            {
                Toast.makeText(this, "ID must be 6 digits", Toast.LENGTH_LONG).show();
                return;
            }
            if(!teacher.isChecked() && !student.isChecked())
            {
                Toast.makeText(this, "please select account type", Toast.LENGTH_LONG).show();
                return;
            }
            if (teacher.isChecked() && student.isChecked())
            {
                Toast.makeText(this, "please select only one account type", Toast.LENGTH_LONG).show();
                return;
            }
            if(teacher.isChecked())
            {
                Teacher t = new Teacher(Name,Password,Id);
                TeacherRef.child(Id).setValue(t)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Registration failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());
                Intent intent = new Intent(this, TeachersLounge.class);
                intent.putExtra("teacherID",Id);
                startActivity(intent);
            }
            else if(student.isChecked())
            {
                Student s = new Student(Name,Password,Id);
                StudentRef.child(Id).setValue(s)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Registration failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());
                Intent intent = new Intent(Register.this, StudentsLounge.class);
                String Sid = id.getText().toString().trim();
                intent.putExtra("studentID", Sid);
                startActivity(intent);
            }
        }
    }
}