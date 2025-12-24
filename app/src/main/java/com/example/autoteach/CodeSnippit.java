package com.example.autoteach;
import java.util.*;

public class CodeSnippit {
    public byte[] ogImageBytes;
    public String codeText;
    public String finalCode;
    public HashMap<Integer , Boolean> testCaseRes;//t0t1t2
    double grade;

    public CodeSnippit() {
    }
    public CodeSnippit(byte [] og )
    {
        this.ogImageBytes = og;
        this.codeText = picToText();
        this.finalCode = textToRunable(codeText);
        this.testCaseRes = new HashMap<Integer, Boolean>();
        this.grade = calculateGrade();
    }
    private double calculateGrade()
    {
        return 1.0;
    }
    private String picToText()
    {
        // use OCR to convert image to text
        return "converted text";
    }
    private String textToRunable(String codeText)
    {
        // process text to make it runable code
        return "runable code";
    }



}
