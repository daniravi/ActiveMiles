package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GPSTable {
    private static final String TAG = "GPSTable";
    public static final String TABLE_GPS = "location";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME_STEMP = "timestemp";
    public static final String COLUMN_LNG = "longitude";
    public static final String COLUMN_ALT = "altitude";
    public static final String COLUMN_LAT = "latitude";
    public static final String PRECISION = "precision";
    public static final String SPEED = "speed";
    public static final String TYPE = "type_provider";
    public static final int PRECISION_TRESHOLD = 70;
    public static final int PRECISION_TRESHOLD_READ = 20;
    private static final String DATABASE_CREATE = "create table " + TABLE_GPS + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TIME_STEMP + " TEXT not null, " + COLUMN_LNG
            + " real not null, " + COLUMN_ALT + " real not null, " + COLUMN_LAT + " real not null, " + PRECISION + " real not null, " + SPEED
            + " real not null," + TYPE + " string not null" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(GPSTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
        onCreate(database);
    }
}