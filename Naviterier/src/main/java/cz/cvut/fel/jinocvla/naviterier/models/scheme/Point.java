package cz.cvut.fel.jinocvla.naviterier.models.scheme;

import org.simpleframework.xml.Element;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by usul on 3.3.14.
 */
public class Point implements Serializable, Comparable {

    @Element(name = "location", required = false)
    private SchemeLocation schemeLocation;

    @Element(name = "description")
    private String description;

    @Element(name = "id")
    private Long id;

    @Element(name = "keywords")
    private String keywords;

    private Float distance = Float.MAX_VALUE;

    private Connection parentConnection;

    private List<Connection> connections = new LinkedList<Connection>();

    public SchemeLocation getSchemeLocation() {
        return schemeLocation;
    }

    public void setSchemeLocation(SchemeLocation schemeLocation) {
        this.schemeLocation = schemeLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Connection getParentConnection() {
        return parentConnection;
    }

    public void setParentConnection(Connection parentConnection) {
        this.parentConnection = parentConnection;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    @Override
    public String toString() {
        return keywords;
    }

    @Override
    public int compareTo(Object o) {
        if (! (o instanceof Point)) {
            throw new IllegalArgumentException("Comparing object is not type of Point!");
        }
        Point point = (Point) o;

        return this.distance.compareTo(point.distance);
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Point)) {
            throw new IllegalArgumentException("Comparing object is not type of Point!");
        }
        Point point = (Point) o;

        return this.id == point.id;
    }
}
