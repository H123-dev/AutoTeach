package com.example.autoteach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Base64;

public class Helper {

    public static boolean checkPass(String pass)
    {
        return pass.length()>=6 && pass.matches(".*[A-Z].*") && pass.matches(".*[a-z].*") && pass.matches(".*[0-9].*");
    }
    public static boolean checkID(String id)
    {
        return id.length()==9 && id.matches("[0-9]+");
    }

    public static class ImageUtils {
        // Convert Uri to byte array
        public static byte[] uriToBytes(Context context, Uri uri) throws IOException {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            inputStream.close();
            return buffer.toByteArray();
        }
        public static String bytesToBase64(byte[] bytes) {
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }
    }


}
