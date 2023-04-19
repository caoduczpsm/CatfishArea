package com.example.catfisharea.models;

import java.util.Date;
import java.util.List;

public class WarehouseHistory {
    private String id;
    private Date date;
    private List<Category> mCategory;
    private String warehouseID;
    private String total;

    public WarehouseHistory(String id, String warehouseID) {
        this.id = id;
        this.warehouseID = warehouseID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Category> getmCategory() {
        return mCategory;
    }

    public void setmCategory(List<Category> mCategory) {
        this.mCategory = mCategory;
    }

    public String getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        this.warehouseID = warehouseID;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
