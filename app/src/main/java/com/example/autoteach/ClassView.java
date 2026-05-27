package com.example.autoteach;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
//דף שמציג את הציונים של התלמידים בכיתה מסוימת, המורה יכול לראות את הציונים של כל התלמידים בכיתה  המידע מוצג בצורה של רשימה עם תז התלמיד והציון שלו.
public class ClassView extends AppCompatActivity {
    String classID;
    FirebaseDatabase db;
    DatabaseReference classRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_view);
        LinearLayout classesContainer = findViewById(R.id.classes_container);
        db = FirebaseDatabase.getInstance();
        classRef = db.getReference("Classes");
        classID = getIntent().getStringExtra("ClassID");
        if (classID == null || classID.isEmpty()) {
            finish();
        }
        classRef.child(classID).get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                Classroom classRoom = task.getResult().getValue(Classroom.class);
                if(classRoom!=null)
                {
                    if(classRoom.studentsSubmissions==null)
                    {
                        classRoom.studentsSubmissions = new HashMap<>();
                    }
                    HashMap<String, CodeSnippit> grades = classRoom.studentsSubmissions;
                    for(String studentId : grades.keySet())
                    {
                        CodeSnippit snippit = grades.get(studentId);
                        if(snippit==null)
                        {
                            continue;
                        }
                        TextView studentGrade = new TextView(this);
                        studentGrade.setText(studentId + " : " + snippit.grade);
                        studentGrade.setTextSize(18);
                        studentGrade.setTextColor(Color.parseColor("#3949AB"));
                        studentGrade.setBackgroundColor(Color.parseColor("#E8EAF6"));
                        studentGrade.setPadding(12, 12, 12, 12);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 12);
                        studentGrade.setLayoutParams(params);
                        classesContainer.addView(studentGrade);
                    }
                }
            }
        });

    }
}