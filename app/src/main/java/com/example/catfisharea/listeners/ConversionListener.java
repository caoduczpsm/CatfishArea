package com.example.catfisharea.listeners;

import com.example.catfisharea.models.Group;
import com.example.catfisharea.models.User;

public interface ConversionListener {
    void onConversionClicker(User user);
    void onConversionClicker(Group group);
}

