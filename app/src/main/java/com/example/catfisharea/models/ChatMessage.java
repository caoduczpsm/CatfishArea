package com.example.catfisharea.models;

import com.example.catfisharea.ultilities.Constants;

import java.util.Date;

public class ChatMessage {
    public String type;
    public String id;
    public String senderId;
    public String receiverId;
    public String message;
    public String dateTime;
    public Date dataObject;
    public String conversionId, conversionName, conversionImage;
    public Boolean isSelected = false, isSeen = false;
    public String model;
    public Boolean lastReceiver = false;

    public ChatMessage() {
        type = Constants.MESSAGE_TEXT;
    }
}
