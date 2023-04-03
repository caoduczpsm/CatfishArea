package com.example.catfisharea.models;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    public String name, image, id, token, description;
    public List<String> member;
}

