package com.damia.blackboxmed.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Measurement implements Serializable{

    int measureID;
    String type;
    String unit;
    int value;
    String createdAt;


    public Measurement(){}

    public Measurement(int measureID, String type, String unit, int value, String createdAt){
        this.measureID = measureID;
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

    public int getMeasureID() {
        return measureID;
    }

    public void setMeasureID(int measureID) {
        this.measureID = measureID;
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

    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("unit", getUnit());
            jsonObject.put("value", getValue());
            jsonObject.put("createdAt", getCreatedAt());
            System.out.println(jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
}

