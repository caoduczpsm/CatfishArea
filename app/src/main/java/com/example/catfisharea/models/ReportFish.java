package com.example.catfisharea.models;

public class ReportFish {

    public String id, reporterName, reporterImage, reporterPosition, reporterPhone, reporterId, guess, image, date, pondId, status;
    public boolean isSelected = false;

    public ReportFish(){

    }

    public ReportFish(ReportFish reportFish){
        this.id = reportFish.id;
        this.status = reportFish.status;
        this.reporterName = reportFish.reporterName;
        this.reporterImage = reportFish.reporterImage;
        this.reporterPosition = reportFish.reporterPosition;
        this.reporterPhone = reportFish.reporterPhone;
        this.reporterId = reportFish.reporterId;
        this.guess = reportFish.guess;
        this.image = reportFish.image;
        this.date = reportFish.date;
        this.isSelected = reportFish.isSelected;
        this.pondId = reportFish.pondId;
    }

}
