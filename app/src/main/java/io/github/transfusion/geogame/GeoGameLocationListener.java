package io.github.transfusion.geogame;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bryan Kok on 17-5-1.
 */
public class GeoGameLocationListener implements LocationListener{

    public interface OnLocationChangedCallback{
        void handle(Location location);
    }

    private List<OnLocationChangedCallback> onLocationChangedCallbackList = new ArrayList<>();

    public void addLocationChangedCallback(OnLocationChangedCallback cb){
        onLocationChangedCallbackList.add(cb);
    }

    @Override
    public void onLocationChanged(Location location) {

        for (OnLocationChangedCallback cb : onLocationChangedCallbackList){
            cb.handle(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
        Log.d(getClass().getName(), "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(getClass().getName(), "gps enabled? " + provider.toString());
    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
