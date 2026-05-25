package com.example.autoteach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Base64;

//מחלקת עזר כללית
public class Helper {

    //פונקציית עזר לבדיקת סיסמה
    public static boolean checkPass(String pass)
    {
        return pass.length()>=6 && pass.matches(".*[A-Z].*") && pass.matches(".*[a-z].*") && pass.matches(".*[0-9].*");
    }
    //פונקציית עזר לבדיקת תעודת זהות
    public static boolean checkID(String id)
    {
        return id.length()==9 && id.matches("[0-9]+");
    }
}
