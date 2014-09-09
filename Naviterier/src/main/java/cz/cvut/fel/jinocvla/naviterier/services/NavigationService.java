package cz.cvut.fel.jinocvla.naviterier.services;

import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;

/**
 * Created by usul on 2.4.2014.
 */
public interface NavigationService {

    public boolean hasNext();

    public String getNext();

    public boolean hasPrevious();

    public SchemeLocation getNextLocation();

    public String getPrevious();

    public void goNextTimes(Integer times);

    public float getDistanceToFinish();

    public Integer getPosition();


    public String getCurrent();

    public Connection getCurrentConnection();
}
