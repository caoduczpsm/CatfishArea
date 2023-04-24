package com.example.catfisharea.models;

import java.util.Map;

public class Warehouse {
    private String id;
    private String name;
    private String areaId;
    private String campusId;
    private String acreage;
    private String description;
    private String pondId;
    private String pondName;
    private Map<String, Integer> category;

    public Warehouse(String id, String name, String areaId, String campusId, String acreage, String description) {
        this.id = id;
        this.name = name;
        this.areaId = areaId;
        this.campusId = campusId;
        this.acreage = acreage;
        this.description = description;
    }

    public String getPondName() {
        return pondName;
    }

    public void setPondName(String pondName) {
        this.pondName = pondName;
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

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getCampusId() {
        return campusId;
    }

    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    public String getPondId() {
        return pondId;
    }

    public void setPondId(String pondId) {
        this.pondId = pondId;
    }

    public String getAcreage() {
        return acreage;
    }

    public void setAcreage(String acreage) {
        this.acreage = acreage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
