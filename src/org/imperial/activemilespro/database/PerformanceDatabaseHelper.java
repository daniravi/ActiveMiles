package org.imperial.activemilespro.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class PerformanceDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "performance.db";
    private static final int DATABASE_VERSION = 31;

    public PerformanceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        PerformanceHourTable.onCreate(database);
        PerformanceDayTable.onCreate(database);
        GPSTable.onCreate(database);
        ImageTable.onCreate(database);
        StepTable.onCreate(database);
        SettingTable.onCreate(database);
        RecordTable.onCreate(database);
        TableSingleActivity.onCreate(database);
        database.execSQL("INSERT INTO " + SettingTable.TABLE_SETUP + " VALUES (1,1,'1',2,0,0,'',0,1000)");

    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        PerformanceHourTable.onUpgrade(database, oldVersion, newVersion);
        PerformanceDayTable.onUpgrade(database, oldVersion, newVersion);
        GPSTable.onUpgrade(database, oldVersion, newVersion);
        ImageTable.onUpgrade(database, oldVersion, newVersion);
        StepTable.onUpgrade(database, oldVersion, newVersion);
        SettingTable.onUpgrade(database, oldVersion, newVersion);
        RecordTable.onUpgrade(database, oldVersion, newVersion);
        TableSingleActivity.onUpgrade(database, oldVersion, newVersion);
        database.execSQL("INSERT INTO " + SettingTable.TABLE_SETUP + " VALUES (1,1,'1',2,0,0,'',0,1000)");
    }
}
