package cz.cvut.fel.jinocvla.naviterier.services.impl;

import java.util.LinkedList;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Point;
import cz.cvut.fel.jinocvla.naviterier.services.NavigationService;

/**
 * Created by usul on 11.3.14.
 */
public class NavigationServiceImpl implements NavigationService {

    private List<Connection> connectionList;

    private Integer position;

    private Integer currentConnection;

    public NavigationServiceImpl(List<Connection> connectionList) {
        this(connectionList, 0);
    }

    public NavigationServiceImpl(List<Connection> connectionList, Integer position) {
        this.connectionList = new LinkedList<Connection>();
        this.connectionList.add(new StartConnection(connectionList.get(0).getFromPoint()));
        this.connectionList.addAll(connectionList);
        this.position = position;
        this.currentConnection = 0;
    }

    public boolean hasNext() {
        if (connectionList.get(currentConnection).hasNext()) {
            return true;
        }

        if (currentConnection < connectionList.size() - 1) {
            return true;
        }
        return false;
    }

    public String getNext() {
        if (connectionList.get(currentConnection).hasNext()) {
            position++;
            return connectionList.get(currentConnection).getNext();
        } else if (currentConnection < connectionList.size()) {
            currentConnection++;
            position++;
            return connectionList.get(currentConnection).getNext();
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public boolean hasPrevious() {
        if (connectionList.get(currentConnection).hasPrevious()) {
            return true;
        }

        if (currentConnection > 0) {
            return true;
        }
        return false;
    }

    public SchemeLocation getNextLocation() {
        if (currentConnection != 0) {
            return connectionList.get(currentConnection).getToPoint().getSchemeLocation();
        } else {
            return connectionList.get(currentConnection + 1).getFromPoint().getSchemeLocation();
        }
    }

    public String getPrevious() {
        if (connectionList.get(currentConnection).hasPrevious()) {
            position--;
            return connectionList.get(currentConnection).getPrevious();
        } else if (currentConnection > 0) {
            connectionList.get(currentConnection--).decreseCurrent();
            position--;
            return connectionList.get(currentConnection).getPrevious();
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void goNextTimes(Integer times) {
        for(int i = 0; i < times; i++) {
            getNext();
        }
    }

    public float getDistanceToFinish() {
        float result = 0;
        for (int i = currentConnection + 1; i < connectionList.size() - 1; i++) {
            result += connectionList.get(i).getDistance();
        }
        return result;
    }

    public Integer getPosition() {
        return position;
    }


    public String getCurrent() {
        return connectionList.get(currentConnection).getCurrent();
    }

    public Connection getCurrentConnection() {
        return connectionList.get(currentConnection);
    }

    private class StartConnection extends Connection {

        private final String step;

        private StartConnection(Point startPoint) {
            this.step = startPoint.getDescription();
        }

        @Override
        public Boolean hasNext() {
            return false;
        }

        @Override
        public Boolean hasPrevious() {
            return false;
        }

        @Override
        public String getCurrent() {
            return step;
        }

        @Override
        public String getNext() {
            throw new ArrayIndexOutOfBoundsException();
        }

        @Override
        public String getPrevious() {
            return step;
        }
    }


}
