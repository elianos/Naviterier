package cz.cvut.fel.jinocvla.naviterier.utils;

/**
 * Created by usul on 2.4.2014.
 */
public class GraphUtilException extends Throwable {

    private Integer emessage;

    private Integer title;

    public GraphUtilException(Integer emessage, Integer title) {

        this.emessage = emessage;
        this.title = title;
    }

    public Integer getEmessage() {
        return emessage;
    }

    public Integer getTitle() {
        return title;
    }
}
