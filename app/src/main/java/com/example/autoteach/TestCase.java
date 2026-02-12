package com.example.autoteach;

public class TestCase
{
    public String input;
    public String expectedOutput;

    public TestCase() {
    }

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;// ex "6" for input "1 2 3"
    }
}
