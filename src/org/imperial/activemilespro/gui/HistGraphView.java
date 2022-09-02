package org.imperial.activemilespro.gui;

import org.imperial.activemilespro.R;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer.GridStyle;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;

public class HistGraphView extends LinearLayout {
    private final GraphView graph;
    private static final int maxMetMinutesForHour = 200;


    public HistGraphView(Context context) {
        super(context);
        this.setOrientation(VERTICAL);
        TableRow row = (TableRow) View.inflate(context, R.layout.compare_item, null);
        this.addView(row);
        LinearLayout gv = (LinearLayout) findViewById(R.id.graph);

        graph = new GraphView(context);
        graph.setBackgroundColor(Color.argb(50, 127, 127, 200));
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(24);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(maxMetMinutesForHour);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setGridStyle(GridStyle.NONE);

        gv.addView(graph);

    }

    public void resetData(int[] data, int color) {
        graph.removeAllSeries();
        BarGraphSeries<DataPoint> mSeries = new BarGraphSeries<>();
        for (int i = 0; i < data.length; i++)
            mSeries.appendData(new DataPoint(i, data[i]), true, 24);
        mSeries.setSpacing(50);
        mSeries.setColor(color);
        graph.addSeries(mSeries);
        graph.invalidate();

    }

}