package org.imperial.activemilespro.gui;

import org.imperial.activemilespro.interface_utility.MyInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

class GetUser2PerformanceTask extends AsyncTask<JSONArray, Void, Void> {
    private final int[] PerformanceInt;
    private final ComporeListItmesFragment f;
    private final MyInteger finalScore;
    private int numberOfsharedPerformance = 0;

    public GetUser2PerformanceTask(int[] PerformanceInt, MyInteger FinalScore, ComporeListItmesFragment f, Context c) {
        this.PerformanceInt = PerformanceInt;
        this.f = f;
        this.finalScore = FinalScore;
    }

    @Override
    protected Void doInBackground(JSONArray... jsonNArrayActivity) {

        getAvgPerfromanc(jsonNArrayActivity[0]);
        return null;
    }

    private void getAvgPerfromanc(JSONArray jsonNArrayActivity) {
        for (int j = 0; j < jsonNArrayActivity.length(); j++) {

            try {
                JSONObject performanceObject = jsonNArrayActivity.getJSONObject(j).getJSONObject("data").getJSONObject("performance");

                String performanceId = performanceObject.getString("id");

                GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), performanceId, null, HttpMethod.GET, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            if (response != null)
                                if (response.getJSONObject() != null) {
                                    if (response.getJSONObject().getJSONObject("data") != null) {
                                        JSONObject jsonPerformance = new JSONObject(response.getJSONObject().getJSONObject("data").toString());
                                        String performance = jsonPerformance.getString("performance_data");
                                        String[] hour_performance = performance.split(",");
                                        for (int z = 0; z < hour_performance.length; z++) {
                                            PerformanceInt[z] += (Integer.parseInt(hour_performance[z]));
                                        }
                                        numberOfsharedPerformance++;
                                    }

                                } //else {
                            //showToast(response.getError().getErrorMessage(), c);
                            //}

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
                request.executeAndWait();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if (jsonNArrayActivity.length() != 0 && numberOfsharedPerformance != 0)
            for (int z = 0; z < PerformanceInt.length; z++) {
                PerformanceInt[z] = PerformanceInt[z] / numberOfsharedPerformance;
                finalScore.add(PerformanceInt[z]);
            }
    }

    @Override
    protected void onPostExecute(Void result) {
        f.update();
    }

}