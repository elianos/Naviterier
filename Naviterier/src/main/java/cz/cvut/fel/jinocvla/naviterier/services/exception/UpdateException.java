package cz.cvut.fel.jinocvla.naviterier.services.exception;

import android.app.Activity;

/**
 * Created by usul on 25.4.2014.
 */
public class UpdateException extends AbstracetException {

    public UpdateException(Activity activity, String title, String message) {
        this.activity = activity;
        this.message = message;
        this.title = title;
    }

}
