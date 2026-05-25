package com.example.autoteach;

//מחלקה אבסטרקטית של משתמש, כל המשתמשים יורשים ממנה

public abstract class User
{
    public String name;
    public String password;
    public String id;
    public User () {//פעולה בונה בשביל FIREBASE
    }
    public User (String name, String password, String id) {
        this.name = name;
        this.password = password;
        this.id = id;
    }

}
