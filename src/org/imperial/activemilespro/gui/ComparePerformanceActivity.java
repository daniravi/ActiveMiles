package org.imperial.activemilespro.gui;

import java.util.ArrayList;
import java.util.Calendar;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.SettingTable;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;
import org.imperial.activemilespro.interface_utility.UtilsNetwork;
import org.imperial.activemilespro.service.ActivityDetectorBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ComparePerformanceActivity extends FacebookManager implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "FacebookManager";
    private CalendarView myDataPicker;
    private ComporeListItmesFragment MyComporeListItmesFragment;
    private boolean toLoad;
    private HistGraphView myPerformance;
    private final int[][] dataPerfromance = new int[ActiveMilesGUI.NumberOfActivities][24];
    private final int[] energyDay = new int[24];
    private int finalEnergy;
    private String start;
    private String end;
    private long DataToGetPerformance;
    private int numActivitReceivedType1 = 0;
    private ImageView myProfilePicture;
    private TextView userName;
    private TextView myFinalScose;
    private String myName = "Me";
    private ImageView colorHistView1;
    private ImageView colorHistView2;
    private ImageView colorHistView3;
    private ImageView colorHistView4;
    private ImageView colorHistView5;
    private int color;
    private LinearLayout layout_text_network_error;
    private ArrayList<DownloadImageFromHttp> myArrayDownloadImageFromHttp;
    private ArrayList<GetUser2PerformanceTask> myArrayGetUser2PerformanceTask;

    public void yesterday(View v) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, day, 0, 0));
        applyData(v);
    }

    public void today(View v) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, day, 0, 0));
        applyData(v);
    }

    private void resetColorButton() {
        stopAllTheTaskGetPerformance();
        switch (ActiveMilesGUI.color_hist_facebook) {
            case 0:
                colorHistView1.setBackgroundResource(R.drawable.border);
                break;
            case 1:
                colorHistView2.setBackgroundResource(R.drawable.border);
                break;
            case 2:
                colorHistView3.setBackgroundResource(R.drawable.border);
                break;
            case 3:
                colorHistView4.setBackgroundResource(R.drawable.border);
                break;
            case 4:
                colorHistView5.setBackgroundResource(R.drawable.border);
                break;
        }
    }

    private void getColorfromIndex() {
        if (ActiveMilesGUI.color_hist_facebook == 0)
            color = ContextCompat.getColor(getApplicationContext(), R.color.colorHist1);
        if (ActiveMilesGUI.color_hist_facebook == 1)
            color = ContextCompat.getColor(getApplicationContext(), R.color.colorHist2);
        if (ActiveMilesGUI.color_hist_facebook == 2)
            color = ContextCompat.getColor(getApplicationContext(), R.color.colorHist3);
        if (ActiveMilesGUI.color_hist_facebook == 3)
            color = ContextCompat.getColor(getApplicationContext(), R.color.colorHist4);
        if (ActiveMilesGUI.color_hist_facebook == 4)
            color = ContextCompat.getColor(getApplicationContext(), R.color.colorHist5);
    }

    private void saveNewColor() {
        ContentValues values = new ContentValues();
        values.put(SettingTable.COLUMN_COLOR_HIST_SOCIAL, ActiveMilesGUI.color_hist_facebook);
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
        getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
        LoadFriendPerformance();
        toggle();
        getColorfromIndex();
    }

    public void changeColor1(View ignored) {

        if (ActiveMilesGUI.color_hist_facebook != 0) {
            colorHistView1.setBackgroundResource(R.drawable.red_border);
            resetColorButton();
            ActiveMilesGUI.color_hist_facebook = 0;
            saveNewColor();
        }
    }

    public void changeColor2(View ignored) {
        if (ActiveMilesGUI.color_hist_facebook != 1) {

            colorHistView2.setBackgroundResource(R.drawable.red_border);
            resetColorButton();
            ActiveMilesGUI.color_hist_facebook = 1;
            saveNewColor();
        }
    }

    public void changeColor3(View ignored) {
        if (ActiveMilesGUI.color_hist_facebook != 2) {

            colorHistView3.setBackgroundResource(R.drawable.red_border);
            resetColorButton();
            ActiveMilesGUI.color_hist_facebook = 2;
            saveNewColor();
        }
    }

    public void changeColor4(View ignored) {
        if (ActiveMilesGUI.color_hist_facebook != 3) {
            colorHistView4.setBackgroundResource(R.drawable.red_border);
            resetColorButton();
            ActiveMilesGUI.color_hist_facebook = 3;
            saveNewColor();
        }
    }

    public void changeColor5(View ignored) {
        if (ActiveMilesGUI.color_hist_facebook != 4) {
            colorHistView5.setBackgroundResource(R.drawable.red_border);
            resetColorButton();
            ActiveMilesGUI.color_hist_facebook = 4;
            saveNewColor();
        }
    }

    public void applyData(View ignored) {

        long dataToAnalize = myDataPicker.getDate();
        ChangeDate(dataToAnalize);

        final Handler h = new Handler(Looper.getMainLooper());

        final Runnable r = new Runnable() {
            public void run() {
                toggle();
                userName.setText(myName + " " + UtilsCalendar.timeToStringForView(myDataPicker.getDate()));
            }
        };
        h.post(r);

    }

    private void setFragment(long DataToGetPerformance) {

        start = UtilsCalendar.getStartDay(DataToGetPerformance);
        end = UtilsCalendar.getEndDay(DataToGetPerformance);

        this.DataToGetPerformance = DataToGetPerformance;
        restartLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader = null;
        Uri uri;

        if (id < ActiveMilesGUI.NumberOfActivities) {
            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            end = UtilsCalendar.getEndDay(DataToGetPerformance);

            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_HOUR);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();
            uri = uri.buildUpon().appendQueryParameter("activity", "" + id % ActiveMilesGUI.NumberOfActivities).build();
            cursorLoader = new CursorLoader(this, uri, null, null, null, null);

        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        int currActivity = id % ActiveMilesGUI.NumberOfActivities;

        if (id < ActiveMilesGUI.NumberOfActivities) {
            numActivitReceivedType1++;
            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            for (int i = 0; i < 24; i++) {
                dataPerfromance[currActivity][i] = 0;
            }

            if (data.moveToFirst())
                do {
                    int curHour = (int) (Long.parseLong(data.getString(0)) - Long.parseLong(start));
                    dataPerfromance[currActivity][curHour] = (int) data.getDouble(1);
                    energyDay[curHour] += (int) (dataPerfromance[currActivity][curHour] * ActiveMilesGUI.getMET[currActivity] / (60.0 / ActivityDetectorBase.sizeOfSegment * 1000));

                } while (data.moveToNext());
            if (numActivitReceivedType1 == ActiveMilesGUI.NumberOfActivities) {
                for (int i = 0; i < 24; i++)
                    finalEnergy += energyDay[i];
                myFinalScose.setText((int) ((double) finalEnergy / ActiveMilesGUI.TargetMet * 100) + " %");
                myPerformance.resetData(energyDay, color);
            }
        }

    }

    private void ChangeDate(long DataToGetPerformance) {
        this.DataToGetPerformance = DataToGetPerformance;

        start = UtilsCalendar.getStartDay(DataToGetPerformance);
        end = UtilsCalendar.getEndDay(DataToGetPerformance);

        restartLoader();
    }

    private void restartLoader() {
        for (int i = 0; i < 24; i++) {
            energyDay[i] = 0;
        }
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++)
            this.getSupportLoaderManager().restartLoader(i, null, this);
        numActivitReceivedType1 = 0;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.compare_performance);
        setBehindContentView(R.layout.conf_compare);
        getColorfromIndex();
        SlidingMenu sm = getSlidingMenu();
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        setFragment(System.currentTimeMillis());

        if (this.getSupportLoaderManager().getLoader(0) == null)
            for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++)
                this.getSupportLoaderManager().initLoader(i, null, this);
        else
            restartLoader();

        myPerformance = new HistGraphView(this);

        LinearLayout MyGraph = (LinearLayout) findViewById(R.id.MyComparGraph);
        MyGraph.addView(myPerformance);
        initializeFacebook();
        myProfilePicture = (ImageView) findViewById(R.id.profilepic);

        myDataPicker = (CalendarView) this.findViewById(R.id.datePicker);
        myDataPicker.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView arg0, int year, int month, int date) {
                myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, date, 0, 0));
            }
        });
        MyComporeListItmesFragment = new ComporeListItmesFragment();
        MyComporeListItmesFragment.initialize(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.comparisonList, MyComporeListItmesFragment).commit();
        toLoad = true;
        userName = (TextView) findViewById(R.id.userName);
        myFinalScose = (TextView) findViewById(R.id.FinalScore);
        userName.setText(myName + " " + UtilsCalendar.timeToStringForView(myDataPicker.getDate()));

        colorHistView1 = (ImageView) findViewById(R.id.color1);
        colorHistView2 = (ImageView) findViewById(R.id.color2);
        colorHistView3 = (ImageView) findViewById(R.id.color3);
        colorHistView4 = (ImageView) findViewById(R.id.color4);
        colorHistView5 = (ImageView) findViewById(R.id.color5);
        colorHistView1.setImageResource(R.color.colorHist1);
        colorHistView2.setImageResource(R.color.colorHist2);
        colorHistView3.setImageResource(R.color.colorHist3);
        colorHistView4.setImageResource(R.color.colorHist4);
        colorHistView5.setImageResource(R.color.colorHist5);
        switch (ActiveMilesGUI.color_hist_facebook) {
            case 0:
                colorHistView1.setBackgroundResource(R.drawable.red_border);
                break;
            case 1:
                colorHistView2.setBackgroundResource(R.drawable.red_border);
                break;
            case 2:
                colorHistView3.setBackgroundResource(R.drawable.red_border);
                break;
            case 3:
                colorHistView4.setBackgroundResource(R.drawable.red_border);
                break;
            case 4:
                colorHistView5.setBackgroundResource(R.drawable.red_border);
                break;
        }
        layout_text_network_error = (LinearLayout) this.findViewById(R.id.layout_text_network_error);
    }

    private void getCircProfileImage(String name) {

        if (MyPhoto != null) {
            Bitmap circleBitmap = Bitmap.createBitmap(MyPhoto.getWidth(), MyPhoto.getHeight(), Bitmap.Config.ARGB_8888);

            BitmapShader shader = new BitmapShader(MyPhoto, TileMode.CLAMP, TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas c = new Canvas(circleBitmap);
            c.drawCircle(MyPhoto.getWidth() / 2, MyPhoto.getHeight() / 2, MyPhoto.getWidth() / 2, paint);
            myProfilePicture.setImageDrawable(new BitmapDrawable(this.getResources(), circleBitmap));
        }
        userName.setText(name + " " + UtilsCalendar.timeToStringForView(myDataPicker.getDate()));

    }

    private final BroadcastReceiver mMessageReceiverProfilPicture = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            myName = intent.getExtras().getString("Name");
            getCircProfileImage(myName);
        }
    };

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LoadFriendPerformance();
        }
    };


    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverProfilPicture);
        super.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("FacebookStatusChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverProfilPicture, new IntentFilter("FacebookPictureDownload"));

        if (toLoad) {
            LoadFriendPerformance();
            toLoad = false;
        }

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void stopAllTheTaskImageDownload() {
        if (myArrayDownloadImageFromHttp != null)
            for (int i = 0; i < myArrayDownloadImageFromHttp.size(); i++) {
                if (myArrayDownloadImageFromHttp.get(i) != null)
                    myArrayDownloadImageFromHttp.get(i).cancel(true);

            }
    }

    private void stopAllTheTaskGetPerformance() {
        if (myArrayGetUser2PerformanceTask != null)
            for (int i = 0; i < myArrayGetUser2PerformanceTask.size(); i++) {
                if (myArrayGetUser2PerformanceTask.get(i) != null)
                    myArrayGetUser2PerformanceTask.get(i).cancel(true);

            }
    }

    private void stopAllTheTask() {

        stopAllTheTaskImageDownload();
        stopAllTheTaskGetPerformance();
    }

    private void LoadFriendPerformance() {

        stopAllTheTask();
        myArrayDownloadImageFromHttp = new ArrayList<>();
        myArrayGetUser2PerformanceTask = new ArrayList<>();
        if (UtilsNetwork.isNetworkConnected(this.getApplicationContext())) {
            layout_text_network_error.setVisibility(View.GONE);

            if (AccessToken.getCurrentAccessToken() != null) {
                GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray array, GraphResponse response) {

                        if (response != null) {
                            if (response.getJSONObject() != null)
                                getDataFromUsers(response.getJSONObject());
                            else
                                showToast(response.getError().getErrorMessage(), getApplicationContext());
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,id");
                request.setParameters(parameters);
                request.executeAsync();
            }

        } else
            layout_text_network_error.setVisibility(View.VISIBLE);
    }


    private void showToast(final String msg, final Context c) {
        Handler handler = new Handler(c.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroy() {
        stopAllTheTask();
        super.onDestroy();
    }

    private void getDataFromUsers(JSONObject jsonObj) {
        try {

            JSONArray jsonNArrayFriend = jsonObj.getJSONArray("data");

            if (jsonNArrayFriend != null) {
                MyComporeListItmesFragment.resetList(color);

                for (int i = 0; i < jsonNArrayFriend.length(); i++) {

                    JSONObject jsonObjectFriend = jsonNArrayFriend.getJSONObject(i);
                    getUserPerformance(jsonObjectFriend, i);
                }
            }

        } catch (JSONException e1) {
            try {
                JSONObject jsonFriend = jsonObj.getJSONObject("data");
                MyComporeListItmesFragment.resetList(color);
                getUserPerformance(jsonFriend, 0);
                e1.printStackTrace();
            } catch (JSONException ignored) {
            }
        }

    }

    private void getUserPerformance(JSONObject jsonObjectFriend, int i) throws JSONException {
        String nameFriend = jsonObjectFriend.getString("name");
        String uidFriend = jsonObjectFriend.getString("id");

        Log.i(TAG, "uidFriend: " + uidFriend + ", nameFriend: " + nameFriend);
        MyComporeListItmesFragment.addUser(uidFriend, nameFriend);
        Bundle parmRequeste = new Bundle();
        parmRequeste.putInt("UserPosition", i);
        parmRequeste.putString("uidFriend", uidFriend);

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), uidFriend + "/activemiles:share", parmRequeste, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Bundle curBundle = response.getRequest().getParameters();
                try {

                    if (response.getJSONObject() != null && response.getJSONObject().getJSONArray("data") != null) {

                        JSONArray jsonNArrayActivity = new JSONArray(response.getJSONObject().getJSONArray("data").toString());

                        if (curBundle != null && MyComporeListItmesFragment != null && MyComporeListItmesFragment.listHistPerformance != null) {

                            GetUser2PerformanceTask m1 = new GetUser2PerformanceTask(MyComporeListItmesFragment.listHistPerformance.get(curBundle.getInt("UserPosition")),
                                    MyComporeListItmesFragment.listFinalPerformance.get(curBundle.getInt("UserPosition")), MyComporeListItmesFragment, getApplicationContext());
                            myArrayGetUser2PerformanceTask.add(m1);
                            DownloadImageFromHttp m2 = new DownloadImageFromHttp(MyComporeListItmesFragment);
                            myArrayDownloadImageFromHttp.add(m2);
                            m2.execute("https://graph.facebook.com/" + curBundle.getString("uidFriend") + "/picture?type=large", curBundle.getInt("UserPosition"), 0);
                            m1.execute(jsonNArrayActivity);
                        }

                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }

        });
        request.executeAsync();
    }

}