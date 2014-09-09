package cz.cvut.fel.jinocvla.naviterier.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by usul on 7.3.14.
 */
@Root(name = "schemes")
@Namespace(reference="http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
public class MapsList {

    @Attribute(name = "noNamespaceSchemaLocation")
    @Namespace(reference="http://www.w3.org/2001/XMLSchema-instance")
    private String definition;

    @ElementList(name = "list")
    public List<MapScheme> mapSchemeList;


    @Override
    public String toString() {
        return "MapsList{" +
                "mapSchemeList=" + mapSchemeList +
                '}';
    }

    public void sort() {
        Collections.sort(mapSchemeList, new SchemeComparator());
    }

    public List<MapScheme> getMapSchemeList() {
        return mapSchemeList;
    }

    private class SchemeComparator implements Comparator<MapScheme> {


        @Override
        public int compare(MapScheme s1, MapScheme s2) {
            if (s1 == null || s2 == null) throw new IllegalArgumentException("Scheme can't be null!");

            return s1.getName().compareTo(s2.getName());
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
