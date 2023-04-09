package com.example.catfisharea.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {

    public String title, creatorID, creatorName, creatorImage, creatorPhone,
            id, taskContent, dayOfStart, dayOfEnd, status, typeOfTask;
    public ArrayList<String> receiverID, receiverName, receiverPhone, receiverImage, receiversCompleted;
    public boolean isSelected = false;

}
