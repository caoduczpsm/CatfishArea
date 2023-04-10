package com.example.catfisharea.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.catfisharea.ultilities.Constants;

import java.io.Serializable;

public class User implements Serializable {

    public String name, image, phone, token, id, address, companyID, position, dateOfBirth;
    public Boolean isSelected = false;
    public static Bitmap getUserImage(String image){
        byte[] bytes = new byte[0];
        if (image != null) {
            bytes = Base64.decode(image, Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static String getNamePosition(String position) {
        switch (position) {
            case Constants.KEY_ADMIN:
                return "Admin";
            case Constants.KEY_ACCOUNTANT:
                return "Kế toán";
            case Constants.KEY_REGIONAL_CHIEF:
                return "Trưởng vùng";
            case Constants.KEY_DIRECTOR:
                return "Trưởng khu";
            case Constants.KEY_WORKER:
                return "Công nhân";
        }
        return null;
    }
}
