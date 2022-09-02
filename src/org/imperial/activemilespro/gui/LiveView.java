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

import org.imperial.activemilespro.R;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer.GridStyle;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries.CustomShape;

public class LiveView extends Activity {

    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private LineGraphSeries<DataPoint> mSeries4;
    private LineGraphSeries<DataPoint> mSeries5;
    private LineGraphSeries<DataPoint> mSeries6;
    private LineGraphSeries<DataPoint> mSeries7;
    private LineGraphSeries<DataPoint> mSeries8;
    private LineGraphSeries<DataPoint> mSeriesnull;
    private ArrayList<PointsGraphSeries<DataPoint>> mSeriesActivity;
    private double graph2LastXValue1 = 5d;
    private double graph2LastXValue2 = 5d;
    private double graph2LastXValue3 = 5d;
    public static final String TAG = "LiveView";
    private int numberofCurrentActivity = 0;
    private String[] ActivitySelectedName;
    private int[] ValueToShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.live_view);

        LinearLayout layout1 = (LinearLayout) this.findViewById(R.id.graph1);

        GraphView graph1 = new GraphView(this);
        mSeries1 = new LineGraphSeries<>();
        mSeries1.setColor(Color.GREEN);
        graph1.addSeries(mSeries1);
        mSeries2 = new LineGraphSeries<>();
        mSeries2.setColor(Color.WHITE);
        graph1.addSeries(mSeries2);
        mSeries3 = new LineGraphSeries<>();
        mSeries3.setColor(Color.RED);
        graph1.addSeries(mSeries3);

        mSeries4 = new LineGraphSeries<>();
        mSeries4.setColor(Color.BLUE);
        graph1.addSeries(mSeries4);
        mSeries5 = new LineGraphSeries<>();
        mSeries5.setColor(Color.CYAN);
        graph1.addSeries(mSeries5);
        mSeries6 = new LineGraphSeries<>();
        mSeries6.setColor(Color.DKGRAY);
        graph1.addSeries(mSeries6);

        graph1.getViewport().setYAxisBoundsManual(true);
        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(0);
        graph1.getViewport().setMaxX(100);
        graph1.getViewport().setMinY(-20);
        graph1.getViewport().setMaxY(20);
        graph1.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph1.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph1.getGridLabelRenderer().setGridStyle(GridStyle.HORIZONTAL);
        graph1.getViewport().setBackgroundColor(Color.rgb(10, 20, 40));
        graph1.getGridLabelRenderer().setHighlightZeroLines(false);
        graph1.getGridLabelRenderer().setNumVerticalLabels(3);
        graph1.setTitle("Acceleration & Gyro");
        layout1.addView(graph1);
        LinearLayout layout2 = (LinearLayout) this.findViewById(R.id.graph2);

        GraphView graph2 = new GraphView(this);
        mSeriesnull = new LineGraphSeries<>();
        mSeriesnull.setColor(Color.WHITE);
        graph2.addSeries(mSeriesnull);

        mSeries7 = new LineGraphSeries<>();
        mSeries7.setColor(Color.CYAN);
        graph2.addSeries(mSeries7);
        mSeries8 = new LineGraphSeries<>();
        mSeries8.setColor(Color.RED);
        graph2.addSeries(mSeries8);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(100);
        graph2.getViewport().setMinY(0);
        graph2.getViewport().setMaxY(70);

        graph2.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph2.getGridLabelRenderer().setNumVerticalLabels(10);
        graph2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph2.getGridLabelRenderer().setGridStyle(GridStyle.HORIZONTAL);
        graph2.getViewport().setBackgroundColor(Color.rgb(10, 20, 40));
        graph2.getGridLabelRenderer().setHighlightZeroLines(false);

        graph2.setTitle("Dust & Temperature");
        layout2.addView(graph2);

        LinearLayout layout3 = (LinearLayout) this.findViewById(R.id.graph3);

        GraphView graph3 = new GraphView(this);
        graph3.getViewport().setYAxisBoundsManual(true);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinX(0);
        graph3.getViewport().setMaxX(50);
        graph3.getViewport().setMinY(-50);
        graph3.getViewport().setMaxY(50);
        graph3.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graph3.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph3.getGridLabelRenderer().setGridStyle(GridStyle.HORIZONTAL);
        graph3.getViewport().setBackgroundColor(Color.rgb(10, 20, 40));
        graph3.getGridLabelRenderer().setHighlightZeroLines(false);
        graph3.getGridLabelRenderer().setTextSize(30f);

        graph3.setTitle("Activity");
        mSeriesActivity = new ArrayList<>(ActiveMilesGUI.NumberOfActivities);
        ActivitySelectedName = new String[ActiveMilesGUI.NumberOfActivities];
        ValueToShow = new int[ActiveMilesGUI.NumberOfActivities];
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
            mSeriesActivity.add(new PointsGraphSeries<DataPoint>());
            mSeriesActivity.get(i).setColor(Color.rgb((int) (Math.random() * 200) + 50, (int) (Math.random() * 200) * 50, (int) (Math.random() * 200) + 50));
            if (ActiveMilesGUI.ActivitiesSelected[i] && (i <= 7 || (ActiveMilesGUI.DeviceType == 2))) {
                graph3.addSeries(mSeriesActivity.get(i));
                ValueToShow[i] = numberofCurrentActivity + 1;
                numberofCurrentActivity++;
                ActivitySelectedName[numberofCurrentActivity] = ActiveMilesGUI.ActivitiesToShow[i];
            }
            mSeriesActivity.get(i).setSize(7);
            mSeriesActivity.get(i).setCustomShape(new CustomShape() {

                @Override
                public void draw(Canvas arg0, Paint arg1, float arg2, float arg3, DataPointInterface arg4) {
                    arg1.setStrokeWidth(5);
                    arg0.drawLine(arg2, arg3 - 10, arg2, arg3 + 30, arg1);

                }
            });
        }
        graph3.getViewport().setScalable(true);
        graph3.getViewport().setMinY(1);
        graph3.getViewport().setMaxY(numberofCurrentActivity);
        graph3.getGridLabelRenderer().setNumVerticalLabels(numberofCurrentActivity);

        graph3.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    if ((int) value > 0 && (int) value <= numberofCurrentActivity)
                        return ActivitySelectedName[(int) value];
                    else
                        return "";
                } else {
                    if ((int) value > 0 && (int) value <= numberofCurrentActivity)
                        return ActivitySelectedName[(int) value];
                    else
                        return "";
                }
            }
        });

        if (ActiveMilesGUI.DeviceType == 2) {
            graph1.getViewport().setMinY(-3);
            graph1.getViewport().setMaxY(3);
            graph2.getViewport().setMinY(-2);
            graph2.getViewport().setMaxY(2);
        }
        layout3.addView(graph3);
        if (ActiveMilesGUI.remoteService != null)
            ActiveMilesGUI.remoteService.showOnLiveView(this);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (ActiveMilesGUI.remoteService != null)
            ActiveMilesGUI.remoteService.removeLiveView();
    }

    public void addAccData(float a1, float a2, float a3) {
        graph2LastXValue1 += 1d;
        mSeries1.appendData(new DataPoint(graph2LastXValue1, a1), true, 100);
        mSeries2.appendData(new DataPoint(graph2LastXValue1, a2), true, 100);
        mSeries3.appendData(new DataPoint(graph2LastXValue1, a3), true, 100);
    }

    public void addGyroData(float g1, float g2, float g3) {
        graph2LastXValue2 += 1d;
        mSeries4.appendData(new DataPoint(graph2LastXValue2, g1), true, 100);
        mSeries5.appendData(new DataPoint(graph2LastXValue2, g2), true, 100);
        mSeries6.appendData(new DataPoint(graph2LastXValue2, g3), true, 100);
        mSeriesnull.appendData(new DataPoint(graph2LastXValue2, 0), true, 100);

    }

    public void addDastData(float d1)
    {
        mSeries7.appendData(new DataPoint(graph2LastXValue2, d1), true, 100);

    }
    public void addTempData(float d2)
    {
        mSeries8.appendData(new DataPoint(graph2LastXValue2, d2), true, 100);
    }

    public void addActivity(int[] a1, int classLabel, int numOfSegmentForCurrentActivity) {
        if (ActiveMilesGUI.debug) {
            Toast.makeText(getApplicationContext(), ActiveMilesGUI.ActivitiesToShow[classLabel], Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < numOfSegmentForCurrentActivity * 5; i++) {
            graph2LastXValue3 += 1d;
            for (int j = 0; j < ActiveMilesGUI.NumberOfActivities; j++)
                mSeriesActivity.get(j).appendData(new DataPoint(graph2LastXValue3, ValueToShow[a1[j]]), true, 500);
        }
    }
}
