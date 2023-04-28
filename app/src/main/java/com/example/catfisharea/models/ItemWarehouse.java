package com.example.catfisharea.models;

import java.util.List;

public class ItemWarehouse {
    private RegionModel regionModel;
    private List<Warehouse> warehouseList;

    public RegionModel getRegionModel() {
        return regionModel;
    }

    public void setRegionModel(RegionModel regionModel) {
        this.regionModel = regionModel;
    }

    public List<Warehouse> getWarehouseList() {
        return warehouseList;
    }

    public void setWarehouseList(List<Warehouse> warehouseList) {
        this.warehouseList = warehouseList;
    }
}
