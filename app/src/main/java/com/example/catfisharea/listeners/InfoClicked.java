package com.example.catfisharea.listeners;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;


public interface InfoClicked {
    public void clickItem(LatLng latLng, List<LatLng> listLatlng);
    public void clickEditItem(Object item);
}
