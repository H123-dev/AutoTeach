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
        this.expectedOutput = expectedOutput;// ex "6" for input "1 2 3"
        this.testCaseCode =input;//asssuming like the entry in both is alr codd so for ex int [] a = new int[]{1,2,3};
    }
}
