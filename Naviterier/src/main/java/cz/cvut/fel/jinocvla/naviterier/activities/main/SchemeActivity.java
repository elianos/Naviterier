package cz.cvut.fel.jinocvla.naviterier.activities.main;

import cz.cvut.fel.jinocvla.naviterier.models.Scheme;

/**
 * Created by usul on 23.3.2014.
 */
public interface SchemeActivity {

    public Scheme getScheme();

    public Long getFrom();

    public Long getTo();

    public Integer getCurrent();

    public Class getClassName();

    public void setScheme(Scheme scheme);

    public void setFrom(Long from);

    public void setTo(Long to);

    public void setCurrent(Integer current);

    public SchemeActivity getSchemeActivity();

    public static enum State {
        COMPLETE,
        PROGRESS;
    }

}
