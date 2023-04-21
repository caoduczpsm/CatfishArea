package com.example.catfisharea.models;

import java.util.HashMap;
import java.util.List;

public class Treatment {
   public String id, pondId, campusId, creatorId, creatorImage, creatorName,
           creatorPhone, replaceWater, noFood, suckMud, date, note, sickName, status;
   public List<String> receiverIds, receiverImages, receiverNames, receiverPhones;
   public HashMap<String, Object> medicines;
}
