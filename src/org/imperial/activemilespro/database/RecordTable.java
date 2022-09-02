package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RecordTable {
    private static final String TAG = "RecordTable";
    public static final String TABLE_RECORD = "record";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_TIME_STAMP_LONG = "time_stamp_long";
    private static final String DATABASE_CREATE = "create table " + TABLE_RECORD + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_LABEL + " TEXT not null, " + COLUMN_DATA
            + " TEXT not null," + COLUMN_TIME_STAMP_LONG + " LONG not null" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(RecordTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
        onCreate(database);
    }
}