package com.example.autoteach;

//מחלקה שמאחסנת את המידע של כל מבחן, כולל הקלט והפלט הצפוי.

public class TestCase
{
    public String input;
    public String expectedOutput;

    public TestCase() {//פעולה בונה ריקה בשביל FIREBASE
    }

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;// ex "6" for input "1 2 3"
    }
}
