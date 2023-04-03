package com.example.catfisharea.models;

import java.util.Date;

public class Comment {
    public String type;
    public String id;
    public String userCommentId;
    public String userCommentName;
    public String userCommentImage;
    public String userCommentPosition;
    public String taskId;
    public String content;
    public String dateTime;
    public Date dataObject;
    public Boolean isSelected = false, isSeen = false;
}
