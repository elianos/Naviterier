package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.conn.ClientConnectionManager;

import cz.cvut.fel.jinocvla.naviterier.services.ConnectionService;

/**
 * Created by usul on 25.4.2014.
 */
public class ConnectionServiceImpl implements ConnectionService {

    private Context activity;

    public ConnectionServiceImpl(Context activity) {
        this.activity = activity;
    }

    public boolean checkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

    public boolean checkWifiConnection() {
        if (!checkConnection()) return false;

        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) return true;

        return false;
    }

}
