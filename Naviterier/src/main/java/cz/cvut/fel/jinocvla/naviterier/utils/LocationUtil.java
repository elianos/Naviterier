package cz.cvut.fel.jinocvla.naviterier.utils;

import android.location.Location;

import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;

/**
 * Created by usul on 5.4.2014.
 */
public class LocationUtil {

    public static float calcDistanceBetween(SchemeLocation from, Location to) {
        if (from == null || to == null) return 0f;

        float[] results = new float[1];
        Location.distanceBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), results);
        return results[0];
    }
}
