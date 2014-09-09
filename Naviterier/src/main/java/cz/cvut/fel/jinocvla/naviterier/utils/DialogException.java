package cz.cvut.fel.jinocvla.naviterier.utils;

import android.app.Activity;

import cz.cvut.fel.jinocvla.naviterier.services.exception.AbstracetException;

/**
 * Created by usul on 2.4.2014.
 */
public class DialogException extends AbstracetException {

    public DialogException(Activity activity, String title, String message) {
        this.activity = activity;
        this.message = message;
        this.title = title;
    }
}
