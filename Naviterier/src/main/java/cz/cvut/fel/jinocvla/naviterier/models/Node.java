package cz.cvut.fel.jinocvla.naviterier.models;

/**
 * Created by usul on 21.2.14.
 */
public class Node {

    private String name;


    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
