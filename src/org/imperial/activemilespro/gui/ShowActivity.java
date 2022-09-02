/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.imperial.activemilespro.gui;

import java.util.ArrayList;
import java.util.Calendar;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.Window;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.CalendarView.OnDateChangeListener;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer.GridStyle;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries.CustomShape;

public class ShowActivity extends FacebookManager implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<PointsGraphSeries<DataPoint>> mSeriesActivity1;
    private ArrayList<PointsGraphSeries<DataPoint>> mSeriesActivity2;
    private ArrayList<PointsGraphSeries<DataPoint>> mSeriesActivity3;
    private GraphView graph1;
    private GraphView graph2;
    private GraphView graph3;
    private int[] color;
    public static final String TAG = "LiveView";
    private int[] ValueToShow;
    private long dataToAnalize;
    private CalendarView myDataPicker = null;
    private final int maxNumberofPoint = 8 * 60 * 60 / 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        setContentView(R.layout.live_view);

        dataToAnalize = System.currentTimeMillis();

        setBehindContentView(R.layout.conf_map);
        myDataPicker = (CalendarView) this.findViewById(R.id.datePicker);
        myDataPicker.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView arg0, int year, int month, int date) {
                myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, date, 0, 0));
            }
        });

        dataToAnalize = myDataPicker.getDate();

        SlidingMenu sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        initializeFacebook();
        String[] activitySelectedName = new String[ActiveMilesGUI.NumberOfActivities];
        ValueToShow = new int[ActiveMilesGUI.NumberOfActivities];

        LinearLayout layout1 = (LinearLayout) this.findViewById(R.id.graph1);
        graph1 = new GraphView(this);
        graph1.getViewport().setYAxisBoundsManual(true);
        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(0);
        graph1.getViewport().setMaxX(maxNumberofPoint);
        graph1.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graph1.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph1.getGridLabelRenderer().setGridStyle(GridStyle.NONE);
        graph1.getViewport().setBackgroundColor(Color.rgb(10, 20, 40));
        graph1.getGridLabelRenderer().setHighlightZeroLines(false);
        graph1.getGridLabelRenderer().setTextSize(30f);
        graph1.setTitle("Activity from 00:00 to 08:00");
        mSeriesActivity1 = new ArrayList<>(ActiveMilesGUI.NumberOfActivities);

        LinearLayout layout2 = (LinearLayout) this.findViewById(R.id.graph2);
        graph2 = new GraphView(this);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(maxNumberofPoint);
        graph2.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graph2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph2.getGridLabelRenderer().setGridStyle(GridStyle.NONE);
        graph2.getViewport().setBackgroundColor(Color.rgb(10, 20, 40));
        graph2.getGridLabelRenderer().setHighlightZeroLines(false);
        graph2.getGridLabelRenderer().setTextSize(30f);
        graph2.setTitle("Activity from 08:00 to 16:00");
        mSeriesActivity2 = new ArrayList<>(ActiveMilesGUI.NumberOfActivities);

        LinearLayout layout3 = (LinearLayout) this.findViewById(R.id.graph3);
        graph3 = new GraphView(this);
        graph3.getViewport().setYAxisBoundsManual(true);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinX(0);
        graph3.getViewport().setMaxX(maxNumberofPoint);
        graph3.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graph3.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph3.getGridLabelRenderer().setGridStyle(GridStyle.NONE);
        graph3.getViewport().setBackgroundColor(Color.rgb(10, 20, 40));
        graph3.getGridLabelRenderer().setHighlightZeroLines(false);
        graph3.getGridLabelRenderer().setTextSize(30f);
        graph3.setTitle("Activity from 16:00 to 00:00");
        mSeriesActivity3 = new ArrayList<>(ActiveMilesGUI.NumberOfActivities);
        color = new int[ActiveMilesGUI.NumberOfActivities];
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
            color[i] = Color.rgb((int) (Math.random() * 200) + 50, (int) (Math.random() * 200) * 50, (int) (Math.random() * 200) + 50);

        }
        int numberofCurrentActivity = 0;
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
            if ((ActiveMilesGUI.ActivitiesSelected[i] && (i <= 7 || (ActiveMilesGUI.DeviceType == 2))) || i == 0) {
                ValueToShow[i] = numberofCurrentActivity + 1;
                numberofCurrentActivity++;
                if (i == 0)
                    activitySelectedName[numberofCurrentActivity] = "[            Others             ]";
                else
                    activitySelectedName[numberofCurrentActivity] = ActiveMilesGUI.ActivitiesToShow[i];
            }
        }
        graph1.getViewport().setScalable(true);
        graph1.getViewport().setMinY(1);
        graph1.getViewport().setMaxY(numberofCurrentActivity);
        graph1.getGridLabelRenderer().setNumVerticalLabels(numberofCurrentActivity);
        graph1.getGridLabelRenderer().setNumHorizontalLabels(8);
        graph1.getGridLabelRenderer().setLabelFormatter(new MylabelFormatter(numberofCurrentActivity, activitySelectedName));
        layout1.addView(graph1);

        graph2.getViewport().setScalable(true);
        graph2.getViewport().setMinY(1);
        graph2.getViewport().setMaxY(numberofCurrentActivity);
        graph2.getGridLabelRenderer().setNumVerticalLabels(numberofCurrentActivity);
        graph2.getGridLabelRenderer().setNumHorizontalLabels(8);
        graph2.getGridLabelRenderer().setLabelFormatter(new MylabelFormatter(numberofCurrentActivity, activitySelectedName));
        layout2.addView(graph2);

        graph3.getViewport().setScalable(true);
        graph3.getViewport().setMinY(1);
        graph3.getViewport().setMaxY(numberofCurrentActivity);
        graph3.getGridLabelRenderer().setNumVerticalLabels(numberofCurrentActivity);
        graph3.getGridLabelRenderer().setNumHorizontalLabels(8);
        graph3.getGridLabelRenderer().setLabelFormatter(new MylabelFormatter(numberofCurrentActivity, activitySelectedName));
        layout3.addView(graph3);
        getSupportLoaderManager().initLoader(0, null, this);
        getSupportLoaderManager().initLoader(1, null, this);
        getSupportLoaderManager().initLoader(2, null, this);
    }

    private void setGraph(int indGraph, int LastXValue1, int[][] data) {

        if (indGraph == 0) {
            graph1.removeAllSeries();
            mSeriesActivity1.clear();
            for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
                mSeriesActivity1.add(new PointsGraphSeries<>(generateData(LastXValue1, data, i)));
                mSeriesActivity1.get(i).setColor(color[i]);

                if ((ActiveMilesGUI.ActivitiesSelected[i] && (i <= 7 || (ActiveMilesGUI.DeviceType == 2))) || i == 0) {
                    graph1.addSeries(mSeriesActivity1.get(i));
                }
                mSeriesActivity1.get(i).setSize(8);
                mSeriesActivity1.get(i).setCustomShape(new CustomShape() {

                    @Override
                    public void draw(Canvas arg0, Paint arg1, float arg2, float arg3, DataPointInterface arg4) {
                        arg1.setStrokeWidth(3);
                        arg0.drawLine(arg2, arg3 - 10, arg2, arg3 + 30, arg1);

                    }
                });
            }

        }
        if (indGraph == 1) {
            graph2.removeAllSeries();
            mSeriesActivity2.clear();
            for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
                mSeriesActivity2.add(new PointsGraphSeries<>(generateData(LastXValue1, data, i)));
                mSeriesActivity2.get(i).setColor(color[i]);

                if ((ActiveMilesGUI.ActivitiesSelected[i] && (i <= 7 || (ActiveMilesGUI.DeviceType == 2))) || i == 0) {
                    graph2.addSeries(mSeriesActivity2.get(i));
                }
                mSeriesActivity2.get(i).setSize(8);
                mSeriesActivity2.get(i).setCustomShape(new CustomShape() {

                    @Override
                    public void draw(Canvas arg0, Paint arg1, float arg2, float arg3, DataPointInterface arg4) {
                        arg1.setStrokeWidth(3);
                        arg0.drawLine(arg2, arg3 - 10, arg2, arg3 + 30, arg1);

                    }
                });
            }
        }
        if (indGraph == 2) {
            graph3.removeAllSeries();
            mSeriesActivity3.clear();
            for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
                mSeriesActivity3.add(new PointsGraphSeries<>(generateData(LastXValue1, data, i)));
                mSeriesActivity3.get(i).setColor(color[i]);

                if ((ActiveMilesGUI.ActivitiesSelected[i] && (i <= 7 || (ActiveMilesGUI.DeviceType == 2))) || i == 0) {
                    graph3.addSeries(mSeriesActivity3.get(i));
                }
                mSeriesActivity3.get(i).setSize(8);
                mSeriesActivity3.get(i).setCustomShape(new CustomShape() {

                    @Override
                    public void draw(Canvas arg0, Paint arg1, float arg2, float arg3, DataPointInterface arg4) {
                        arg1.setStrokeWidth(3);
                        arg0.drawLine(arg2, arg3 - 10, arg2, arg3 + 30, arg1);

                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (ActiveMilesGUI.remoteService != null)
            ActiveMilesGUI.remoteService.removeLiveView();
    }

    public void yesterday(View v) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, day, 0, 0));

        applyData(v);
    }

    public void today(View v) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, day, 0, 0));
        applyData(v);
    }

    public void applyData(View ignored) {

        dataToAnalize = myDataPicker.getDate();
        getSupportLoaderManager().restartLoader(0, null, this);
        getSupportLoaderManager().restartLoader(1, null, this);
        getSupportLoaderManager().restartLoader(2, null, this);
        final Handler h = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            public void run() {
                toggle();
            }
        };
        h.post(r);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Uri uri = null;
        String start;
        String end;
        switch (id) {
            case 0: {
                start = UtilsCalendar.getStartDayInSec(dataToAnalize);
                end = UtilsCalendar.get08AM_InSec(dataToAnalize);
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_SINGLE_ACTIVITY);
                uri = uri.buildUpon().appendQueryParameter("start", start).build();
                uri = uri.buildUpon().appendQueryParameter("end", end).build();
                break;
            }
            case 1: {
                start = UtilsCalendar.get08AM_InSec(dataToAnalize);
                end = UtilsCalendar.get04PM_InSec(dataToAnalize);

                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_SINGLE_ACTIVITY);
                uri = uri.buildUpon().appendQueryParameter("start", start).build();
                uri = uri.buildUpon().appendQueryParameter("end", end).build();
                break;
            }
            case 2: {
                start = UtilsCalendar.get04PM_InSec(dataToAnalize);
                end = UtilsCalendar.getEndDayInSec(dataToAnalize);
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_SINGLE_ACTIVITY);
                uri = uri.buildUpon().appendQueryParameter("start", start).build();
                uri = uri.buildUpon().appendQueryParameter("end", end).build();
                break;
            }
        }
        return (new CursorLoader(this, uri, null, null, null, null));
    }

    private DataPoint[] generateData(int count, int[][] data, int activity) {
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double y = data[activity][i];
            DataPoint v = new DataPoint(i, y);
            values[i] = v;
        }
        return values;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        int[][] data = new int[ActiveMilesGUI.NumberOfActivities][maxNumberofPoint];
        double LastXValue1 = 0d;
        int numOfSegmentForCurrentActivity;
        int currentActivity;
        if (cursor.moveToFirst())
            do {
                numOfSegmentForCurrentActivity = cursor.getInt(1);
                currentActivity = cursor.getInt(0);
                if ((ActiveMilesGUI.ActivitiesSelected[currentActivity])) {
                    for (int i = 0; i < Math.ceil((double) numOfSegmentForCurrentActivity / 5); i++) {
                        LastXValue1 += 1d;
                        for (int j = 0; j < ActiveMilesGUI.NumberOfActivities; j++)
                            data[j][(int) LastXValue1] = -1;
                        data[currentActivity][(int) LastXValue1] = ValueToShow[currentActivity];

                    }
                } else {
                    for (int i = 0; i < Math.ceil((double) numOfSegmentForCurrentActivity / 5); i++) {
                        LastXValue1 += 1d;
                        data[0][(int) LastXValue1] = 1;
                        for (int j = 1; j < ActiveMilesGUI.NumberOfActivities; j++)
                            data[j][(int) LastXValue1] = -1;

                    }
                }

            } while (cursor.moveToNext());

        if (arg0.getId() == 0) {

            setGraph(0, (int) LastXValue1, data);
            graph1.getViewport().setMinX(0);
            graph1.getViewport().setMaxX(LastXValue1);
        }

        if (arg0.getId() == 1) {

            setGraph(1, (int) LastXValue1, data);
            graph2.getViewport().setMinX(0);
            graph2.getViewport().setMaxX(LastXValue1);
        }

        if (arg0.getId() == 2) {

            setGraph(2, (int) LastXValue1, data);
            graph3.getViewport().setMinX(0);
            graph3.getViewport().setMaxX(LastXValue1);
        }
        getSupportLoaderManager().destroyLoader(arg0.getId());

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }
}

class MylabelFormatter extends DefaultLabelFormatter {
    private final int numberofCurrentActivity;
    private final String[] ActivitySelectedName;

    public MylabelFormatter(int numberofCurrentActivity, String[] ActivitySelectedName) {
        this.numberofCurrentActivity = numberofCurrentActivity;
        this.ActivitySelectedName = ActivitySelectedName;
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            return super.formatLabel(value, isValueX);
        } else {
            if ((int) value >= 0 && (int) value <= numberofCurrentActivity)
                return ActivitySelectedName[(int) value];
            else
                return "";
        }
    }
}
