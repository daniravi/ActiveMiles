package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StepTable {

    public static final String TABLE_STEP = "step_table";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_STEP = "performance";
    private static final String DATABASE_CREATE = "create table " + TABLE_STEP + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_HOUR + " TEXT not null, " + COLUMN_STEP
            + " int not null);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(StepTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_STEP);
        onCreate(database);
    }
}