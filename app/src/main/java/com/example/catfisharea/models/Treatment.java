package com.example.catfisharea.models;

import java.util.HashMap;
import java.util.List;

public class Treatment {
   public String id, pondId, campusId, creatorId, creatorImage, creatorName,
           creatorPhone, replaceWater, noFood, suckMud, date, note, sickName, status, reportFishId;
   public List<String> receiverIds, receiverImages, receiverNames, receiverPhones;
   public HashMap<String, Object> medicines;

   public Treatment(){}

   public Treatment(Treatment treatment){
      this.id = treatment.id;
      this.pondId = treatment.pondId;
      this.campusId = treatment.campusId;
      this.creatorId = treatment.creatorId;
      this.creatorImage = treatment.creatorImage;
      this.creatorName = treatment.creatorName;
      this.creatorPhone = treatment.creatorPhone;
      this.replaceWater = treatment.replaceWater;
      this.noFood = treatment.noFood;
      this.suckMud = treatment.suckMud;
      this.date = treatment.date;
      this.note = treatment.note;
      this.sickName = treatment.sickName;
      this.status = treatment.status;
      this.reportFishId = treatment.reportFishId;
      this.receiverIds = treatment.receiverIds;
      this.receiverImages = treatment.receiverImages;
      this.receiverNames = treatment.receiverNames;
      this.receiverPhones = treatment.receiverPhones;
      this.medicines = treatment.medicines;
   }
}
