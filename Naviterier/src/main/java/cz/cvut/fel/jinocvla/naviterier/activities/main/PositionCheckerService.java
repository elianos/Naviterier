package cz.cvut.fel.jinocvla.naviterier.activities.main;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.settings.SettingsActivity;
import cz.cvut.fel.jinocvla.naviterier.services.AccelerometerService;
import cz.cvut.fel.jinocvla.naviterier.services.ConnectionService;
import cz.cvut.fel.jinocvla.naviterier.services.FileService;
import cz.cvut.fel.jinocvla.naviterier.services.LocationService;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.impl.AccelerometerServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.ConnectionServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.FileServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.LocationServiceImpl;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class PositionCheckerService extends Service {

    private AccelerometerService accelerometerService;

    private LocationService locationService;

    private ConnectionService connectionService;

    private FileService fileService;

    private static final int DATA_CATCH_INTERVAL = 5000;

    private static final int LOCATION_CATCH_INTERVAL = 10000;

    private static final int CHECK_INTERVAL = 15;

    private static final String FOLDER = "naviterier";

    private static final String FILE_NAME = "checked_postiotins.json";

    private static final String URL = "http://naviterier.appspot.com/capture";

    private int missCount = 0;


    private StartListener startListener;

    private  String account;

    private Timer timer;

    public PositionCheckerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        accelerometerService = new AccelerometerServiceImpl(this.getBaseContext());
        locationService = new LocationServiceImpl(getBaseContext());
        connectionService = new ConnectionServiceImpl(getBaseContext());
        fileService = new FileServiceImpl();
        startListener = new StartListener();

        // Register service to notification

        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.position_checker), System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getString(R.string.position_checker), getString(R.string.position_checker_running), pendingIntent);
        startForeground(SettingsActivity.POSITION_CHECKER_SERVICE, notification);

        // Try take phone number
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();

        account = "anonymous";

        for (Account ac : accounts) {
            if (ac.type.equals("com.google")) {
                account = ac.name;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startCapture();


        return START_STICKY;
    }

    public void startCapture() {
        missCount = 0;


        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                List<Double> data = accelerometerService.getData(DATA_CATCH_INTERVAL);

                // Testing of context
                if (stepDetecion(data)) {

                    LocationManager locationManager = locationService.getLocationManager();

                    // If aimed store position
                    if (startListener.started == true) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Long delta = new Date().getTime() - location.getTime();
                        if (delta > LOCATION_CATCH_INTERVAL) {
                            if (missCount > 1) {
                                stopLocation();
                            } else {
                                missCount++;
                            }
                        } else {
                            storeLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                        }
                    } else {
                        // Register listener
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, startListener, getMainLooper());

                        // Wait 10 seconds for position takes
                        int j = 0;
                        while (j < 10) {
                            j++;
                            try {
                                synchronized (this) {
                                    this.wait(1000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (startListener.started == true) {
                                j = 100;
                            }
                        }


                        // Check if position is already find (problem inside of buildings)
                        if (startListener.started == false) {
                            stopLocation();
                            missCount = 0;
                        }
                    }
                } else {

                    // If it is already started
                    if (startListener.started) {

                        // if started and miss more than 1 stop getting gps
                        if (missCount > 1) {
                            stopLocation();
                        } else {
                            // increment miss count
                            missCount++;
                        }
                    }
                }
            if(connectionService.checkConnection()) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(PositionCheckerService.this);
                if (connectionService.checkWifiConnection() || !sp.getBoolean("auto_trace_recording", false)) {
                    try {
                        sendData(PositionCheckerService.this.getBaseContext());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 0, LOCATION_CATCH_INTERVAL);
    }

    public void stopLocation() {
        LocationManager locationManager = locationService.getLocationManager();
        locationManager.removeUpdates(startListener);
        missCount = 0;
        startListener.restart();
    }

    @Override
    public void onDestroy() {
        timer.cancel();

        stopForeground(true);
        stopLocation();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean stepDetecion(List<Double> data) {

        // Take sampling rate
        double sampleRate = data.size()/ (DATA_CATCH_INTERVAL/1000);

        // Convert date for FFT real and imagine number
        double[] fftData = new double[data.size()*2];
        int i = 0;
        for (double val : data) {
            fftData[2 * i + 1] = 0;
            fftData[2* i++] = val;
        }

        // Initialization of FFT
        DoubleFFT_1D doubleFFT_1D = new DoubleFFT_1D(data.size());

        // FFT action
        doubleFFT_1D.complexForward(fftData);


        double max_aplitude = -1.0;
        double index = 0;

        // Calc lower and upper bound of frequency
        double lower_bound = 2* ((0.7 * data.size()) / sampleRate );
        double upper_bound = 2* ((2 * data.size()) / sampleRate );

        for(int k = Math.round((float)lower_bound); k < upper_bound; k += 2) {

            // Merge real and imagin value of FFT
            double aplitude = Math.sqrt(Math.pow(fftData[k], 2.) + Math.pow(fftData[k+1], 2.));

            // Check biggest value
            if (k > 1 && aplitude > max_aplitude) {
                max_aplitude = aplitude;
                index = k;
            }
        }

        // Find dominant frequency
        double dominantFreq = ((index / 2.0) / data.size()) * sampleRate;

        // Check if maximal amplitude is biggest then 70
        if (dominantFreq < 2 && max_aplitude > 100) {
            return true;
        }
        return false;
    }


    private void storeLocation(Location location) {
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        String patern = "{'dateTime' : '%s', 'user' : '%s', 'latitude' : %f, 'longitude' : %f, 'accuracy' : %f}, \n";
        Date date = new Date();
        String output = String.format(Locale.US, patern,
                format.format(date),
                account,
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy());
        File file = fileService.getExternalStorageFile(FOLDER, FILE_NAME);
        try {
            if (!file.exists()) {
                fileService.overrideFileByString(file, output);
            } else {
                fileService.appendStringToFile(file, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        missCount = 0;
    }

    public class StartListener implements LocationListener {

        public boolean started = false;

        public void restart() {
            started = false;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (started == false) {
                storeLocation(location);
                started = true;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }


    public static void sendData(Context context) throws IOException, JSONException {
        final FileService fs = new FileServiceImpl();
        File file = fs.getExternalStorageFile(FOLDER, FILE_NAME);
        if (!file.exists()) return;

        final Long size = file.getTotalSpace();
        final String data = fs.readStringFromFile(file);


        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final JSONArray jsonArray;
        String workdata = data.trim();

        if (workdata.length() != 0) {
            if (workdata.charAt(workdata.length() - 1) == ',') {
                jsonArray = new JSONArray("[" + workdata.substring(0, workdata.length() - 1) + "]");
            } else {
                jsonArray = new JSONArray("[" + workdata + "]");
            }


            JsonArrayReq jsonArrayReq = new JsonArrayReq(Request.Method.POST, URL, jsonArray.toString(), new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    File file = fs.getExternalStorageFile(FOLDER, FILE_NAME);
                    try {
                        if (file.getTotalSpace() == size) {
                            fs.overrideFileByString(file, "");
                        } else {
                            int start = data.length();
                            String newData = fs.readStringFromFile(file);
                            String newPart = newData.substring(start);

                            fs.overrideFileByString(file, newPart);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            jsonArrayReq.setShouldCache(false);

            requestQueue.add(jsonArrayReq);
        }
    }

    private static class JsonArrayReq extends JsonRequest<String> {

        public JsonArrayReq(int method, String url, String requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, requestBody, listener, errorListener);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            if (response.statusCode == 200) {
                return Response.success("",
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(null);
            }
        }
    }
}
