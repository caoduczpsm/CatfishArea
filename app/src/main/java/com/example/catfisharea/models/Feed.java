package com.example.catfisharea.models;

import java.util.Date;
import java.util.List;

public class Feed {
    private String id;
    private String planId;
    private Date date;
    private List<String> amountFed;
    private long old;
    private long totalFood;

    public Feed(String id, String planId, Date date, List<String> amountFed) {
        this.id = id;
        this.planId = planId;
        this.date = date;
        this.amountFed = amountFed;
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

    public List<String> getAmountFed() {
        return amountFed;
    }

    public void setAmountFed(List<String> amountFed) {
        this.amountFed = amountFed;
    }

    public long getOld() {
        return old;
    }

    public void setOld(long old) {
        this.old = old;
    }

    public long getTotalFood() {
        return totalFood;
    }

    public void setTotalFood(long totalFood) {
        this.totalFood = totalFood;
    }

    public String sumFood() {
        long total = 0;
        for (String item: amountFed) {
            total += Long.parseLong(item);
        }
        return String.valueOf(total);
    }
}