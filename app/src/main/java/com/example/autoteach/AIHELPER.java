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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public abstract class AIHELPER {
    static OkHttpClient client = new OkHttpClient();
    public static final String OPENAI_API_KEY = "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";
    public static void runAIModel(Activity a , String prompt ,  Listener listener) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", "gpt-4o");

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

                        String aiText = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        a.runOnUiThread(() -> listener.onSuccess(aiText.trim()));//were the actuall response is sent back

                    } catch (Exception e) {
                        a.runOnUiThread(() -> listener.onFailure("Parse error: " + resBody));
                    }
                }
            });
        } catch (Exception e) {
            listener.onFailure("AI setup error: " + e.getMessage());
        }
    }
}
