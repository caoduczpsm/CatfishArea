package com.example.catfisharea.models;

import java.util.List;

public class ItemHome {
    private RegionModel regionModel;
    private List<RegionModel> reginonList;

    public RegionModel getRegionModel() {
        return regionModel;
    }

    public void setRegionModel(RegionModel regionModel) {
        this.regionModel = regionModel;
    }

    public List<RegionModel> getReginonList() {
        return reginonList;
    }

    public void setReginonList(List<RegionModel> reginonList) {
        this.reginonList = reginonList;
    }
}
