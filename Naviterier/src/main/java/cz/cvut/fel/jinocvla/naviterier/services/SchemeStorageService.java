package cz.cvut.fel.jinocvla.naviterier.services;

import android.app.Activity;

import cz.cvut.fel.jinocvla.naviterier.activities.main.SchemeActivity;

/**
 * Created by usul on 25.4.2014.
 */
public interface SchemeStorageService {

    public void loadContext(SchemeActivity schemeActivity, SchemeActivity.State state);

    public void storeContext(SchemeActivity schemeActivity, SchemeActivity.State state);

    public void storeCurrentPosition(SchemeActivity schemeActivity, SchemeActivity.State state);

    public boolean isSchemeStored(SchemeActivity.State state);

    public Class navClass(SchemeActivity.State state);
}
