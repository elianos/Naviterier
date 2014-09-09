package cz.cvut.fel.jinocvla.naviterier.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.HashMap;
import java.util.List;

/**
 * Created by usul on 23.2.14.
 */
@Root(name = "contacts")
@Namespace(reference="http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
public class Contacts {

    @Attribute(name = "noNamespaceSchemaLocation")
    @Namespace(reference="http://www.w3.org/2001/XMLSchema-instance")
    private String definition;

    @ElementList(name = "list")
    private List<Contact> contactList;

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "contactList=" + contactList +
                '}';
    }
}
