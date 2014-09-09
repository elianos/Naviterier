package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;
import cz.cvut.fel.jinocvla.naviterier.services.LocationService;


/**
 * Created by usul on 15.3.14.
 */
public class LocationServiceImpl implements LocationService {

    private LocationManager locationManager;

    private final Context activity;

    private final String locationProviderGPS = LocationManager.GPS_PROVIDER;

    private final String locationProviderNetwork = LocationManager.NETWORK_PROVIDER;

    private LocationListener locationListener = new DummyListener();

    private Logger log = Logger.getLogger(LocationServiceImpl.class.getName());

    private Boolean active = false;

    private Location bestLocation = null;

    public LocationServiceImpl(Context a) {
        this.activity = a;

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(locationProviderGPS);
    }

    public void tryStart() {
        if (active == false && locationManager.isProviderEnabled(locationProviderGPS)) {
            callRefresh();
            active = true;
        } else if (active == true && !locationManager.isProviderEnabled(locationProviderGPS)) {
            locationManager.requestLocationUpdates(locationProviderNetwork, 0, 0, locationListener);
            active = false;
        }
    }

    public void tryStop() {
        locationManager.removeUpdates(locationListener);
        active = false;
    }

    private void callRefresh() {
        locationManager.requestLocationUpdates(locationProviderGPS, 0, 0, locationListener);
        locationManager.requestLocationUpdates(locationProviderNetwork, 0, 0, locationListener);
    }

    public void callRefresh(LocationListener listener) {
        locationManager.requestLocationUpdates(locationProviderGPS, 0, 0, listener);
        locationManager.requestLocationUpdates(locationProviderNetwork, 0, 0, listener);
    }

    public void stopRefresh(LocationListener listener) {
        locationManager.removeUpdates(listener);
    }

    private final Long DELTA_TIME = 1000l * 10;

    public Location getLocation() {
        List<Location> locations = new LinkedList<Location>();

        locations.add(locationManager.getLastKnownLocation(locationProviderGPS));
        locations.add(locationManager.getLastKnownLocation(locationProviderNetwork));

        for (Location loc : locations) {
            if (checkPossibleLoc(loc)) {
                if (bestLocation != null) {
                    if (bestLocation.getAccuracy() > loc.getAccuracy()) {
                        bestLocation = loc;
                    }
                } else {
                    bestLocation = loc;
                }
            }
        }

        return bestLocation;
    }

    public Location tryLocation(int interval) {
        tryStart();

        synchronized (this) {
            try {
                wait(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        tryStop();

        List<Location> locations = new LinkedList<Location>();

        locations.add(locationManager.getLastKnownLocation(locationProviderGPS));
        locations.add(locationManager.getLastKnownLocation(locationProviderNetwork));

        bestLocation = null;

        for (Location loc : locations) {
            if (bestLocation != null) {
                if (bestLocation.getAccuracy() > loc.getAccuracy()) {
                    bestLocation = loc;
                }
            } else {
                bestLocation = loc;
            }
        }


        return bestLocation;
    }

    private boolean checkPossibleLoc(Location loc) {
        Long currentTime = System.currentTimeMillis();
        if (loc == null) return false;
        if (Math.abs(currentTime - loc.getTime()) > DELTA_TIME) return false;
        return true;
    }

    public boolean isActive() { return active; }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public void destroy() {
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public LocationManager getLocationManager() {
        return locationManager;
    }

    public float getAccuracy() {
        if (bestLocation == null) return Float.MAX_VALUE;
        return bestLocation.getAccuracy();
    }

    public static float calcDistanceBetween(SchemeLocation from, SchemeLocation to) {
        float[] results = new float[1];
        Location.distanceBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), results);
        return results[0];
    }

    private class DummyListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }



}
