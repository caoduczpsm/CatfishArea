package com.example.catfisharea.listeners;

import com.example.catfisharea.models.Request;

public interface RequestListener {
    public void accept(Request request);
    public void refush(Request request);

    public void delete(Request request);

}
