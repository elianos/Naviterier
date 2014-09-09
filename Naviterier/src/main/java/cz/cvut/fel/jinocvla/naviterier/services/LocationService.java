package cz.cvut.fel.jinocvla.naviterier.services;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * Created by usul on 5.4.2014.
 */
public interface LocationService {

    public Location getLocation();

    public Location tryLocation(int interval);

    public boolean isGpsEnabled();

    public boolean isActive();

    public void setActive(boolean active);

    public void tryStart();

    public void tryStop();

    public float getAccuracy();

    public void destroy();

    public LocationManager getLocationManager();

    public void callRefresh(LocationListener locationListener);

    public void stopRefresh(LocationListener locationListener);
}
