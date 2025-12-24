package com.example.autoteach;

public class TestCase
{
    public String input;
    public String expectedOutput;
    public String testCaseCode;


    public TestCase() {
    }

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.testCaseCode = turnRunable(input);
    }
    private String turnRunable(String testCaseCode)
    {
        // process text to make it runable code
        return "runable code";
    }
}
