package org.imperial.activemilespro.service;

import java.util.Timer;
import java.util.TimerTask;

import org.imperial.activemilespro.interface_utility.IntOnLocationChange;
import org.imperial.activemilespro.interface_utility.UtilsNetwork;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LocationTracker implements LocationListener {

    private static long DistanceLp;
    private static long MinDistanceLp;
    private static long TimingLp;
    private int TypeLp;														/* 1 LowPower, 2 HighPower*/
    private static final long SearchLocationProviderTime = 10000;
    private static final long WaitForProvider = 5000;
    private long ResetFlag = 1000 * 60 * 5;
    public static final int LocationTimeExpiring = 1000 * 60;
    private static final String TAG = "LocationTracker";
    private Location curr_Best_Location;
    private static String curr_Provider;
    private final Context curr_Context;
    private static LocationManager locationManager;
    private final Timer SearchLocationProviderTask;
    private final Timer ResetFlagTask;
    private boolean NetworkIsNotWorking = false;
    private boolean GPSIsNotWorking = false;
    private boolean AlreadySearchingNetworkingProcessStart = false;
    private boolean AlreadySearchingGPSProcessStart = false;
    private static LocationListener location_Listener;
    private final IntOnLocationChange curr_listener;
    private static final Object lock_curr_Provider = new Object();
    private Location lastSendedLocation;


    public LocationTracker(Context context, IntOnLocationChange listener) {
        curr_Context = context;
        curr_listener = listener;
        setProviderPrecision(0);
        locationManager = (LocationManager) curr_Context.getSystemService(Context.LOCATION_SERVICE);
        SearchLocationProviderTask = new Timer();
        ResetFlagTask = new Timer();
        location_Listener = this;
        SearchLocationProviderTask.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        synchronized (lock_curr_Provider) {
                            if (TypeLp == 1)
                                taskToSearchNetworkAgain(ResetFlag);
                            else
                                taskToSearchGPSkAgain(ResetFlag);
                            String newProvider = check_New_Provider();
                            if (!isLocationValid() || (newProvider != null && !(curr_Provider.compareTo(newProvider) == 0))) {
                                curr_Provider = newProvider;
                                update_Location_Provider();
                                isLocationValid(); //update the
                                // curr_bestLocation
                                onLocationChanged(curr_Best_Location);
                            }
                        }
                    }
                }).start();
            }
        }, 1000, SearchLocationProviderTime);

    }

    private void taskToSearchNetworkAgain(long ResetFlag) {
        synchronized (lock_curr_Provider) {
            if ((!AlreadySearchingNetworkingProcessStart) && (locationManager != null) && (curr_Provider != null) && (curr_Provider.compareTo(LocationManager.GPS_PROVIDER) == 0)
                    && (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                AlreadySearchingNetworkingProcessStart = true;
                ResetFlagTask.schedule(new TimerTask() {
                    public void run() {
                        new Thread(new Runnable() {
                            public void run() {
                                NetworkIsNotWorking = false;
                                GPSIsNotWorking = false;
                                AlreadySearchingNetworkingProcessStart = false;
                            }
                        }).start();
                    }
                }, ResetFlag);
            }
        }
    }

    private void taskToSearchGPSkAgain(long ResetFlag) {

        synchronized (lock_curr_Provider) {
            if ((!AlreadySearchingGPSProcessStart) && (locationManager != null) && (curr_Provider != null) && (curr_Provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0)
                    && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                AlreadySearchingGPSProcessStart = true;
                ResetFlagTask.schedule(new TimerTask() {
                    public void run() {
                        new Thread(new Runnable() {
                            public void run() {
                                NetworkIsNotWorking = false;
                                GPSIsNotWorking = false;
                                AlreadySearchingGPSProcessStart = false;
                            }
                        }).start();
                    }
                }, ResetFlag);
            }
        }
    }

    public final int MaxPrecision = 3;

    public void setProviderPrecision(int levelOfPrecision) {
        NetworkIsNotWorking = false;
        GPSIsNotWorking = false;
        AlreadySearchingNetworkingProcessStart = false;
        AlreadySearchingGPSProcessStart = false;
        if (levelOfPrecision == 3) {
            Log.d(TAG, "GPS");
            DistanceLp = 15;
            MinDistanceLp = 5;
            TimingLp = 10000;
            TypeLp = 2;
            ResetFlag = 1000 * 60 * 5;
        } else if (levelOfPrecision == 2) {
            Log.d(TAG, "High Frequency");
            DistanceLp = 20;
            MinDistanceLp = 10;
            TimingLp = 15000;
            TypeLp = 1;
            ResetFlag = 1000 * 60 * 7;
        } else if (levelOfPrecision == 1) {
            Log.d(TAG, "Medium Precision");
            DistanceLp = 30;
            MinDistanceLp = 15;
            TimingLp = 20000;
            TypeLp = 1;
            ResetFlag = 1000 * 60 * 8;
        } else if (levelOfPrecision == 0) {
            Log.d(TAG, "Low Precision");
            DistanceLp = 50;
            MinDistanceLp = 30;
            TimingLp = 40000;
            TypeLp = 1;
            ResetFlag = 1000 * 60 * 10;
        }
    }

    private boolean isLocationValid() {
        synchronized (lock_curr_Provider) {
            String local_curr_Provider = curr_Provider;
            if (locationManager != null && local_curr_Provider != null) {
                if (locationManager.getLastKnownLocation(local_curr_Provider) != null
                        && (!local_curr_Provider.equals(LocationManager.NETWORK_PROVIDER) || UtilsNetwork.isNetworkConnected(curr_Context))
                        && (Math.abs(System.currentTimeMillis() - locationManager.getLastKnownLocation(local_curr_Provider).getTime())) < LocationTimeExpiring) {
                    saveLocation(locationManager.getLastKnownLocation(local_curr_Provider));
                    return true;
                } else {
                    try {
                        synchronized (this) {
                            this.wait(WaitForProvider);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (locationManager.getLastKnownLocation(local_curr_Provider) != null
                            && (!local_curr_Provider.equals(LocationManager.NETWORK_PROVIDER) || UtilsNetwork.isNetworkConnected(curr_Context))
                            && (Math.abs(System.currentTimeMillis() - locationManager.getLastKnownLocation(local_curr_Provider).getTime())) < LocationTimeExpiring) {
                        saveLocation(locationManager.getLastKnownLocation(local_curr_Provider));
                        return true;
                    }
                    if (local_curr_Provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0)
                        NetworkIsNotWorking = true;
                    if (local_curr_Provider.compareTo(LocationManager.GPS_PROVIDER) == 0)
                        GPSIsNotWorking = true;
                    return false;
                }
            } else
                return false;
        }

    }

    private static final Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            synchronized (lock_curr_Provider) {
                String local_curr_Provider = curr_Provider;
                if (local_curr_Provider != null && locationManager != null)

                {
                    locationManager.removeUpdates(location_Listener);
                    locationManager.requestLocationUpdates(local_curr_Provider, TimingLp, DistanceLp, location_Listener);
                }
            }
        }

    };

    private void update_Location_Provider() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                myHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    private String check_New_Provider() {
        String provider = null;
        if (locationManager != null) {
            if (TypeLp == 1) {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !NetworkIsNotWorking)
                    provider = LocationManager.NETWORK_PROVIDER;
                else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !GPSIsNotWorking)
                    provider = LocationManager.GPS_PROVIDER;
            }
            if (TypeLp == 2) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !GPSIsNotWorking)
                    provider = LocationManager.GPS_PROVIDER;
                else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !NetworkIsNotWorking)
                    provider = LocationManager.NETWORK_PROVIDER;
            }
        }
        if (provider == null) {
            GPSIsNotWorking = false;
            NetworkIsNotWorking = false;
        }
        return provider;
    }

    public void stopUsingLocation() {
        if (locationManager != null)
            locationManager.removeUpdates(this);
        SearchLocationProviderTask.cancel();
        ResetFlagTask.cancel();
    }

    private void saveLocation(Location location) {
        if (isBetterLocation(location, curr_Best_Location)) {
            curr_Best_Location = location;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        saveLocation(location);
        if (lastSendedLocation != null) {
            if (lastSendedLocation.distanceTo(curr_Best_Location) > DistanceLp
                    || (lastSendedLocation.getAccuracy() / 3 * 2 > curr_Best_Location.getAccuracy() && lastSendedLocation.distanceTo(curr_Best_Location) > MinDistanceLp))
                curr_listener.onLocationChange(curr_Best_Location);
        } else
            curr_listener.onLocationChange(curr_Best_Location);
        lastSendedLocation = curr_Best_Location;
    }

    @Override
    public void onProviderDisabled(String provider) {

        synchronized (lock_curr_Provider) {
            if (provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0) {
                NetworkIsNotWorking = true;
                GPSIsNotWorking = false;
            }
            if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0) {
                GPSIsNotWorking = true;
                NetworkIsNotWorking = false;
            }
            if (curr_Provider != null && provider.compareTo(curr_Provider) == 0) {
                curr_Provider = null;
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

        if (provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0)
            NetworkIsNotWorking = false;
        if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0)
            GPSIsNotWorking = false;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        synchronized (lock_curr_Provider) {

            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    if (provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0) {
                        NetworkIsNotWorking = true;
                        GPSIsNotWorking = false;
                        curr_Provider = null;
                    } else if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0) {
                        GPSIsNotWorking = true;
                        NetworkIsNotWorking = false;
                        curr_Provider = null;
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if (provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0) {
                        NetworkIsNotWorking = true;
                        GPSIsNotWorking = false;
                        curr_Provider = null;
                    } else if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0) {
                        GPSIsNotWorking = true;
                        NetworkIsNotWorking = false;
                        curr_Provider = null;
                    }
                    break;
                case LocationProvider.AVAILABLE:
                    if (provider.compareTo(LocationManager.NETWORK_PROVIDER) == 0)
                        NetworkIsNotWorking = false;
                    else if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0)
                        GPSIsNotWorking = false;
                    break;
            }
        }
    }

    private boolean isBetterLocation(Location location, Location curr_Best_Location) {
        if (curr_Best_Location == null) {
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - curr_Best_Location.getTime();
        boolean isSignificantlyNewer = timeDelta > LocationTimeExpiring;
        boolean isSignificantlyOlder = timeDelta < -LocationTimeExpiring;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - curr_Best_Location.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), curr_Best_Location.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    public Location getCurrentLocation() {
        if (UtilsNetwork.isLocationValid(curr_Best_Location))
            return this.curr_Best_Location;
        else
            return null;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}