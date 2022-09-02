package org.imperial.activemilespro.gui;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.imperial.activemilespro.interface_utility.IntDrowableContainer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

class DownloadImageFromHttp extends AsyncTask<Object, Void, Bitmap> {
    private int position;
    private int activity;
    private final IntDrowableContainer myDrowableContainer;

    public DownloadImageFromHttp(IntDrowableContainer myDrowableContainer) {
        this.myDrowableContainer = myDrowableContainer;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Bitmap doInBackground(Object... params) {

        position = (Integer) params[1];
        activity = (Integer) params[2];

        try {
            InputStream in = openHttpConnection((String) params[0]);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (in != null) {
                in.close();
                return Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            } else
                return null;
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(Bitmap drawable) {

        if (drawable != null)
            myDrowableContainer.saveImage(position, activity, drawable);
    }

    private InputStream openHttpConnection(String urlStr) {
        InputStream in = null;

        try {
            HttpsURLConnection conn1 = (HttpsURLConnection) new URL(urlStr).openConnection();
            HttpsURLConnection.setFollowRedirects(true);
            conn1.setInstanceFollowRedirects(true);
            if (conn1.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = conn1.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return in;
    }

}
