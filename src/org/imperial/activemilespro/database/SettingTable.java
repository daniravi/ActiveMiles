package org.imperial.activemilespro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SettingTable {
    private static final String TAG = "SettingTable";
    public static final String TABLE_SETUP = "setup";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TYPE_SENSOR = "type_sensor";
    public static final String COLUMN_ADDRESS_SENSOR = "adress_sensor";
    public static final String COLUMN_TYPE_SPPED_UPDATE = "speed_update";
    public static final String COLUMN_SENSITIVITY_STEP = "step_sensitivity";
    public static final String COLUMN_IS_RECORDING = "is_recording";
    public static final String COLUMN_CURR_LABEL = "curr_label";
    public static final String COLUMN_COLOR_HIST_SOCIAL = "color_hist_social";
    public static final String COLUMN_TARGET_MET = "TARGET_MET";
    private static final String DATABASE_CREATE = "create table " + TABLE_SETUP + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TYPE_SENSOR + " int not null, "
            + COLUMN_ADDRESS_SENSOR + " text not null, " + COLUMN_SENSITIVITY_STEP + " int not null, " + COLUMN_TYPE_SPPED_UPDATE
            + " int not null, " + COLUMN_IS_RECORDING + " int not null, " + COLUMN_CURR_LABEL + " text not null, " + COLUMN_COLOR_HIST_SOCIAL
            + " int not null, " + COLUMN_TARGET_MET + " int not null );";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(SettingTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_SETUP);
        onCreate(database);
    }
}