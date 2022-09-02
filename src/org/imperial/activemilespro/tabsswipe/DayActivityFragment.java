package org.imperial.activemilespro.tabsswipe;

import org.imperial.activemilespro.gui.ActiveMilesGUI;

import com.google.gson.Gson;

public class DayActivityFragment extends WebViewFragment<int[][]> {

    public DayActivityFragment() {
        super();
    }

    public void plot(int[][] performance) {
        Gson gson = new Gson();
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++)
            wChart.loadUrl("javascript:plot(" + gson.toJson(performance[i]) + "," + i + ",'" + ActiveMilesGUI.ActivitiesToShow[i] + "'," + data + ")");
    }

}
