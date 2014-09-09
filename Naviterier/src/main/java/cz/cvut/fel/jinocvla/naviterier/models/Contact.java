package cz.cvut.fel.jinocvla.naviterier.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.HashMap;

/**
 * Created by usul on 23.2.14.
 */
@Root(name = "contact")
public class Contact {

    @Element(name = "name")
    private String name;

    @Element(name = "phone")
    private String number;

    @Element(name = "description")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, Object> getListMap() {
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        outputMap.put("line1", getName());
        outputMap.put("line2", getDescription());
        outputMap.put("instance", this);

        return outputMap;
    }

    @Override
    public String toString() {
        return name;
    }
}
