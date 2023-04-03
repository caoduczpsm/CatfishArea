package com.example.catfisharea.models;

import com.google.firebase.firestore.GeoPoint;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class Campus extends RegionModel {
    private String idArea;
    private List<GeoPoint> geoList;

    private int numberPond = 0;
    private boolean isSelected = false;

    public Campus(String id, String name, List<GeoPoint> geoList, String idArea) {
        super(id, name);
        this.geoList = geoList;
        this.idArea = idArea;
    }

    public int getNumberPond() {
        return numberPond;
    }

    public void setNumberPond(int numberPond) {
        this.numberPond = numberPond;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getIdArea() {
        return idArea;
    }

    public void setIdArea(String idArea) {
        this.idArea = idArea;
    }

    public List<GeoPoint> getGeoList() {
        return geoList;
    }

    public void setGeoList(List<GeoPoint> geoList) {
        this.geoList = geoList;
    }

    public List<LatLng> getListLatLng(){
        List<LatLng> listpoint = new ArrayList<>();
        for (GeoPoint point: geoList){
            listpoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return listpoint;
    }

    public LatLng getPolygonCenterPoint() {
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (GeoPoint lt : geoList) {
            builder.include(new LatLng(lt.getLatitude(), lt.getLongitude()));
        }
        LatLngBounds bounds = builder.build();
        centerLatLng = bounds.getCenter();
        return centerLatLng;
    }

    public String getDMStoDec(double lat, double longt) {
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
