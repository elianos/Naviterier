package cz.cvut.fel.jinocvla.naviterier.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by usul on 7.3.14.
 */
@Root(name = "scheme")
public class MapScheme {

    @Attribute(name = "name")
    String name;

    @Attribute(name = "url")
    String url;

    @Attribute(name = "fileName")
    String fileName;

    private boolean localeCopy = false;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isLocaleCopy() {
        return localeCopy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLocaleCopy(boolean localeCopy) {
        this.localeCopy = localeCopy;
    }


    @Override
    public String toString() {
        return name;
    }
}