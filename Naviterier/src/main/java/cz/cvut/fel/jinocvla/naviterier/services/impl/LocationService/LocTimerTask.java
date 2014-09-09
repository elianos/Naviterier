package cz.cvut.fel.jinocvla.naviterier.services.impl.LocationService;

import android.location.LocationManager;

import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by usul on 5.4.2014.
 */
public class LocTimerTask extends TimerTask {

    private Logger log = Logger.getLogger(LocTimerTask.class.getName());

    LocationCallback callback;

    LocationManager locationManager;

    String locationProviderGPS = LocationManager.GPS_PROVIDER;

    public void setCallback(LocationCallback callback) {
        this.callback = callback;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void run() {
        log.info("Loc Timer callback called!");
        callback.call(locationManager.getLastKnownLocation(locationProviderGPS));
    }

}
