package com.example.autoteach;

public abstract class User
{
    public String name;
    public String password;
    public String id;
    public User () {
    }
    public User (String name, String password, String id) {
        this.name = name;
        this.password = password;
        this.id = id;
    }

}
