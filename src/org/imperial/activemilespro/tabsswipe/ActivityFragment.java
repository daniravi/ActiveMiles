package org.imperial.activemilespro.tabsswipe;

import com.google.gson.Gson;

public class ActivityFragment extends WebViewFragment<int[]> {

    private final Gson gson = new Gson();
    private final int[] newPerformance = new int[5];
    private final String[] newlabel = {"Low", "Medium", "High", "Idle", "Others"};

    public ActivityFragment() {
        super();
    }


    public void plot(int[] performance) {
        newPerformance[0] = performance[7];
        newPerformance[1] = performance[2];
        newPerformance[2] = performance[1] + performance[3];
        newPerformance[3] = performance[0] + performance[6] + performance[5];
        newPerformance[4] = performance[4];
        wChart.loadUrl("javascript:plot(" + gson.toJson(newPerformance) + "," + gson.toJson(newlabel) + "," + 0 + "," + data + ")");
    }

}
