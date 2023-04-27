package com.example.catfisharea.models;

import android.annotation.SuppressLint;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Environment {
    private String id;
    private String planId;
    private Date date;
    private Map<String, String> parameter;
    private long old;

    public Environment(String id, String planId, Date date, Map<String, String> parameter, long old) {
        this.id = id;
        this.planId = planId;
        this.date = date;
        this.parameter = parameter;
        this.old = old;
    }

    public long getOld() {
        return old;
    }

    public void setOld(long old) {
        this.old = old;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @SuppressLint("NewApi")
    public Map<String, String> getParameter() {
        parameter.forEach((key, value) ->{
           if (value.equals("0")) parameter.put(key, "Không đo");
        });
        return parameter;
    }

    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
    }
}
