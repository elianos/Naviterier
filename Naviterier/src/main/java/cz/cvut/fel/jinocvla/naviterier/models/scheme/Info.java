package cz.cvut.fel.jinocvla.naviterier.models.scheme;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created by usul on 3.3.14.
 */
@Root(name = "info")
public class Info implements Serializable {

    @Element(name = "name")
    private String name;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "difficulty", required = false)
    private String difficulty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "Info{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
