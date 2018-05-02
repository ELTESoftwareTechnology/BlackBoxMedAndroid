package com.damia.blackboxmed;

import com.damia.blackboxmed.Helper.Measurement;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ClassToJsonTest {

    String type;
    String units;
    int value;
    String createdAt;

    @Before
    public void initStrings() {
        type = "heartrate";
        units = "bpm";
        createdAt = "2018-03-12T19:12:01.0000Z";
        value = 113;
    }

    @Test
    public void checkJsonConvertion(){

        String j_type = "";
        String j_units = "";
        String j_createdAt = "";
        int j_value = 0;

        Measurement m = new Measurement(type, units, value, createdAt);
        JSONObject jo = m.toJSON();
        try{
            j_type = jo.getString("type");
            j_units = jo.getString("unit");
            j_value = jo.getInt("value");
            j_createdAt = jo.getString("createdAt");
        } catch (JSONException e){

        }
        assertTrue(type.matches(j_type));
        assertTrue(units.matches(j_units));
        assertTrue(createdAt.matches(j_createdAt));
        assertTrue(value == j_value);
    }
}
