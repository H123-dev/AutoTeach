package com.example.autoteach;
import java.util.*;
public class Student extends User{
    public HashMap<String,Double> submissions; // classID , grade
    public Student() {
    }
    public Student(String name, String password, String id) {
        super(name, password, id);
        this.submissions = new HashMap<>();
    }
}
