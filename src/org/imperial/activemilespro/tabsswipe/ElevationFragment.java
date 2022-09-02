package org.imperial.activemilespro.tabsswipe;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.gui.PersonalPerformanceActivity;

import com.google.gson.Gson;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;

public class ElevationFragment extends Fragment {
    private WebView wChart;
    private boolean toload = false;
    private Double[] speed;
    private Double[] alt;
    private String[] timestemp;
    private long data;
    private boolean isReady = false;
    private String pageUrl;
    private int height;
    private int width;
    private PersonalPerformanceActivity myactivity;

    private class JsObject {
        @JavascriptInterface
        public void completed() {
            myactivity.loadedComplete();
        }
    }

    public void setPerfomance(final Double[] speed, final Double[] alt, final String[] timestemp, long time, int w, int h) {
        this.toload = true;
        this.speed = speed;
        this.alt = alt;
        this.timestemp = timestemp;
        this.data = time;
        this.height = h;
        this.width = w;
        if (wChart != null && isReady) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    plot(speed, alt, timestemp);
                    wChart.loadUrl("javascript:setTitle(" + data + "," + width + "," + height + ")");
                }
            });
        }
    }

    public ElevationFragment() {
        super();
    }

    public void setWebViewFragment(String pageUrl, PersonalPerformanceActivity activity) {

        this.pageUrl = pageUrl;
        this.myactivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chart, container, false);

        wChart = (WebView) rootView.findViewById(R.id.wChart);
        wChart.getSettings().setJavaScriptEnabled(true);
        wChart.addJavascriptInterface(new JsObject(), "injectedObject");
        wChart.loadUrl(pageUrl);
        wChart.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                isReady = true;
                if (toload && url.compareTo(pageUrl) == 0) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                plot(speed, alt, timestemp);
                                wChart.loadUrl("javascript:setTitle(" + data + "," + width + "," + height + ")");
                            }
                        });
                }
            }
        });
        return rootView;
    }

    private void plot(Double[] speed, Double[] alt, String[] timestemp) {
        Gson gson = new Gson();
        wChart.loadUrl("javascript:plot(" + gson.toJson(speed) + "," + gson.toJson(timestemp) + "," + 0 + ",'Speed [m/s]'," + data + ")");
        wChart.loadUrl("javascript:plot(" + gson.toJson(alt) + "," + gson.toJson(timestemp) + "," + 1 + ",'Elevation'," + data + ")");
    }

}
