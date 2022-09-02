package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ImageTable {
    private static final String TAG = "ImageTable";
    public static final String TABLE_IMAGE = "image";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LNG = "longitude";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_TIME_STAMP = "time_stamp";
    private static final String DATABASE_CREATE = "create table " + TABLE_IMAGE + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NAME + " TEXT not null, " + COLUMN_LNG
            + " real not null, " + COLUMN_LAT + " real not null, " + COLUMN_TIME_STAMP + " TEXT not null" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(ImageTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);
        onCreate(database);
    }
}