package com.example.autoteach;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import android.app.Activity;

public abstract class AIHELPER {

    static OkHttpClient client = new OkHttpClient();
    public static final String OPENAI_API_KEY = "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";


    public static void runAIModel(Activity a, String prompt, Listener listener) {//פונקצייה הזאת אחראית על להריץ את המודל של OPENAI עם הPROMPT שניתן ואז להחזיר את התשובה של המודל דרך הLISTENER
        try {
            JSONObject body = new JSONObject();
            body.put("model", "gpt-4.1-mini");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            body.put("messages", messages);

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    a.runOnUiThread(() -> listener.onFailure(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resBody = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject json = new JSONObject(resBody);
                        if (json.has("error")) {
                            String errMsg = json.getJSONObject("error").optString("message", "Unknown AI error");
                            a.runOnUiThread(() -> listener.onFailure(errMsg));
                            return;
                        }
                        String aiText = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        a.runOnUiThread(() -> listener.onSuccess(aiText.trim()));
                    } catch (Exception e) {
                        a.runOnUiThread(() -> listener.onFailure("Parse error: " + resBody));
                    }
                }
            });
        } catch (Exception e) {
            listener.onFailure("AI setup error: " + e.getMessage());
        }
    }

    public static void runCodeOnJudge0(Activity a, String code, Listener listener) {//פונקצייה הזאת אחראית על להריץ את הקוד הניתן בספרייה חיצונית בשם JUDGE0 ואז להחזיר את הOUTPUT של הקוד
        try {
            JSONObject json = new JSONObject();
            json.put("language_id", 62);
            json.put("source_code", code);
            json.put("stdin", "");
            json.put("base64_encoded", false);
            json.put("cpu_time_limit", 2);
            json.put("memory_limit", 51200);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://ce.judge0.com/submissions?wait=true")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    a.runOnUiThread(() -> listener.onFailure("Request failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        a.runOnUiThread(() -> listener.onFailure("Server error: " + response.code()));
                        return;
                    }

                    String bodyStr = response.body() != null ? response.body().string() : "";

                    try {
                        JSONObject resJson = new JSONObject(bodyStr);
                        String stdout = resJson.optString("stdout", "").trim();
                        String stderr = resJson.optString("stderr", "");
                        String compileOutput = resJson.optString("compile_output", "");
                        String status = resJson.optJSONObject("status").optString("description", "");

                        a.runOnUiThread(() -> {
                            if (!compileOutput.isEmpty() && !"null".equals(compileOutput)) {
                                listener.onFailure("Compilation Error:\n" + compileOutput);
                            } else if (!stderr.isEmpty() && !"null".equals(stderr)) {
                                listener.onFailure("Runtime Error:\n" + stderr);
                            } else if (!stdout.isEmpty()) {
                                listener.onSuccess(stdout);
                            } else if (!"Accepted".equals(status)) {
                                listener.onFailure("Error: " + status);
                            } else {
                                listener.onFailure("Unknown execution error.");
                            }
                        });
                    } catch (Exception e) {
                        a.runOnUiThread(() -> listener.onFailure("Parse error: " + bodyStr));
                    }
                }
            });
        } catch (Exception e) {
            listener.onFailure("Setup error: " + e.getMessage());
        }
    }

    public static void checkTestCase(Activity a, String code, String input, String expectedOutput, Listener listener) {//פונקצייה הזאת אחראית על לבדוק את הקוד שניתן מול TEST CASE מסויים, היא עושה את זה ע"י להוסיף פונקציית MAIN לקוד שניתן ואז להריץ את הקוד עם הINPUT של הTEST CASE ולבדוק אם הOUTPUT שווה לEXPECTED OUTPUT

        String promptCODE =
                "You are given Java code that compiles but does not contain a main method. " +
                        "Your task is to add a valid Java main method. " +
                        "Do NOT modify existing methods. Only add a main method. " +
                        "Preserve the original class name exactly as given. " +
                        "Extract numeric values from the input and map them to function parameters in order. " +
                        "Call the appropriate function and print the return value using System.out.println. " +
                        "Return the FULL Java class code. " +
                        "Output ONLY valid Java code. " +
                        "Do NOT include explanations, comments, markdown, or code blocks.\n\n" +

                        "EXAMPLE:\n" +
                        "Given code:\n" +
                        "public class Main {\n" +
                        "    public static int add(int a, int b) {\n" +
                        "        return a + b;\n" +
                        "    }\n" +
                        "}\n\n" +
                        "Input:\n\"check 1 and 7\"\n\n" +
                        "Output:\n" +
                        "public class Main {\n" +
                        "    public static int add(int a, int b) {\n" +
                        "        return a + b;\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int a = 1;\n" +
                        "        int b = 7;\n" +
                        "        int output = add(a, b);\n" +
                        "        System.out.println(output);\n" +
                        "    }\n" +
                        "}\n\n" +

                        "NOW DO THIS FOR:\n" +
                        "Given code:\n" + code + "\n\n" +
                        "Input:\n\"" + input + "\"";

        runAIModel(a, promptCODE, new Listener() {
            @Override
            public void onSuccess(String generatedCode) {
                runCodeOnJudge0(a, generatedCode, new Listener() {
                    @Override
                    public void onSuccess(String programOutput) {
                        String promptTC =
                                "You are comparing program output with expected output. " +
                                        "Ignore differences in whitespace, spacing around commas, and line breaks. " +
                                        "Treat outputs as equal if they contain the same values in the same order. " +
                                        "Respond with EXACTLY one word: PASSED or FAILED. " +
                                        "Do not include punctuation, explanations, markdown, or extra text.\n\n" +
                                        "Program output:\n" + programOutput + "\n\n" +
                                        "Expected output:\n" + expectedOutput;
                        runAIModel(a, promptTC, listener);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        listener.onFailure("Code execution failed: " + errorMessage);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                listener.onFailure("Failed to generate main method: " + errorMessage);
            }
        });
    }
}
