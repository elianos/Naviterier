package cz.cvut.fel.jinocvla.naviterier.services.impl;

import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;
import cz.cvut.fel.jinocvla.naviterier.services.NavigationService;

/**
 * Created by usul on 11.3.14.
 */
public class DummyNavigationServiceImpl implements NavigationService {

    public DummyNavigationServiceImpl() {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String getNext() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public SchemeLocation getNextLocation() {
        return null;
    }

    @Override
    public String getPrevious() {
        return null;
    }

    @Override
    public void goNextTimes(Integer times) {

    }

    @Override
    public float getDistanceToFinish() {
        return 0;
    }

    @Override
    public Integer getPosition() {
        return null;
    }

    @Override
    public String getCurrent() {
        return null;
    }

    @Override
    public Connection getCurrentConnection() {
        return null;
    }
}
