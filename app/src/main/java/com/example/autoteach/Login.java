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

//דף LOGIN
public class Login extends AppCompatActivity implements View.OnClickListener {

    EditText password, id;
    Button send, register;

    FirebaseDatabase db;
    DatabaseReference StudentRef, TeacherRef , UserRef;

    CheckBox student , teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        password = findViewById(R.id.pass);
        id = findViewById(R.id.id);
        send = findViewById(R.id.send);
        register = findViewById(R.id.register);

        db = FirebaseDatabase.getInstance();
        UserRef = db.getReference("Users");
        StudentRef = UserRef.child("Students");
        TeacherRef = UserRef.child("Teachers");

        student = findViewById(R.id.student);
        teacher = findViewById(R.id.teacher);

        register.setOnClickListener(this);
        send.setOnClickListener(this);

        // ONLY ONE CHECKBOX CAN BE SELECTED
        student.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                teacher.setChecked(false);
            }
        });

        teacher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                student.setChecked(false);
            }
        });
    }

    @Override
    public void onClick(View view) {

        if(view==register)
        {
            Intent intent = new Intent(this, Register.class);
            startActivity(intent);
        }
        else
        {
            String Password = password.getText().toString();
            String Id = id.getText().toString();

            if(teacher.isChecked())
            {
                TeacherRef.child(Id).get().addOnCompleteListener(task -> {

                    if(task.isSuccessful())
                    {
                        Teacher t = task.getResult().getValue(Teacher.class);

                        if(t!=null && t.password.equals(Password))
                        {
                            Intent intent = new Intent(Login.this, TeachersLounge.class);
                            String teacherId = id.getText().toString().trim();
                            intent.putExtra("teacherID", teacherId);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(this, "wrong password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if(student.isChecked())
            {
                StudentRef.child(Id).get().addOnCompleteListener(task -> {

                    if(task.isSuccessful())
                    {
                        Student u = task.getResult().getValue(Student.class);

                        if(u!=null && u.password.equals(Password))
                        {
                            Intent intent = new Intent(Login.this, StudentsLounge.class);
                            String Sid = id.getText().toString().trim();
                            intent.putExtra("studentID", Sid);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(this, "wrong password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}