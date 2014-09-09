package cz.cvut.fel.jinocvla.naviterier.models.scheme;

import android.location.Location;
import android.location.LocationManager;

import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * Created by usul on 3.3.14.
 */
public class SchemeLocation implements Serializable {

    @Element(name = "longitude")
    private Double longitude;

    @Element(name = "latitude")
    private Double latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "SchemeLocation{" +
                "longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}
