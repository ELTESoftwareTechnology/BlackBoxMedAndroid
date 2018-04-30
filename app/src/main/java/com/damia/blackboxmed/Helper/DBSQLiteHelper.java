package com.damia.blackboxmed.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import static com.damia.blackboxmed.Helper.DBMeasurements.Measure.COLUMN_TYPE;
import static com.damia.blackboxmed.Helper.DBMeasurements.Measure.TABLE_NAME;


public class DBSQLiteHelper extends SQLiteOpenHelper {

    private String type, unit, createdAt;
    private int id, value;

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "database";

    public DBSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBMeasurements.Measure.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addHandler(Measurement measure) {
        ContentValues values = new ContentValues();
        values.put(DBMeasurements.Measure.COLUMN_TYPE, measure.getType());
        values.put(DBMeasurements.Measure.COLUMN_MEASURE, measure.getValue());
        values.put(DBMeasurements.Measure.COLUMN_UNITS, measure.getUnit());
        values.put(DBMeasurements.Measure.COLUMN_CREATED_AT, measure.getCreatedAt());

        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("Inserting: "+values);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<Measurement> findHandler(String type) {
        String query = "Select * FROM " + TABLE_NAME + " WHERE " + COLUMN_TYPE + " LIKE " + "' %" + type + "% '";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Measurement measure;
        ArrayList<Measurement> data_to_return = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do{
                id = Integer.parseInt(cursor.getString(0));
                type = cursor.getString(1);
                value = Integer.parseInt(cursor.getString(2));
                unit = cursor.getString(3);
                createdAt = cursor.getString(4);
                measure = new Measurement(id, type, unit, value, createdAt);
                data_to_return.add(measure);
            } while(cursor.moveToNext());

            cursor.close();
        } else {

        }
        db.close();
        return data_to_return;
    }

    public ArrayList<Measurement> findAllHandler() {
        String query = "Select * FROM " + TABLE_NAME+";";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Measurement measure;
        ArrayList<Measurement> data_to_return = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do{
                id = Integer.parseInt(cursor.getString(0));
                type = cursor.getString(1);
                value = Integer.parseInt(cursor.getString(2));
                unit = cursor.getString(3);
                createdAt = cursor.getString(4);
                measure = new Measurement(id, type, unit, value, createdAt);
                data_to_return.add(measure);
            } while(cursor.moveToNext());

            cursor.close();
        } else {

        }
        db.close();
        return data_to_return;
    }

    public boolean deleteHandler(String createdAt) {
        boolean result = false;
        String query = "Select * FROM " + TABLE_NAME + " WHERE " + DBMeasurements.Measure.COLUMN_CREATED_AT + " = '" + createdAt + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Measurement measure = new Measurement();
        if (cursor.moveToFirst()) {
            measure.setMeasureID(Integer.parseInt(cursor.getString(0)));
            db.delete(TABLE_NAME, DBMeasurements.Measure.COLUMN_CREATED_AT + "=?",
                    new String[] {
                createdAt
            });
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }
}