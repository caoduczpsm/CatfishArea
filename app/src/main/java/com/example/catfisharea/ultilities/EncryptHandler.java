package com.example.catfisharea.ultilities;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.RequiresApi;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@SuppressLint("GetInstance")
public class EncryptHandler {

    static final String secret = "Catfish_Area";
    // Tạo ra một khóa bí mật từ một chuỗi cố định
    static SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encryptPassword(String pass) throws NoSuchAlgorithmException, InvalidKeyException {

        // Tạo ra một đối tượng Mac với thuật toán "HmacSHA256"
        Mac hasher = Mac.getInstance("HmacSHA256");
        hasher.init(key);

        // Tạo ra một mảng byte băm từ mật khẩu
        byte[] hash = hasher.doFinal(pass.getBytes());

        // Chuyển đổi mảng byte băm thành chuỗi hexa
        return Base64.getEncoder().encodeToString(hash);
    }

    // Hàm mã hóa AES
    public static String encryptAESMessage(String message, String key){
        try{
            Cipher cipher = Cipher.getInstance("AES");
            byte[] keyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] messageBytes = message.getBytes();
            byte[] encryptedBytes = cipher.doFinal(messageBytes);
            return bytesToHex(encryptedBytes);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }

    // Hàm giải mã AES
    public static String decryptAESMessage(String message, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] keyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] messageBytes = hexToBytes(message);
            byte[] decryptedBytes = cipher.doFinal(messageBytes);
            return new String(decryptedBytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }

    // Hàm chuyển đổi từ mảng byte sang chuỗi dạng hexa
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Hàm chuyển đổi từ chuỗi dạng hexa sang mảng byte
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

}
