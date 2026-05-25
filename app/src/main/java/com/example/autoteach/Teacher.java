package com.example.autoteach;
import java.util.*;
//מחלקת המורה
public class Teacher extends User
{
    public ArrayList<String> classIDs;
    public Teacher() {//פעולה בונה ריקה בשביל FIREBASE
    }
    public Teacher(String name, String password, String id) {
        super(name, password, id);
        this.classIDs = new ArrayList<>();
    }
}
