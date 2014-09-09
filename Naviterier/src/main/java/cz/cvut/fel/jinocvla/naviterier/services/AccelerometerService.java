package cz.cvut.fel.jinocvla.naviterier.services;

import java.util.List;

/**
 * Created by usul on 5.4.2014.
 */
public interface AccelerometerService {

    public List<Double> getData(int interval);
}
