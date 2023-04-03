package com.example.catfisharea.models;

public class Plan {
    private String planId;
    private String pondId;
    private String pondName; // tên ao
    private String acreage = "0";  // diện tích nước (m2)
    private String date;
    private int consistence = 0; // mật độ thả (con/m2)
    private int numberOfFish = 0;   //Số lượng cá thả (con)
    private float survivalRate = 0; // tỷ lệ sống (%)
    private int numberOfFishAlive = 0;  // số lượng cá còn (con)
    private float harvestSize = 0;  //size thu hoạch (kg/ con)
    private int harvestYield = 0;   //sản lương thu hoạch (kg)
    private float fcr = 0;  // FCR
    private int food = 0;   // Thức ăn/ vụ nuôi (kg)
    private int fingerlingSamples = 0; // Mẫu cá con/kg

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getConsistence() {
        return consistence;
    }

    public void setConsistence(int consistence) {
        this.consistence = consistence;
    }

    public int getNumberOfFish() {
        return numberOfFish;
    }

    public void setNumberOfFish(int numberOfFish) {
        this.numberOfFish = numberOfFish;
    }

    public float getSurvivalRate() {
        return survivalRate;
    }

    public void setSurvivalRate(float survivalRate) {
        this.survivalRate = survivalRate;
    }

    public int getNumberOfFishAlive() {
        return numberOfFishAlive;
    }

    public void setNumberOfFishAlive(int numberOfFishAlive) {
        this.numberOfFishAlive = numberOfFishAlive;
    }

    public float getHarvestSize() {
        return harvestSize;
    }

    public void setHarvestSize(float harvestSize) {
        this.harvestSize = harvestSize;
    }

    public int getHarvestYield() {
        return harvestYield;
    }

    public void setHarvestYield(int harvestYield) {
        this.harvestYield = harvestYield;
    }

    public float getFcr() {
        return fcr;
    }

    public void setFcr(float fcr) {
        this.fcr = fcr;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getFingerlingSamples() {
        return fingerlingSamples;
    }

    public void setFingerlingSamples(int fingerlingSamples) {
        this.fingerlingSamples = fingerlingSamples;
    }

    public String getPondId() {
        return pondId;
    }

    public void setPondId(String pondId) {
        this.pondId = pondId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }
}
