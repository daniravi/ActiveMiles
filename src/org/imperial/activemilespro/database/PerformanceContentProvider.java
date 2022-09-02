package org.imperial.activemilespro.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class PerformanceContentProvider extends ContentProvider {

    private PerformanceDatabaseHelper database;
    private static final int INT_TABLE_HOUR = 1;
    private static final int INT_TABLE_DAY = 2;
    private static final int INT_GET_SUM_HOUR = 3;
    private static final int INT_TABLE_GPS = 4;
    private static final int INT_TABLE_IMAGE = 5;
    private static final int INT_TABLE_STEP = 6;
    private static final int INT_GET_SUM_STEP = 7;
    private static final int INT_TABLE_SETTING = 8;
    private static final int INT_SPEED_ALT = 11;
    private static final int INT_SPEED_ALT_MAX = 12;
    private static final int INT_RECORD = 13;
    private static final int INT_SINGLE_ACTIVITY = 14;
    private static final int INT_GET_LAST_TIMESTEMP = 15;

    private static final String AUTHORITY = "org.imperial.activemilespro.provider";
    private static final String NAME_DATABASE = "performance";
    public static final String TABLE_HOUR = "tableHour";
    public static final String TABLE_DAY = "tableDay";
    public static final String SUM_PERFORMANCE = "SumPerformance";
    public static final String SUM_STEP = "SumStep";
    public static final String TABLE_GPS = "tableGPS";
    public static final String TABLE_IMAGE = "tableImage";
    public static final String TABLE_STEP = "tableStep";
    public static final String TABLE_SETTING = "tableSetting";
    public static final String GET_SPEED_ALT = "speed_alt";
    private static final String GET_SPEED_ALT_MAX = "speed_alt_max";
    public static final String GET_RECROD = "RECORD";
    public static final String GET_SINGLE_ACTIVITY = "single_activity";
    public static final String GET_LAST_TIMESTEMP = "getLastMinute";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + NAME_DATABASE;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + NAME_DATABASE;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, TABLE_HOUR, INT_TABLE_HOUR);
        sURIMatcher.addURI(AUTHORITY, SUM_PERFORMANCE, INT_GET_SUM_HOUR);
        sURIMatcher.addURI(AUTHORITY, TABLE_DAY, INT_TABLE_DAY);
        sURIMatcher.addURI(AUTHORITY, TABLE_GPS, INT_TABLE_GPS);
        sURIMatcher.addURI(AUTHORITY, TABLE_IMAGE, INT_TABLE_IMAGE);
        sURIMatcher.addURI(AUTHORITY, TABLE_STEP, INT_TABLE_STEP);
        sURIMatcher.addURI(AUTHORITY, SUM_STEP, INT_GET_SUM_STEP);
        sURIMatcher.addURI(AUTHORITY, TABLE_SETTING, INT_TABLE_SETTING);
        sURIMatcher.addURI(AUTHORITY, GET_SPEED_ALT, INT_SPEED_ALT);
        sURIMatcher.addURI(AUTHORITY, GET_SPEED_ALT_MAX, INT_SPEED_ALT_MAX);
        sURIMatcher.addURI(AUTHORITY, GET_RECROD, INT_RECORD);
        sURIMatcher.addURI(AUTHORITY, GET_SINGLE_ACTIVITY, INT_SINGLE_ACTIVITY);
        sURIMatcher.addURI(AUTHORITY, GET_LAST_TIMESTEMP, INT_GET_LAST_TIMESTEMP);
    }

    @Override
    public synchronized boolean onCreate() {
        database = new PerformanceDatabaseHelper(getContext());
        return false;
    }

    @Override
    public synchronized Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        int uriType = sURIMatcher.match(uri);
        String start;
        String end;
        String activity;

        switch (uriType) {
            case INT_TABLE_SETTING:
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + SettingTable.COLUMN_TYPE_SENSOR + "," + SettingTable.COLUMN_ADDRESS_SENSOR + "," + SettingTable.COLUMN_TYPE_SPPED_UPDATE + "," + SettingTable.COLUMN_SENSITIVITY_STEP
                                + "," + SettingTable.COLUMN_IS_RECORDING + "," + SettingTable.COLUMN_CURR_LABEL + "," + SettingTable.COLUMN_COLOR_HIST_SOCIAL + "," + SettingTable.COLUMN_TARGET_MET
                                + " FROM " + SettingTable.TABLE_SETUP, null);
                break;
            case INT_TABLE_HOUR:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                activity = uri.getQueryParameter("activity");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + PerformanceHourTable.COLUMN_HOUR + "," + PerformanceHourTable.COLUMN_PERFORMANCE + " FROM " + PerformanceHourTable.TABLE_PERFORMANCE_HOUR + " WHERE "
                                + PerformanceHourTable.COLUMN_HOUR + " <= " + end + " AND " + PerformanceHourTable.COLUMN_HOUR + " >= " + start + " AND " + PerformanceHourTable.COLUMN_ACTIVITY + " == "
                                + activity, null);
                break;
            case INT_TABLE_DAY:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                activity = uri.getQueryParameter("activity");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + PerformanceDayTable.COLUMN_DAY + "," + PerformanceDayTable.COLUMN_PERFORMANCE + " FROM " + PerformanceDayTable.TABLE_PERFORMANCE_DAY + " WHERE "
                                + PerformanceDayTable.COLUMN_DAY + " <= " + end + " AND " + PerformanceDayTable.COLUMN_DAY + " >= " + start + " AND " + PerformanceDayTable.COLUMN_ACTIVITY + " == "
                                + activity + " ORDER BY " + PerformanceDayTable.COLUMN_DAY, null);
                break;
            case INT_TABLE_GPS:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + GPSTable.COLUMN_LAT + "," + GPSTable.COLUMN_LNG + "," + GPSTable.SPEED + "," + GPSTable.COLUMN_TIME_STEMP + "," + GPSTable.PRECISION + " FROM " + GPSTable.TABLE_GPS
                                + " WHERE " + GPSTable.COLUMN_TIME_STEMP + " <= " + end + " AND " + GPSTable.COLUMN_TIME_STEMP + " >= " + start + " ORDER BY " + GPSTable.COLUMN_TIME_STEMP, null);
                break;

            case INT_TABLE_IMAGE:
                start = uri.getQueryParameter("time_stamp");
                if (start == null)
                    cursor = database.getReadableDatabase().rawQuery(
                            "SELECT " + ImageTable.COLUMN_NAME + "," + ImageTable.COLUMN_LAT + "," + ImageTable.COLUMN_LNG + " FROM " + ImageTable.TABLE_IMAGE + " ORDER BY " + ImageTable.COLUMN_NAME
                                    + " ASC", null);
                else

                    cursor = database.getReadableDatabase().rawQuery(
                            "SELECT " + ImageTable.COLUMN_NAME + "," + ImageTable.COLUMN_LAT + "," + ImageTable.COLUMN_LNG + " FROM " + ImageTable.TABLE_IMAGE + " WHERE " + ImageTable.COLUMN_TIME_STAMP
                                    + " == " + start + " ORDER BY " + ImageTable.COLUMN_NAME + " ASC", null);
                break;
            case INT_GET_SUM_HOUR:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                activity = uri.getQueryParameter("activity");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT SUM(" + PerformanceHourTable.COLUMN_PERFORMANCE + ") FROM " + PerformanceHourTable.TABLE_PERFORMANCE_HOUR + " WHERE " + PerformanceHourTable.COLUMN_HOUR + " <= " + end
                                + " AND " + PerformanceHourTable.COLUMN_HOUR + " >= " + start + " AND " + PerformanceHourTable.COLUMN_ACTIVITY + " == " + activity, null);
                break;

            case INT_GET_SUM_STEP:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT SUM(" + StepTable.COLUMN_STEP + ") FROM " + StepTable.TABLE_STEP + " WHERE " + StepTable.COLUMN_HOUR + " <= " + end + " AND " + StepTable.COLUMN_HOUR + " >= " + start,
                        null);
                break;
            case INT_TABLE_STEP:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + StepTable.COLUMN_STEP + " FROM " + StepTable.TABLE_STEP + " WHERE " + StepTable.COLUMN_HOUR + " <= " + end + " AND " + StepTable.COLUMN_HOUR + " >= " + start, null);
                break;

            case INT_SPEED_ALT:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + GPSTable.COLUMN_TIME_STEMP + " , " + GPSTable.SPEED + " , " + GPSTable.COLUMN_ALT + " FROM " + GPSTable.TABLE_GPS + " WHERE " + GPSTable.COLUMN_TIME_STEMP + " <= "
                                + end + " AND " + GPSTable.COLUMN_TIME_STEMP + " >= " + start + " AND " + GPSTable.SPEED + " > 0 AND " + GPSTable.PRECISION + " <= " + GPSTable.PRECISION_TRESHOLD_READ
                                + " ORDER BY " + GPSTable.COLUMN_TIME_STEMP, null);
                break;
            case INT_SPEED_ALT_MAX:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT max(" + GPSTable.SPEED + ") FROM " + GPSTable.TABLE_GPS + " WHERE " + GPSTable.COLUMN_TIME_STEMP + " <= " + end + " AND " + GPSTable.COLUMN_TIME_STEMP + " >= " + start
                                + " AND " + GPSTable.SPEED + " > 0 AND " + GPSTable.PRECISION + " <= " + GPSTable.PRECISION_TRESHOLD_READ, null);
                break;
            case INT_RECORD:
                start = uri.getQueryParameter("time_stamp");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + RecordTable.COLUMN_LABEL + "," + RecordTable.COLUMN_TIME_STAMP_LONG + " FROM " + RecordTable.TABLE_RECORD + " WHERE " + RecordTable.COLUMN_DATA + " == " + start
                                + " ORDER BY " + RecordTable.COLUMN_DATA + " DESC", null);
                break;
            case INT_SINGLE_ACTIVITY:
                start = uri.getQueryParameter("start");
                end = uri.getQueryParameter("end");
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT " + TableSingleActivity.COLUMN_ACTIVITY + "," + TableSingleActivity.COLUMN_NUMB_SEGMENTS + "," + TableSingleActivity.COLUMN_TIME_STEMP + " FROM " + TableSingleActivity.TABLE_PERFORMANCE_ACTIVITY + " WHERE "
                                + TableSingleActivity.COLUMN_TIME_STEMP + " <= " + end + " AND " + TableSingleActivity.COLUMN_TIME_STEMP + " >= " + start + " ORDER BY " + TableSingleActivity.COLUMN_TIME_STEMP, null);
                break;
            case INT_GET_LAST_TIMESTEMP:
                cursor = database.getReadableDatabase().rawQuery(
                        "SELECT max(" + PerformanceHourTable.COLUMN_HOUR + ") FROM " + PerformanceHourTable.TABLE_PERFORMANCE_HOUR, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public synchronized String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues values) {
        String t;
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case INT_TABLE_HOUR:
                id = sqlDB.insert(PerformanceHourTable.TABLE_PERFORMANCE_HOUR, null, values);
                t = TABLE_HOUR;
                break;
            case INT_TABLE_DAY:
                id = sqlDB.insert(PerformanceDayTable.TABLE_PERFORMANCE_DAY, null, values);
                t = TABLE_DAY;
                break;
            case INT_TABLE_GPS:
                id = sqlDB.insert(GPSTable.TABLE_GPS, null, values);
                t = TABLE_GPS;
                break;
            case INT_TABLE_STEP:
                id = sqlDB.insert(StepTable.TABLE_STEP, null, values);
                t = TABLE_STEP;
                break;
            case INT_TABLE_IMAGE:
                id = sqlDB.insert(ImageTable.TABLE_IMAGE, null, values);
                t = TABLE_IMAGE;
                break;
            case INT_RECORD:
                id = sqlDB.insert(RecordTable.TABLE_RECORD, null, values);
                t = GET_RECROD;
                break;
            case INT_SINGLE_ACTIVITY:
                id = sqlDB.insert(TableSingleActivity.TABLE_PERFORMANCE_ACTIVITY, null, values);
                t = GET_SINGLE_ACTIVITY;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(t + "/" + id);
    }

    @Override
    public synchronized int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public synchronized int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int rowsUpdated;
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        switch (uriType) {
            case INT_TABLE_HOUR:
                rowsUpdated = sqlDB.update(PerformanceHourTable.TABLE_PERFORMANCE_HOUR, values, selection, selectionArgs);
                break;
            case INT_TABLE_STEP:
                rowsUpdated = sqlDB.update(StepTable.TABLE_STEP, values, selection, selectionArgs);
                break;
            case INT_TABLE_SETTING:
                rowsUpdated = sqlDB.update(SettingTable.TABLE_SETUP, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}