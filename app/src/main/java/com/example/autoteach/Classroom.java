package com.example.autoteach;
import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Classroom {
    public String classID;
    public String teacherID;
    public HashMap<String ,CodeSnippit> studentsSubmissions; // studentID , codeSnippit
    public ArrayList<TestCase>testCases;
    public Classroom() {
    }
    public Classroom(String teacher, ArrayList<TestCase> testCases , String className)  {
        this.teacherID = teacher;
        this.studentsSubmissions = new HashMap<>();
        this.testCases = testCases;
        this.classID = className;
    }


}
