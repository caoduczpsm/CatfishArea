package com.example.catfisharea.models;

import com.airbnb.lottie.L;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.List;

public class Pond extends RegionModel {
    private String idCampus;

    private String acreage;
    private GeoPoint geo;
    private int numberWorker = 0;
    private boolean isSelected = false;
    private String idArea;
    private int numOfFeeding;
    private List<String> numOfFeedingList;
    private List<String> amountFeedList;
    private List<String> specificationsToMeasureList;
    private HashMap<String, Object> parameters;

    public Pond(String id, String name) {
        super(id, name);
    }

    public Pond(String id, String name, GeoPoint geo, String idCampus, String acreage, int numOfFeeding,
                List<String> numOfFeedingList, List<String> amountFeedList, List<String> specificationsToMeasureList, HashMap<String, Object> parameters) {
        super(id, name);
        this.geo = geo;
        this.idCampus = idCampus;
        this.acreage = acreage;
        this.numOfFeeding = numOfFeeding;
        this.numOfFeedingList = numOfFeedingList;
        this.amountFeedList = amountFeedList;
        this.specificationsToMeasureList = specificationsToMeasureList;
        this.parameters = parameters;
    }

    public Pond(Pond pond) {
        super(pond.getId(), pond.getName());
        this.geo = pond.getGeo();
        this.idCampus = pond.getIdCampus();
        this.acreage = pond.getAcreage();
        this.numOfFeeding = pond.getNumOfFeeding();
        this.numOfFeedingList = pond.getNumOfFeedingList();
        this.amountFeedList = pond.getAmountFeedList();
        this.specificationsToMeasureList = pond.getSpecificationsToMeasureList();
        this.parameters = pond.getParameters();
    }

    public List<String> getSpecificationsToMeasureList() {
        return specificationsToMeasureList;
    }

    public void setSpecificationsToMeasureList(List<String> specificationsToMeasureList) {
        this.specificationsToMeasureList = specificationsToMeasureList;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public List<String> getAmountFeedList() {
        return amountFeedList;
    }

    public void setAmountFeedList(List<String> amountFeedList) {
        this.amountFeedList = amountFeedList;
    }

    public List<String> getNumOfFeedingList() {
        return numOfFeedingList;
    }

    public void setNumOfFeedingList(List<String> numOfFeedingList) {
        this.numOfFeedingList = numOfFeedingList;
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

    public LatLng getLatLng() {
        return new LatLng(geo.getLatitude(), geo.getLongitude());
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
        return "[" + d1 + "°" + m1 + "'" + s1 + "\"N, " + d2 + "°" + m2 + "'" + s2 + "\"E]";
    }
}
