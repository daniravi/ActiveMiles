/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.imperial.activemilespro.gui;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.ui.IconGenerator;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MapActivity extends FacebookManager implements LoaderManager.LoaderCallbacks<Cursor>, LocationListener, ClusterManager.OnClusterClickListener<ItemPhotoForMap>,
        ClusterManager.OnClusterInfoWindowClickListener<ItemPhotoForMap>, ClusterManager.OnClusterItemClickListener<ItemPhotoForMap>,
        ClusterManager.OnClusterItemInfoWindowClickListener<ItemPhotoForMap>, OnTouchListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private CalendarView myDataPicker = null;
    private LatLngBounds.Builder bc;
    private long dataToAnalize;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private static final int LandEachMeter = 1000;
    private final String IconFileLoc = CameraView.IconFileLoc;
    private boolean movedCamera = false;
    private boolean showImage = true;
    private boolean ImgAsPoint = false;
    private boolean followCurrentLocation;
    private SlidingMenu sm;
    private ClusterManager<ItemPhotoForMap> mClusterManager;
    private HeatmapTileProvider mProvider;
    private final ArrayList<LatLng> allProviderLocation = new ArrayList<>();
    private static final double ALT_HEATMAP_OPACITY = 1;
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255), Color.rgb(0, 191, 255), Color.rgb(0, 0, 127), Color.rgb(255, 0, 0)};
    private static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {0.0f, 0.10f, 0.20f, 0.60f, 1.0f};

    private static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS, ALT_HEATMAP_GRADIENT_START_POINTS);
    private ScrollView menuSetting;
    private final SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.getDefault());
    private final SimpleDateFormat day_date = new SimpleDateFormat("dd", Locale.getDefault());
    private TextView giorno;
    private TextView mese;


    public void locate(View ignored) {
        if (mMap != null) {
            Location loc = mMap.getMyLocation();
            if (loc != null) {
                CameraPosition camPos = new CameraPosition.Builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(mMap.getCameraPosition().zoom).build();
                CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
                mMap.moveCamera(camUpdate);
            }
        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(.5f);
            startPlay();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setAlpha(1f);
        }
        return false;
    }

    private void startPlay() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.like_main);
        try {
            mp.prepare();
        } catch (IllegalStateException | IOException ignored) {
        }
        mp.start();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(128);
        ActionBar bar = getActionBar();
        bar.hide();
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        setContentView(R.layout.map);
        giorno = (TextView) this.findViewById(R.id.giorno);
        mese = (TextView) this.findViewById(R.id.mese);

        giorno.setText(day_date.format(System.currentTimeMillis()));
        mese.setText(month_date.format(System.currentTimeMillis()));
        dataToAnalize = System.currentTimeMillis();

        setBehindContentView(R.layout.conf_map);
        myDataPicker = (CalendarView) this.findViewById(R.id.datePicker);
        myDataPicker.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView arg0, int year, int month, int date) {
                myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, date, 0, 0));
            }
        });

        dataToAnalize = myDataPicker.getDate();

        sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);

        initializeFacebook();


        followCurrentLocation = false;

        LinearLayout rb2 = (LinearLayout) this.findViewById(R.id.calendario);

        rb2.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View vee) {
                openCalendar(null);
            }
        });
        rb2.setOnTouchListener(this);

        menuSetting = (ScrollView) this.findViewById(R.id.menuSetting);

        TextView rb1 = (TextView) this.findViewById(R.id.locate);
        rb1.setOnTouchListener(this);

        TextView rb3 = (TextView) this.findViewById(R.id.Settings);

        rb3.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View vee) {
                if (menuSetting.getVisibility() == View.VISIBLE)
                    menuSetting.setVisibility(LinearLayout.GONE);
                else
                    menuSetting.setVisibility(LinearLayout.VISIBLE);
            }
        });

        rb3.setOnTouchListener(this);
        menuSetting.setVisibility(LinearLayout.GONE);
        TextView shareMap = (TextView) this.findViewById(R.id.ShareMap);
        shareMap.setOnTouchListener(this);
        shareMap.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View vee) {
                shareMap();
            }
        });

        if (checkPlayServices()) {
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }

    private void openCalendar(View ignored) {
        sm.showMenu();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (mMap != null) {
            if (followCurrentLocation) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
    }

    private boolean checkPlayServices() {
        try {
            getPackageManager().getApplicationInfo("com.google.android.gms", 0);
            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            if (status != ConnectionResult.SUCCESS) {
                if (GoogleApiAvailability.getInstance().isUserResolvableError(status)) {
                    showErrorDialog(status);
                } else {
                    Toast.makeText(this, "The map requires Google Play Services to be installed.", Toast.LENGTH_LONG).show();
                    finish();
                }
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "The map requires Google Play Services to be installed.", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void showErrorDialog(int code) {
        GoogleApiAvailability.getInstance().getErrorDialog(this, code, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    private void setUpMapIfNeeded() {

        if (mMap != null) {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mClusterManager = new ClusterManager<>(this, mMap);
            mClusterManager.setRenderer(new PhotoRenderer());
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.setOnClusterInfoWindowClickListener(this);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);
            mClusterManager.cluster();
            redrawMap();
            mMap.setMyLocationEnabled(true);
            mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = View.inflate(getApplicationContext(), R.layout.info_windows_layout, null);
                    TextView note = (TextView) v.findViewById(R.id.note);
                    note.setText(marker.getTitle());
                    return v;
                }

            });
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            // TODO fixes Google Maps bug:
            // http://stackoverflow.com/a/20905954/2075875
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        switch (id) {
            case 0: {
                String start = UtilsCalendar.getStartDayInMinute(dataToAnalize);
                String end = UtilsCalendar.getEndDayInMinute(dataToAnalize);

                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_GPS);
                uri = uri.buildUpon().appendQueryParameter("start", start).build();
                uri = uri.buildUpon().appendQueryParameter("end", end).build();
                break;
            }
            case 1: {
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_IMAGE);
                uri = uri.buildUpon().appendQueryParameter("time_stamp", UtilsCalendar.timeToStringDate(dataToAnalize).substring(0, 8)).build();
                break;
            }
        }

        return (new CursorLoader(this, uri, null, null, null, null));
    }

    private void showPath(Cursor cursor) {
        mMap.clear();
        mClusterManager.clearItems();
        PolylineOptions polyline = new PolylineOptions();
        bc = new LatLngBounds.Builder();
        Location newLoc = new Location("dummyprovider");
        Location prevLoc = new Location("dummyprovider");
        LatLng myLatLng = null;
        double distanceInMeters;
        double sumOfDistanza = 0;
        double remainingInLine;
        int indexDistance = 1;
        int nextLendMarker = LandEachMeter;
        double prevAccuracy = 0;
        double currAccuracy = 0;
        int currHour;
        int currMin;
        long currTimeStemp;
        long prevTimeStep;
        double diffSecondi;
        double currError;
        DecimalFormat df = new DecimalFormat("00");
        if (cursor.moveToFirst()) {
            myLatLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
            prevLoc.setLatitude(myLatLng.latitude);
            prevLoc.setLongitude(myLatLng.longitude);
            prevTimeStep = Long.parseLong(cursor.getString(3));
            currHour = Integer.parseInt(cursor.getString(3).substring(8, 10));
            currMin = Integer.parseInt(cursor.getString(3).substring(10, 12));
            IconGenerator iconFactory = new IconGenerator(this);
            iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
            iconFactory.setTextAppearance(R.style.iconMaps);
            mMap.addMarker(
                    new MarkerOptions().position(new LatLng(myLatLng.latitude, myLatLng.longitude)).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("START")))
                            .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()).anchor(0.5f, 1)).setTitle(df.format(currHour) + ":" + df.format(currMin));
            bc.include(myLatLng);
            if (!movedCamera)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
            polyline.add(myLatLng);

            if (cursor.moveToNext()) {
                do {

                    myLatLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                    currTimeStemp = Long.parseLong(cursor.getString(3));

                    newLoc.setLatitude(myLatLng.latitude);
                    newLoc.setLongitude(myLatLng.longitude);
                    distanceInMeters = SphericalUtil.computeDistanceBetween(new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()), myLatLng);
                    currError = (currAccuracy + prevAccuracy) / 0.8246;
                    distanceInMeters -= (currError / 5);
                    currAccuracy = cursor.getDouble(4);
                    diffSecondi = UtilsCalendar.computeDiffSec("" + currTimeStemp, "" + prevTimeStep);
                    if (((((currError < (distanceInMeters / 5))  && distanceInMeters < diffSecondi*27) && distanceInMeters > 30) || (currError < 30 && distanceInMeters > 10) /*|| currAccuracy < 20*/)
                            && diffSecondi > 5) {
                        prevTimeStep = currTimeStemp;
                        prevAccuracy = currAccuracy;
                        sumOfDistanza += distanceInMeters;
                        if (!ImgAsPoint)
                            while (sumOfDistanza >= nextLendMarker) {
                                remainingInLine = (nextLendMarker - (sumOfDistanza - distanceInMeters)) / distanceInMeters;
                                getProp(prevLoc.getLatitude(), prevLoc.getLongitude(), myLatLng.latitude, myLatLng.longitude, remainingInLine, indexDistance,
                                        df.format(currHour) + ":" + df.format(currMin));
                                indexDistance++;
                                if (indexDistance > 99)
                                    indexDistance = 1;
                                nextLendMarker += LandEachMeter;
                            }

                        polyline.add(myLatLng);
                        polyline.color(Color.rgb(1, 70, 150));
                        polyline.width(15);
                        mMap.addPolyline(polyline);

                        polyline = new PolylineOptions();
                        polyline.add(myLatLng);
                        bc.include(myLatLng);
                        prevLoc = new Location("dummyprovider");
                        prevLoc.setLatitude(newLoc.getLatitude());
                        prevLoc.setLongitude(newLoc.getLongitude());
                        currHour = Integer.parseInt(cursor.getString(3).substring(8, 10));
                        currMin = Integer.parseInt(cursor.getString(3).substring(10, 12));
                    }
                } while (cursor.moveToNext());
                mMap.addMarker(
                        new MarkerOptions().position(new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("END"))).anchor(0.5f, 1))
                        .setTitle(df.format(currHour) + ":" + df.format(currMin));
                mMap.addPolyline(polyline);
            }
        }
        if (myLatLng != null) {
            if (!movedCamera) {
                try {
                    movedCamera = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));

                } catch (IllegalStateException e) {

                    final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
                    if (mapView.getViewTreeObserver().isAlive()) {
                        mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
                            }
                        });
                    }
                }
            }
        }
        if (showImage)
            getSupportLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case 0: {
                showPath(cursor);
                break;
            }
            case 1: {

                Paint myPaint = new Paint();
                myPaint.setColor(Color.rgb(0, 0, 0));
                myPaint.setStrokeWidth(10);
                myPaint.setStyle(Paint.Style.STROKE);
                allProviderLocation.clear();

                if (cursor.moveToFirst())
                    do {
                        File file = new File(IconFileLoc + cursor.getString(0) + ".jpg");
                        if (file.exists()) {
                            String currdara = UtilsCalendar.timeToStringSec(Long.parseLong(cursor.getString(0)));
                            if (!ImgAsPoint) {
                                mClusterManager.addItem(new ItemPhotoForMap(new LatLng(randomize(cursor.getDouble(1)), randomize(cursor.getDouble(2))), currdara.substring(8, 10) + ":"
                                        + currdara.substring(10, 12), IconFileLoc + cursor.getString(0) + ".jpg"));

                            } else
                                allProviderLocation.add(new LatLng(cursor.getDouble(1), cursor.getDouble(2)));
                        }
                    } while (cursor.moveToNext());
                drawHeatMap();
                break;

            }
        }
        mClusterManager.cluster();
    }

    private double randomize(double a) {
        return a + (Math.random() - 0.5) / 3000;
    }

    private void addIcon(IconGenerator iconFactory, String text, LatLng position, String title) {
        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).position(position)
                .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        mMap.addMarker(markerOptions).setTitle(title);

    }

    private void getProp(double x0, double y0, double x1, double y1, double prop, int imgId, String title) {

        double point_x_2 = x1 - (x1 - x0) * (1 - prop);
        double point_y_2 = y1 - (y1 - y0) * (1 - prop);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        iconFactory.setTextAppearance(R.style.iconMaps);
        addIcon(iconFactory, imgId + "\n" + getString(R.string.Km), new LatLng(point_x_2, point_y_2), title);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private final SnapshotReadyCallback callback = new SnapshotReadyCallback() {

        @Override
        public void onSnapshotReady(Bitmap snapshot) {

            try {

													/*getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(true);
                                                    Bitmap backBitmap = getWindow().getDecorView().findViewById(android.R.id.content).getDrawingCache();
													Bitmap bmOverlay = Bitmap.createBitmap(
													        backBitmap.getWidth(), backBitmap.getHeight(),
													        backBitmap.getConfig());
													Canvas canvas = new Canvas(bmOverlay);
													canvas.drawBitmap(snapshot, new Matrix(), null);
													canvas.drawBitmap(backBitmap, 0, 0, null);*/
                postPhoto(snapshot, defaultCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void shareMap() {

        if (hasPermission()) {
            openProgressBar();
            mMap.snapshot(callback);

        } else {
            obtainPermission();
            Toast.makeText(getApplicationContext(), "Obtain facebook permission before share the Daily Performance!", Toast.LENGTH_LONG).show();
        }

    }

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

    public void applyData(View ignored) {

        dataToAnalize = myDataPicker.getDate();
        mese.setText(month_date.format(myDataPicker.getDate()));
        giorno.setText(day_date.format(myDataPicker.getDate()));
        movedCamera = false;
        redrawMap();
        final Handler h = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            public void run() {
                toggle();
            }
        };
        h.post(r);

    }

    private void redrawMap() {

        if (mMap != null) {
            mMap.clear();
            mClusterManager.clearItems();
            if (showImage)
                getSupportLoaderManager().restartLoader(0, null, this);
        }
    }

    public void showPhoto(View ignored) {
        CheckBox checbox = (CheckBox) this.findViewById(R.id.showPhoto);
        showImage = checbox.isChecked();
        redrawMap();

    }

    public void followCurrentLocation(View ignored) {
        CheckBox checbox = (CheckBox) this.findViewById(R.id.followCurrentLocation);
        followCurrentLocation = checbox.isChecked();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public boolean onClusterClick(Cluster<ItemPhotoForMap> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().title;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<ItemPhotoForMap> cluster) {

    }

    @Override
    public boolean onClusterItemClick(ItemPhotoForMap item) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(ItemPhotoForMap item) {
    }

    private void drawHeatMap() {
        if (mProvider == null && allProviderLocation.size() > 0) {
            mProvider = new HeatmapTileProvider.Builder().data(allProviderLocation).build();
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);

            // Render links

        }

        if (mProvider != null && allProviderLocation.size() > 0) {
            TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            mProvider.setData(allProviderLocation);
            mOverlay.clearTileCache();
        }

    }

    private class PhotoRenderer extends DefaultClusterRenderer<ItemPhotoForMap> implements OnCameraIdleListener {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PhotoRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);
            View multiProfile = View.inflate(getApplicationContext(), R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(ItemPhotoForMap photo, MarkerOptions markerOptions) {
            Drawable drawable = Drawable.createFromPath(photo.photoUrl);
            int width = mDimension;
            int height = mDimension;
            drawable.setBounds(0, 0, width, height);
            mImageView.setImageDrawable(drawable);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(photo.title);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<ItemPhotoForMap> cluster, MarkerOptions markerOptions) {
            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (ItemPhotoForMap p : cluster.getItems()) {

                if (profilePhotos.size() == 4)
                    break;

                Drawable drawable = Drawable.createFromPath(p.photoUrl);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }

        @Override
        public void onCameraIdle() {
            if (mMap.getCameraPosition().zoom <= 11 && !ImgAsPoint) {
                ImgAsPoint = true;
                redrawMap();
            }
            if (mMap.getCameraPosition().zoom > 11 && ImgAsPoint) {
                ImgAsPoint = false;
                redrawMap();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMapIfNeeded();
    }

}
