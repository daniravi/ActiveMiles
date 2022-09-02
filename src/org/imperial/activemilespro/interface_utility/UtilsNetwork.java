package org.imperial.activemilespro.interface_utility;

import org.imperial.activemilespro.service.LocationTracker;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UtilsNetwork {

    public static boolean isNetworkConnected(Context context) {
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && (activeNetworkInfo.getType() == networkType && activeNetworkInfo.isConnected()))
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean isLocationValid(Location CurrentBestLocation) {
        return (CurrentBestLocation != null && (Math.abs(System.currentTimeMillis() - CurrentBestLocation.getTime())) < LocationTracker.LocationTimeExpiring);
    }

}