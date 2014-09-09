package cz.cvut.fel.jinocvla.naviterier.services.exception;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.main.MainActivity;

/**
 * Created by usul on 17.3.14.
 */
public class AbstracetException extends Exception {

    protected Activity activity;

    protected String message;

    protected String title;

    public AlertDialog.Builder preparedBuilder() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setTitle(title);
        return builder;
    }

    public void getCancelDialog() {
        final AlertDialog.Builder builder = preparedBuilder();
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    public void getFinishDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();


                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();

                    }
                }
        );
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }
}
