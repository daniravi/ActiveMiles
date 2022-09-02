package org.imperial.activemilespro.tabsswipe;

import org.imperial.activemilespro.R;

import com.google.gson.Gson;

public class DayFragment extends WebViewFragment<int[]> {

    public DayFragment() {
        super();
    }


    public void plot(int[] performance) {
        Gson gson = new Gson();
        wChart.loadUrl("javascript:plot(" + gson.toJson(performance) + "," + 0 + ",'" + getString(R.string.MetMinutes) + "'," + data + ")");
    }

}
