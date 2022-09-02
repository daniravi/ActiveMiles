package org.imperial.activemilespro.tabsswipe;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.gui.PersonalPerformanceActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;

public abstract class WebViewFragment<T> extends Fragment {
    WebView wChart;
    private boolean dataAvaileble = false;
    private T performance;
    long data;
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

    public void setPerfomance(final T performance, long time, int w, int h) {
        this.dataAvaileble = true;
        this.performance = performance;
        this.data = time;
        this.height = h;
        this.width = w;
        if (wChart != null && isReady) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    plot(performance);
                    wChart.loadUrl("javascript:setTitle(" + data + "," + width + "," + height + ")");

                }
            });
        }
    }


    public WebViewFragment() {
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
        // wChart.setWebContentsDebuggingEnabled(true);
        wChart.addJavascriptInterface(new JsObject(), "injectedObject");
        wChart.loadUrl(pageUrl);
        wChart.setWebViewClient(new WebViewClient() {

			/*public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
				myactivity.loadedStart();
			}*/

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isReady = true;
                if (dataAvaileble && url.compareTo(pageUrl) == 0) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                plot(performance);
                                wChart.loadUrl("javascript:setTitle(" + data + "," + width + "," + height + ")");

                            }
                        });
                }

            }
        });

        return rootView;
    }

    protected abstract void plot(T data);

}
