package com.example.autoteach;
import java.util.*;
public class Teacher extends User
{
    public ArrayList<String> classIDs;
    public Teacher() {
    }
    public Teacher(String name, String password, String id) {
        super(name, password, id);
        this.classIDs = new ArrayList<>();
    }
}
