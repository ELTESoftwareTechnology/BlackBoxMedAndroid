package com.damia.blackboxmed.Helper;

import android.provider.BaseColumns;

public final class DBMeasurements {
    private DBMeasurements() {
    }

    public static class Measure implements BaseColumns {
        public static final String TABLE_NAME = "measurements";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_UNITS = "units";
        public static final String COLUMN_CREATED_AT = "createdAt";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_MEASURE + " INTEGER, " +
                COLUMN_UNITS + " TEXT, " +
                COLUMN_CREATED_AT + " TEXT" + ")";
    }
}