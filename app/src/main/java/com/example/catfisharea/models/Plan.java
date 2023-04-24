package com.example.catfisharea.models;

import java.util.Date;

public class Plan {
    private String planId;
    private String pondId;
    private String pondName; // tên ao
    private String acreage = "0";  // diện tích nước (m2)
    private Date date;
    private long numberOfFish = 0;   //Số lượng cá thả (con)
    private long fishWeight = 0;
    private long numberOfDeadFish = 0;
    private long LKnumberOfDeadFish = 0;
    private float survivalRate = 0; // tỷ lệ sống (%)
    private long numberOfFishAlive = 0;  // số lượng cá còn (con)
    private long food = 0;   // Thức ăn/ vụ nuôi (kg)
    private long old = 0;
    private long totalFood = 0;
    private long AVG = 0;

    public long getAVG() {
        return AVG;
    }

    public void setAVG(long AVG) {
        this.AVG = AVG;
    }

    public long getTotalFood() {
        return totalFood;
    }

    public void setTotalFood(long totalFood) {
        this.totalFood = totalFood;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPondId() {
        return pondId;
    }

    public void setPondId(String pondId) {
        this.pondId = pondId;
    }

    public String getPondName() {
        return pondName;
    }

    public void setPondName(String pondName) {
        this.pondName = pondName;
    }

    public String getAcreage() {
        return acreage;
    }

    public void setAcreage(String acreage) {
        this.acreage = acreage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getNumberOfFish() {
        return numberOfFish;
    }

    public void setNumberOfFish(long numberOfFish) {
        this.numberOfFish = numberOfFish;
    }

    public long getFishWeight() {
        return fishWeight;
    }

    public void setFishWeight(long fishWeight) {
        this.fishWeight = fishWeight;
    }

    public long getNumberOfDeadFish() {
        return numberOfDeadFish;
    }

    public void setNumberOfDeadFish(long numberOfDeadFish) {
        this.numberOfDeadFish = numberOfDeadFish;
    }

    public long getLKnumberOfDeadFish() {
        return LKnumberOfDeadFish;
    }

    public void setLKnumberOfDeadFish(long LKnumberOfDeadFish) {
        this.LKnumberOfDeadFish = LKnumberOfDeadFish;
    }

    public float getSurvivalRate() {
        return survivalRate;
    }

    public void setSurvivalRate(float survivalRate) {
        this.survivalRate = survivalRate;
    }

    public long getNumberOfFishAlive() {
        return numberOfFishAlive;
    }

    public void setNumberOfFishAlive(long numberOfFishAlive) {
        this.numberOfFishAlive = numberOfFishAlive;
    }

    public long getFood() {
        return food;
    }

    public void setFood(long food) {
        this.food = food;
    }


    public long getOld() {
        return old;
    }

    public void setOld(long old) {
        this.old = old;
    }
}
