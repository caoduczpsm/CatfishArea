package com.example.catfisharea.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Pond extends RegionModel {
    private String idCampus;

    private String acreage;
    private GeoPoint geo;
    private int numberWorker = 0;
    private boolean isSelected = false;
    private String idArea;
    private int numOfFeeding;

    public int getSt1Feeding() {
        return st1Feeding;
    }

    private int st1Feeding, st2Feeding, st3Feeding, st4Feeding, st5Feeding, st6Feeding, st7Feeding, st8Feeding;

    public Pond(String id, String name) {
        super(id, name);
    }

    public Pond(String id, String name, GeoPoint geo, String idCampus, String acreage, int numOfFeeding, List<Integer> numOfFeedingList) {
        super(id, name);
        this.geo = geo;
        this.idCampus = idCampus;
        this.acreage = acreage;
        this.numOfFeeding = numOfFeeding;
        this.st1Feeding = numOfFeedingList.get(0);
        this.st2Feeding = numOfFeedingList.get(1);
        this.st3Feeding = numOfFeedingList.get(2);
        this.st4Feeding = numOfFeedingList.get(3);
        this.st5Feeding = numOfFeedingList.get(4);
        this.st6Feeding = numOfFeedingList.get(5);
        this.st7Feeding = numOfFeedingList.get(6);
        this.st8Feeding = numOfFeedingList.get(7);

    }

    public void setSt1Feeding(int st1Feeding) {
        this.st1Feeding = st1Feeding;
    }

    public int getSt2Feeding() {
        return st2Feeding;
    }

    public void setSt2Feeding(int st2Feeding) {
        this.st2Feeding = st2Feeding;
    }

    public int getSt3Feeding() {
        return st3Feeding;
    }

    public void setSt3Feeding(int st3Feeding) {
        this.st3Feeding = st3Feeding;
    }

    public int getSt4Feeding() {
        return st4Feeding;
    }

    public void setSt4Feeding(int st4Feeding) {
        this.st4Feeding = st4Feeding;
    }

    public int getSt5Feeding() {
        return st5Feeding;
    }

    public void setSt5Feeding(int st5Feeding) {
        this.st5Feeding = st5Feeding;
    }

    public int getSt6Feeding() {
        return st6Feeding;
    }

    public void setSt6Feeding(int st6Feeding) {
        this.st6Feeding = st6Feeding;
    }

    public int getSt7Feeding() {
        return st7Feeding;
    }

    public void setSt7Feeding(int st7Feeding) {
        this.st7Feeding = st7Feeding;
    }

    public int getSt8Feeding() {
        return st8Feeding;
    }

    public void setSt8Feeding(int st8Feeding) {
        this.st8Feeding = st8Feeding;
    }

    public String getIdCampus() {
        return idCampus;
    }

    public void setIdCampus(String idCampus) {
        this.idCampus = idCampus;
    }

    public GeoPoint getGeo() {
        return geo;
    }

    public void setGeo(GeoPoint geo) {
        this.geo = geo;
    }

    public int getNumberWorker() {
        return numberWorker;
    }

    public void setNumberWorker(int numberWorker) {
        this.numberWorker = numberWorker;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getNumOfFeeding() {
        return numOfFeeding;
    }

    public void setNumOfFeeding(int numOfFeeding) {
        this.numOfFeeding = numOfFeeding;
    }

    public String getIdArea() {
        return idArea;
    }

    public void setIdArea(String idArea) {
        this.idArea = idArea;
    }

    public String getAcreage() {
        return acreage;
    }

    public void setAcreage(String acreage) {
        this.acreage = acreage;
    }

    public String getDMStoDec() {
        double lat = geo.getLatitude();
        double longt = geo.getLongitude();
        int d1 = (int) lat;
        int m1 = (int) ((lat - d1) * 60);
        float s1 = (float) ((lat - d1 - m1 / 60.00) * 3600);
        s1 = (float) (Math.ceil(s1 * 100) / 100);
        int d2 = (int) longt;
        int m2 = ((int) (longt - d2)) * 60;
        ;
        float s2 = (float) ((longt - d2 - m2 / 60.00) * 3600);
        s2 = (float) (Math.ceil(s2 * 100) / 100);
        String result = "[" + d1 + "°" + m1 + "'" + s1 + "\"N, " + d2 + "°" + m2 + "'" + s2 + "\"E]";
        return result;
    }
}
