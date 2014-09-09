package cz.cvut.fel.jinocvla.naviterier.models.scheme;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.services.impl.LocationServiceImpl;

/**
 * Created by usul on 3.3.14.
 */
@Root(name = "connection")
public class Connection implements Serializable {

    @Element(name = "from")
    private Long from;

    @Element(name = "to")
    private Long to;

    @Element(name = "type", required = false)
    private String type;

    @Element(name = "difficulty", required = false)
    private Integer difficulty;

    @Element(name = "distance", required = false)
    private Float distance;

    @ElementList(name = "steps")
    private List<Step> steps;

    private Point fromPoint;

    private Point toPoint;

    private Integer currentPosition = -1;

    public void setFromPoint(Point fromPoint) {
        this.fromPoint = fromPoint;
    }

    public void setToPoint(Point toPoint) {
        this.toPoint = toPoint;
    }

    public Point getFromPoint() {
        return fromPoint;
    }

    public Point getToPoint() {
        return toPoint;
    }

    // Calculate distance
    public Float calcConnectionDistance(Connection connection) {
        if (fromPoint != null && toPoint != null) {
            if (fromPoint.getSchemeLocation() != null && toPoint.getSchemeLocation() != null) {
                return LocationServiceImpl.calcDistanceBetween(connection.getFromPoint().getSchemeLocation(), connection.getToPoint().getSchemeLocation());
            } else {
                return  1f;
            }
        }
        return null;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Boolean hasNext() {
        if (currentPosition < steps.size()) {
            return true;
        }
        return false;
    }

    public String getNext() {
        if (currentPosition < steps.size() - 1) {
            return steps.get(++currentPosition).getText();
        } else if (currentPosition == steps.size() -1) {
            currentPosition++;
            return toPoint.getDescription();
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public Boolean hasPrevious() {
        if (currentPosition > 0) {
            return true;
        }
        return false;
    }

    public String getPrevious() {
        if (currentPosition == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return steps.get(--currentPosition).getText();
    }

    public String getCurrent() {
        if (currentPosition == steps.size()) {
            return toPoint.getDescription();
        } else {
            return steps.get(currentPosition).getText();
        }
    }

    public void decreseCurrent() {
        currentPosition--;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "from='" + from + '\'' +
                ", fromPoint='" + fromPoint + '\'' +
                ", to='" + to + '\'' +
                ", toPoint='" + toPoint + '\'' +
                ", type='" + type + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", distance='" + distance + '\'' +
                ", steps=" + steps +
                '}';
    }
}
