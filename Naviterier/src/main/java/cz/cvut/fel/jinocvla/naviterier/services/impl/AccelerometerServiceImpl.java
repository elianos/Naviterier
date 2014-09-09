package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.LinkedList;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.services.AccelerometerService;

/**
 * Created by usul on 5.4.2014.
 */
public class AccelerometerServiceImpl implements AccelerometerService {

    private final SensorManager mSensorManager;

    private final Sensor mSensor;

    private final Context activity;

    private final AccelerometerListener accelerometerListener;

    public AccelerometerServiceImpl(Context activity) {
        this.activity = activity;
        this.mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.accelerometerListener = new AccelerometerListener();
    }

    @Override
    public List<Double> getData(int interval) {
        accelerometerListener.clear();

        mSensorManager.registerListener(accelerometerListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        try {
            synchronized (this) {
                wait(interval);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mSensorManager.unregisterListener(accelerometerListener);
        return accelerometerListener.getData();
    }


    public class AccelerometerListener implements SensorEventListener
    {
        private List<Double> data = new LinkedList<Double>();

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            double v = Math.pow(values[0], 2.) + Math.pow(values[1], 2.) + Math.pow(values[2], 2.);
            data.add(Math.pow(v, 0.5));
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void clear() {
            data.clear();
        }

        public List<Double> getData() {
            return data;
        }
    }
}
