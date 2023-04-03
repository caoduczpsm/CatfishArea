package com.example.catfisharea.models;

import java.util.ArrayList;

public class ImportRequest extends Request{
    private ArrayList<Materials> materials;

    public ImportRequest(String id, String name, String note, User requeseter, String dateCreated, String typeRequest, ArrayList<Materials> materials) {
        super(id, name, note, requeseter, dateCreated, typeRequest);
        this.materials = materials;
    }

    public ArrayList<Materials> getMaterials() {
        return materials;
    }

    public void setMaterials(ArrayList<Materials> materials) {
        this.materials = materials;
    }
}
