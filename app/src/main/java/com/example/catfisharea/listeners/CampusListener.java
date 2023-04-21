package com.example.catfisharea.listeners;

import com.example.catfisharea.models.RegionModel;

public interface CampusListener {

    void OnCampusClicker(RegionModel regionModel);
    void OnPondClicker(RegionModel regionModel);
    void onCreatePlan(RegionModel regionModel);
}
