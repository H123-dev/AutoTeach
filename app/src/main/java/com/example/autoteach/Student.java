package com.example.autoteach;
import java.util.*;
//מחלקת התלמיד
public class Student extends User{
    public HashMap<String,Double> submissions; // classID , grade
    public Student() {//פעולה בונה ריקה בשביל FIREBASE
    }
    public Student(String name, String password, String id) {
        super(name, password, id);
        this.submissions = new HashMap<>();
    }
}
