package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableSingleActivity {

    public static final String TABLE_PERFORMANCE_ACTIVITY = "perform_activity";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME_STEMP = "timestemp";
    public static final String COLUMN_ACTIVITY = "activity";
    public static final String COLUMN_NUMB_SEGMENTS = "performance";
    private static final String DATABASE_CREATE = "create table " + TABLE_PERFORMANCE_ACTIVITY + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TIME_STEMP
            + " TEXT not null, " + COLUMN_NUMB_SEGMENTS + " int not null," + COLUMN_ACTIVITY + " int not null" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TableSingleActivity.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PERFORMANCE_ACTIVITY);
        onCreate(database);
    }
}