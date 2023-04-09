package com.example.catfisharea.ultilities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EncryptHandler {

    static final String secret = "Catfish_Area";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encryptPassword(String pass) throws NoSuchAlgorithmException, InvalidKeyException {

        // Tạo ra một khóa bí mật từ một chuỗi cố định
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

        // Tạo ra một đối tượng Mac với thuật toán "HmacSHA256"
        Mac hasher = Mac.getInstance("HmacSHA256");
        hasher.init(key);

        // Tạo ra một mảng byte băm từ mật khẩu
        byte[] hash = hasher.doFinal(pass.getBytes());

        // Chuyển đổi mảng byte băm thành chuỗi hexa
        return Base64.getEncoder().encodeToString(hash);
    }

}
