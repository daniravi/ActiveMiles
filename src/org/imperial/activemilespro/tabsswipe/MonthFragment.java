package org.imperial.activemilespro.tabsswipe;

import org.imperial.activemilespro.R;

import com.google.gson.Gson;

public class MonthFragment extends WebViewFragment<int[]> {

    public MonthFragment() {
        super();
    }


    public void plot(int[] performance) {
        Gson gson = new Gson();
        wChart.loadUrl("javascript:plot(" + gson.toJson(performance) + "," + 0 + ",'" + getString(R.string.MetMinutes) + "'," + data + ")");
    }

}
