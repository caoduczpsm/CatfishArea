package com.example.catfisharea.listeners;

import com.example.catfisharea.models.Pond;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;


public interface InfoClicked {
    public void clickItem(LatLng latLng, List<LatLng> listLatlng);
    public void clickEditItem(Object item);
    public void clickPond(LatLng point);
}
