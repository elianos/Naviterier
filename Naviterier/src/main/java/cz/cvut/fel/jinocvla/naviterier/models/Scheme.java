package cz.cvut.fel.jinocvla.naviterier.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Info;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Point;

/**
 * Created by usul on 21.2.14.
 */
@Root(name = "scheme")
@Namespace(reference="http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
public class Scheme implements Serializable {

    @Attribute(name = "noNamespaceSchemaLocation")
    @Namespace(reference="http://www.w3.org/2001/XMLSchema-instance")
    private String definition;

    @Element(name = "info")
    private Info info;

    @ElementList(name = "points")
    private List<Point> points;

    @ElementList(name = "connections")
    private List<Connection> connections;

    private String fileName;

    public Info getInfo() {
        return info;
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public String getName() {
        return info.getName();
    }

    public void setName(String name) {
        this.info.setName(name);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Scheme cleanToSerialization() {
        for (Connection connection : connections) {
            connection.setFromPoint(null);
            connection.setToPoint(null);
            connection.setCurrentPosition(-1);
        }

        for (Point point : points) {
            point.setDistance(Float.MAX_VALUE);
            point.getConnections().clear();
        }
        return this;
    }

    public void sort() {
        Collections.sort(points, new PointComparator());
    }

    @Override
    public String toString() {
        return "Scheme{" +
                "info=" + info +
                ", points=" + points +
                ", connections=" + connections +
                '}';
    }

    private class PointComparator implements Comparator<Point> {

        @Override
        public int compare(Point p1, Point p2) {
            if (p1 == null || p2 == null) throw new IllegalArgumentException("Scheme can't be null!");

            return p1.getKeywords().compareTo(p2.getKeywords());
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
