package cz.cvut.fel.jinocvla.naviterier.services.exception;

import android.app.Activity;

/**
 * Created by usul on 17.3.14.
 */
public class FileException extends AbstracetException {

    public FileException(Activity activity, String title, String message) {
        this.activity = activity;
        this.message = message;
        this.title = title;
    }


}
