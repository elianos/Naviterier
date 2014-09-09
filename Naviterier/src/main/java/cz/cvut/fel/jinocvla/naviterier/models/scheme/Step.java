package cz.cvut.fel.jinocvla.naviterier.models.scheme;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.io.Serializable;

/**
 * Created by usul on 7.3.14.
 */
@Root(name = "step")
public class Step implements Serializable {

    @Text
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Step{" +
                "text='" + text + '\'' +
                '}';
    }
}
