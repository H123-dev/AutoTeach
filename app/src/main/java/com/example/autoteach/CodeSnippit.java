package com.example.autoteach;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CodeSnippit {
    FirebaseDatabase db;
    DatabaseReference classRef;
    public String base64Image;
    public String codeText;
    public String finalCode;
    public HashMap<String , Boolean> testCaseRes;
    public double grade;

    public CodeSnippit() {
    }
    public CodeSnippit(String og , String ocr , String finalc)
    {
        this.base64Image = og;
        this.codeText = ocr;
        this.finalCode = finalc;
        this.testCaseRes = new HashMap<String, Boolean>();
        this.grade = 0;//temp
    }
    public void calculateGrade(Activity a , String id , Listener listener)
    {
        db = FirebaseDatabase.getInstance();
        classRef = db.getReference("Classes");
        classRef.child(id).get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DataSnapshot snapshot = task.getResult();
                if(snapshot.exists())
                {
                    Classroom classRoom = snapshot.getValue(Classroom.class);
                    if(classRoom!=null)
                    {
                        ArrayList<TestCase> testCases = classRoom.testCases;
                        final int[] count = {0};
                        for(TestCase tc : testCases)
                        {
                            AIHELPER.checkTestCase(a, this.finalCode, tc.input, tc.expectedOutput, new Listener() {
                                @Override
                                public void onSuccess(String result) {
                                    if(result.equals("PASSED"))
                                    {
                                        testCaseRes.put(tc.input , true);
                                        count[0]++;
                                        grade += 100.0/3;
                                        if(count[0]==3)
                                            listener.onSuccess("Graded");
                                    }
                                    else
                                    {
                                        testCaseRes.put(tc.input , false);
                                        count[0]++;
                                        if(count[0]==3)
                                            listener.onSuccess("Graded");
                                    }
                                }

                                @Override
                                public void onFailure(String errorMessage) {

                                }
                            });
                        }
                    }
                }
            }
        });
    }

}
