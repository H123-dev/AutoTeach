package com.example.autoteach;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CodeSnippit {
    public String base64Image;
    public String codeText;
    public String finalCode;
    public HashMap<String , Boolean> testCaseRes;
    double grade;

    public CodeSnippit() {
    }
    public CodeSnippit(String og , String ocr , String finalc , String className)
    {
        this.base64Image = og;
        this.codeText = ocr;
        this.finalCode = finalc;
        this.testCaseRes = new HashMap<String, Boolean>();
        this.grade = calculateGrade(className);
    }
    private double calculateGrade(String id)
    {
        return 1.0;
    }
}
