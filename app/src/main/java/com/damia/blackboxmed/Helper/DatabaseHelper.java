package com.damia.blackboxmed.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "measurementsManager";

    // Table Names
    private static final String TABLE_MEASUREMENTS = "measurements";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_LINK = "link";

    // Common column names
    private static final String KEY_ID = "id";

    // NOTES Table - column names
    public static final String KEY_TYPE = "type";
    public static final String KEY_MEASURE = "measure";
    public static final String KEY_UNITS = "units";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_IMAGE = "imgRes";

    // TAGS Table - column names
    public static final String KEY_USERNAME = "username";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_MEASUREMENT_ID = "measurement_id";

    public static final String CREATE_TABLE_MEASUREMENTS = "CREATE TABLE IF NOT EXISTS " +
            TABLE_MEASUREMENTS + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_TYPE + " TEXT, " +
            KEY_MEASURE + " INTEGER, " +
            KEY_UNITS + " TEXT, " +
            KEY_IMAGE + " TEXT, " +
            KEY_CREATED_AT + " TEXT" + ")";

    // todo_tag table create statement
    private static final String CREATE_TABLE_LINK = "CREATE TABLE "
            + TABLE_LINK + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_USERNAME + " INTEGER," + KEY_MEASUREMENT_ID + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_MEASUREMENTS);
        db.execSQL(CREATE_TABLE_LINK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // create new tables
        onCreate(db);
    }

    public long createMeasure(Measurement measure, String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, measure.getType());
        values.put(KEY_MEASURE, measure.getValue());
        values.put(KEY_UNITS, measure.getUnit());
        values.put(KEY_IMAGE, measure.getImg_res());
        values.put(KEY_CREATED_AT, measure.getCreatedAt());

        // insert row
        long measurement_id = db.insert(TABLE_MEASUREMENTS, null, values);

        // assigning tags to todo

        createLink(measurement_id, username);


        return measurement_id;
    }


    public Measurement getMeasure(long todo_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_MEASUREMENTS + " WHERE "
                + KEY_ID + " = " + todo_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Measurement m = new Measurement();
        m.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        m.setType((c.getString(c.getColumnIndex(KEY_TYPE))));
        m.setUnit((c.getString(c.getColumnIndex(KEY_UNITS))));
        m.setValue((c.getInt(c.getColumnIndex(KEY_MEASURE))));
        m.setImg_res((c.getString(c.getColumnIndex(KEY_IMAGE))));
        m.setCreatedAt((c.getString(c.getColumnIndex(KEY_CREATED_AT))));

        return m;
    }

    public List<Measurement> getAllMeasurements() {
        List<Measurement> measures = new ArrayList<Measurement>();
        String selectQuery = "SELECT  * FROM " + TABLE_MEASUREMENTS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Measurement m = new Measurement();
                m.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                m.setType((c.getString(c.getColumnIndex(KEY_TYPE))));
                m.setUnit((c.getString(c.getColumnIndex(KEY_UNITS))));
                m.setValue((c.getInt(c.getColumnIndex(KEY_MEASURE))));
                m.setImg_res((c.getString(c.getColumnIndex(KEY_IMAGE))));
                m.setCreatedAt((c.getString(c.getColumnIndex(KEY_CREATED_AT))));

                // adding to todo list
                measures.add(m);
            } while (c.moveToNext());
        }

        return measures;
    }

    public ArrayList<Measurement> getAllMeasurementsByUser(String username) {
        ArrayList<Measurement> measures = new ArrayList<Measurement>();

        String selectQuery = "SELECT  * FROM " + TABLE_MEASUREMENTS + " tm, "
                + TABLE_LINK + " tl WHERE tl."
                + KEY_USERNAME + " = '" + username + "'" +
                " AND tm." + KEY_ID + " = " + "tl." + KEY_MEASUREMENT_ID;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Measurement m = new Measurement();
                m.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                m.setType((c.getString(c.getColumnIndex(KEY_TYPE))));
                m.setUnit((c.getString(c.getColumnIndex(KEY_UNITS))));
                m.setValue((c.getInt(c.getColumnIndex(KEY_MEASURE))));
                m.setImg_res((c.getString(c.getColumnIndex(KEY_IMAGE))));
                m.setCreatedAt((c.getString(c.getColumnIndex(KEY_CREATED_AT))));

                // adding to todo list
                measures.add(m);
            } while (c.moveToNext());
        }

        return measures;
    }

    public void deleteMeasurement(String createdAt) {

        long measurementId = 999999999;
        String getIdQuery = "SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE "
                + KEY_CREATED_AT + " = '" + createdAt + "'" +
                "";
        SQLiteDatabase dbr = this.getReadableDatabase();
        Cursor c = dbr.rawQuery(getIdQuery, null);

        if (c.moveToFirst()) {
            do {
                measurementId = c.getLong(c.getColumnIndex(KEY_ID));
            } while (c.moveToNext());
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEASUREMENTS, KEY_ID + " = ?",
                new String[]{String.valueOf(measurementId)});
        db.delete(TABLE_LINK, KEY_MEASUREMENT_ID + " = ?",
                new String[]{String.valueOf(measurementId)});
    }


    public long createLink(long measurement_id, String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MEASUREMENT_ID, measurement_id);
        values.put(KEY_USERNAME, username);

        long id = db.insert(TABLE_LINK, null, values);

        return id;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}