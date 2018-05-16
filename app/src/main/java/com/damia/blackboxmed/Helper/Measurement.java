package com.damia.blackboxmed.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Measurement implements Serializable, Comparable<Measurement>{

    int id;
    String type;
    String unit;
    int value;
    String createdAt;
    String img_res;


    public Measurement(){}

    public Measurement(int id, String type, String unit, int value, String createdAt){
        this.id = id;
        this.createdAt = createdAt;
        this.type = type;
        this.unit = unit;
        this.value = value;
    }

    public Measurement( String type, String unit, int value, String createdAt){
        this.createdAt = createdAt;
        this.type = type;
        this.unit = unit;
        this.value = value;
    }

    public Measurement( String type, String unit, int value, String createdAt, String img_res){
        this.createdAt = createdAt;
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.img_res = img_res;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public int getValue() {
        return value;
    }
    public String getType() {
        return type;
    }
    public String getUnit() {
        return unit;
    }
    public String getImg_res() {
        return img_res;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public void setImg_res(String img_res) { this.img_res = img_res; }

    @Override
    public int compareTo(Measurement m) {
        return getCreatedAt().compareTo(m.getCreatedAt());
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("unit", getUnit());
            jsonObject.put("value", getValue());
            jsonObject.put("createdAt", getCreatedAt());
            System.out.println(jsonObject.toString());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}

