package cz.cvut.fel.jinocvla.naviterier.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Point;

/**
 * Created by usul on 9.3.14.
 */
public class GraphUtil {

    /**
     * Calculation distances on connections
     * @param scheme map scheme
     */
    private static void calcDistances(Scheme scheme) {
        for (Connection connection : scheme.getConnections()) {
            if (connection.getDistance() == null) {
                connection.setDistance(connection.calcConnectionDistance(connection));
            }
        }
    }

    /**
     * Connet nodes by edges
     *
     * @param scheme map scheme
     */
    private static void connectGraph(Scheme scheme) {
        HashMap<Long, Point> connectionMap = new HashMap<Long, Point>();
        for(Point point : scheme.getPoints()) {
            connectionMap.put(point.getId(), point);
        }
        Point from;
        Point to;
        for(Connection connection : scheme.getConnections()) {
            from = connectionMap.get(connection.getFrom());
            to = connectionMap.get(connection.getTo());

            from.getConnections().add(connection);

            connection.setFromPoint(from);
            connection.setToPoint(to);

        }
    }

    /**
     * Create graph with distances
     *
     * @param scheme map scheme
     */
    public static void createGraph(Scheme scheme) {
        connectGraph(scheme);
        calcDistances(scheme);
    }

    public static List<Connection> dijkstra(Scheme scheme, Point from, Point to) throws GraphUtilException {
        from.setDistance(0);

        PriorityQueue<Point> connectionsQueue = new PriorityQueue<Point>();
        connectionsQueue.addAll(scheme.getPoints());
        Set<Point> closed = new HashSet<Point>();

        while (connectionsQueue.size() != 0) {
            Point parent = connectionsQueue.poll();
            closed.add(parent);


            for (Connection connection : parent.getConnections()) {
                Point child = connection.getToPoint();
                if (!closed.contains(child)) {
                    if (parent.getDistance() + connection.getDistance() < child.getDistance()) {
                        child.setDistance(parent.getDistance() + connection.getDistance());
                        child.setParentConnection(connection);
                        // Queue reordering
                        connectionsQueue.remove(child);
                        connectionsQueue.add(child);

                    }
                }
            }
        }

        return generateTrace(to);
    }

    private static List<Connection> generateTrace(Point finish) throws GraphUtilException {
        List<Connection> result = new LinkedList<Connection>();
        if (finish.getParentConnection() == null) {
            throw new GraphUtilException(R.string.problem_with_scheme, R.string.path_wasnt_find);
        }
        Point point = finish;
        while (true) {
            if (point.getParentConnection() == null) {
                break;
            }
            result.add(point.getParentConnection());
            point = point.getParentConnection().getFromPoint();
        }

        // Switch list order
        Collections.reverse(result);

        return result;
    }

}
