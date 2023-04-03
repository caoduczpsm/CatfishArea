package com.example.catfisharea.models;

import com.example.catfisharea.ultilities.Constants;

public class Request {
    private String id;
    private String name;
    private String note = "";
    private User requeseter;
    private String dateCreated;
    private String typeRequest;
    private String status = Constants.KEY_PENDING;

    public Request(String id, String name, String note, User requeseter, String dateCreated, String typeRequest) {
        this.id = id;
        this.name = name;
        this.note = note;
        this.requeseter = requeseter;
        this.dateCreated = dateCreated;
        this.typeRequest = typeRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getRequeseter() {
        return requeseter;
    }

    public void setRequeseter(User requeseter) {
        this.requeseter = requeseter;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTypeRequest() {
        return typeRequest;
    }

    public void setTypeRequest(String typeRequest) {
        this.typeRequest = typeRequest;
    }
}
