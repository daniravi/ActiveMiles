package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PerformanceHourTable {

    public static final String TABLE_PERFORMANCE_HOUR = "perform_hour";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_PERFORMANCE = "performance";
    public static final String COLUMN_ACTIVITY = "activity";
    private static final String DATABASE_CREATE = "create table " + TABLE_PERFORMANCE_HOUR + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_HOUR + " TEXT not null, "
            + COLUMN_PERFORMANCE + " int not null," + COLUMN_ACTIVITY + " int not null" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(PerformanceHourTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PERFORMANCE_HOUR);
        onCreate(database);
    }
}