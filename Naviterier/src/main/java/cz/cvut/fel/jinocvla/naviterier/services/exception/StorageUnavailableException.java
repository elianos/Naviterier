package cz.cvut.fel.jinocvla.naviterier.services.exception;

import android.app.Activity;

/**
 * Created by usul on 10.3.14.
 */
public class StorageUnavailableException extends AbstracetException {

    public StorageUnavailableException(Activity activity, String title, String message) {
        this.activity = activity;
        this.message = message;
        this.title = title;
    }

}
