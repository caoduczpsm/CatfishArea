package com.example.catfisharea.models;

public class RequestLeave extends Request{
    private String dateStart;
    private String dateEnd;
    private String reason;

    public RequestLeave(String id, String name, String note, User sendUser, String dateCreated, String typeRequest, String dateStart, String dateEnd, String reason) {
        super(id, name, note, sendUser, dateCreated, typeRequest);
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.reason = reason;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }
}
