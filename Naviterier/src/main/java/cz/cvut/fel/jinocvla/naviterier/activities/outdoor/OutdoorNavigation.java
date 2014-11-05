package cz.cvut.fel.jinocvla.naviterier.activities.outdoor;

import java.util.List;
import java.util.Locale;
import java.util.Timer;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.main.CallActivity;
import cz.cvut.fel.jinocvla.naviterier.activities.main.MainActivity;
import cz.cvut.fel.jinocvla.naviterier.activities.main.SchemeActivity;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Point;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;
import cz.cvut.fel.jinocvla.naviterier.services.LocationService;
import cz.cvut.fel.jinocvla.naviterier.services.NavigationService;
import cz.cvut.fel.jinocvla.naviterier.services.SchemeStorageService;
import cz.cvut.fel.jinocvla.naviterier.services.TextToSpeechService;
import cz.cvut.fel.jinocvla.naviterier.services.impl.DummyNavigationServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.LocationService.LocTimerTask;
import cz.cvut.fel.jinocvla.naviterier.services.impl.LocationService.LocationCallback;
import cz.cvut.fel.jinocvla.naviterier.services.impl.LocationServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.NavigationServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.SchemeStorageServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.TextToSpeechServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.utils.DialogException;
import cz.cvut.fel.jinocvla.naviterier.utils.GraphUtil;
import cz.cvut.fel.jinocvla.naviterier.utils.GraphUtilException;
import cz.cvut.fel.jinocvla.naviterier.utils.LocationUtil;

public class OutdoorNavigation extends Activity implements SchemeActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
//    private NavigationDrawerFragment mNavigationDrawerFragment;

    private Scheme scheme;

    private Long from_id;

    private Long to_id;

    private Integer current_id;

    private Point from;

    private Point to;

    private NavigationService navigation;

    private TextToSpeechService textToSpeech;

    protected LocationService location;

    private Timer timer = new Timer();

    private SmsManager smsManager;

    PlaceholderFragment pf;

    MenuFragment menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_navigation);



        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                setTitle(mSectionsPagerAdapter.getPageTitle(i));
                getWindow().getDecorView()
                        .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mViewPager.setCurrentItem(1);

        textToSpeech = new TextToSpeechServiceImpl(this);

        smsManager = SmsManager.getDefault();

        // Load data context
        SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(this);
        schemeStorageService.loadContext(getSchemeActivity(), State.COMPLETE);

        List<Point> points = this.scheme.getPoints();
        for (Point point : points) {
            if (point.getId().equals(from_id)) {
                this.from = point;
            }
            if (point.getId().equals(to_id)) {
                this.to = point;
            }
        }

        GraphUtil.createGraph(this.scheme);
        List<Connection> connections = null;

        try {
            connections = GraphUtil.dijkstra(this.scheme, this.from, this.to);
            navigation = new NavigationServiceImpl(connections);

            AlertDialog.Builder ebuilder = new AlertDialog.Builder(this);
            ebuilder.setTitle(R.string.navigation_start).setMessage(R.string.navigation_instruction);

            ebuilder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();

                }});

            AlertDialog edialog = ebuilder.create();
            edialog.show();

        } catch (GraphUtilException e) {
            DialogException exception = new DialogException(this, getString(e.getTitle()), getString(e.getEmessage()));
            exception.getFinishDialog();
            navigation = new DummyNavigationServiceImpl();
        }
        location = new LocationServiceImpl(this);

        if (!location.isGpsEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.gps_disable);
            builder.setMessage(R.string.gps_want_to_start);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Launch settings, allowing user to make a change
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    location.setActive(false);
                }
            });

            builder.show();
        }

        location.tryStart();

        if (current_id != null) {
            navigation.goNextTimes(current_id);
        }

        pf = PlaceholderFragment.getInstance();
        menu = MenuFragment.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        current_id = navigation.getPosition();

        // Store data context
        SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(this);
        schemeStorageService.storeContext(getSchemeActivity(), State.COMPLETE);

        // SchemeLocation Service destroy
        location.destroy();

        // Cancel timer
        timer.cancel();

        // Destroy TTS
        textToSpeech.destroy();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder ebuilder = new AlertDialog.Builder(this);
        ebuilder.setTitle(R.string.exit_navigation).setMessage(R.string.really_exit);

        ebuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                // Start context
                Intent intent = new Intent(OutdoorNavigation.this, MainActivity.class);
                startActivity(intent);
                finish();

            }});

        ebuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
        {
            public void onClick (DialogInterface dialog,int id){
                dialog.cancel();
            }});

        AlertDialog edialog = ebuilder.create();
        edialog.show();
    }

    @Override
    protected void onPause() {
        location.tryStop();

        current_id = navigation.getPosition();

        // Store data context
        SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(this);
        schemeStorageService.storeCurrentPosition(getSchemeActivity(), State.COMPLETE);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        location.tryStart();
//        pf.startTimer();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
//        }
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Scheme getScheme() {
        return this.scheme.cleanToSerialization();
    }

    @Override
    public Long getFrom() {
        return this.from.getId();
    }

    @Override
    public Long getTo() {
        return this.to.getId();
    }

    @Override
    public Integer getCurrent() {
        return this.current_id;
    }

    @Override
    public Class getClassName() {
        return OutdoorNavigation.class;
    }

    @Override
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    @Override
    public void setFrom(Long from) {
        this.from_id = from;
    }

    @Override
    public void setTo(Long to) {
        this.to_id = to;
    }

    @Override
    public void setCurrent(Integer current) {
        this.current_id = current;
    }

    @Override
    public SchemeActivity getSchemeActivity() {
        return this;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return menu;
            } else {
                return pf;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.menu);
                case 1:
                    return getString(R.string.navigation);
            }
            return null;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        TextView distanceText;

        private LocationService locationService;

        public static PlaceholderFragment getInstance() {
            PlaceholderFragment placeholderFragment = new PlaceholderFragment();
            return placeholderFragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_outdoor_navigation, container, false);

            OutdoorNavigation outdoorNavigation = (OutdoorNavigation) getActivity();

            final NavigationService navigationService = outdoorNavigation.navigation;
            final TextToSpeechService textToSpeechService = outdoorNavigation.textToSpeech;
            locationService = outdoorNavigation.location;

            final Button previous = (Button)rootView.findViewById(R.id.btn_previous);
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (navigationService.hasPrevious()) {
                        textToSpeechService.read(navigationService.getPrevious());
                    }
                }
            });

            final Button next = (Button)rootView.findViewById(R.id.btn_next);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (navigationService.hasNext()) {
                        textToSpeechService.read(navigationService.getNext());
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.navigation_done);
                        builder.setMessage(R.string.want_navigate_to);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                SchemeActivity schemeActivity = (SchemeActivity) getActivity();

                                // Store data context
                                SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(getActivity());
                                schemeStorageService.storeContext(schemeActivity, State.PROGRESS);

                                // Start context
                                Intent intent = new Intent(getActivity(), OutdoorFrom.class);
                                startActivity(intent);
                                getActivity().finish();

                            }
                        });

                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                // Start context
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        });

                        builder.show();

                    }
                }
            });

            final Button current = (Button)rootView.findViewById(R.id.btn_current);
            current.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textToSpeechService.read(navigationService.getCurrent());
                }
            });

            distanceText = (TextView)rootView.findViewById(R.id.distance_text);

            SchemeLocation next_loc = navigationService.getNextLocation();
            Location cur_loc = locationService.getLocation();
            if (cur_loc == null) {
                distanceText.setText(getActivity().getString(R.string.point_distance_unavailable));
            } else {
                distanceText.setText(getActivity().getString(R.string.point_distance, (int) LocationUtil.calcDistanceBetween(next_loc, cur_loc)));
            }

            if (locationService.isActive()) {
                startTimer();
            }

            return rootView;
        }

        public void startTimer() {


            OutdoorNavigation outdoorNavigation = (OutdoorNavigation) getActivity();

            Timer timer = outdoorNavigation.timer;

            LocTimerTask timerTask = new LocTimerTask();
            timerTask.setCallback(new TimerCallback(locationService, distanceText));
            timerTask.setLocationManager(locationService.getLocationManager());
            timer.schedule(timerTask, 0, 5000);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        /**
         * Timer callback for refresing distance to end of interesting point
         */
        private class TimerCallback implements LocationCallback {

            private final TextView distanceText;

            private final LocationService locationService;

            private final Handler mHandler;

            private TimerCallback(LocationService locationService, TextView distanceText) {
                this.locationService = locationService;
                this.distanceText = distanceText;
                this.mHandler = new Handler();
            }

            @Override
            public void call(Location location) {

                OutdoorNavigation outdoorNavigation = (OutdoorNavigation) getActivity();

                final NavigationService navigationService = outdoorNavigation.navigation;

                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        if (navigationService.hasNext()) {
                            SchemeLocation next_loc = navigationService.getNextLocation();
                            Location cur_loc = locationService.getLocation();
                            distanceText.setText(getActivity().getString(R.string.point_distance, (int) LocationUtil.calcDistanceBetween(next_loc, cur_loc)));
                        }
                    }
                });
            }

        }
    }

    public static class MenuFragment extends Fragment {

        private ListView menuList;

        public static MenuFragment getInstance() {
            return new MenuFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            menuList = (ListView) inflater.inflate(
                    R.layout.fragment_navigation_menu, container, false);
            menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectItem(position);
                }
            });
            menuList.setAdapter(new ArrayAdapter<String>(
                    getActivity().getBaseContext(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    new String[]{
                            getString(R.string.nav_distance),
                            getString(R.string.test_gps_statue),
                            getString(R.string.send_sos_sms),
                            getString(R.string.call_help),
                            getString(R.string.exit_navigation),
                    }));
            return menuList;
        }

        private void selectItem(int position) {
            final OutdoorNavigation outdoorNavigation = (OutdoorNavigation) getActivity();

            switch (position) {

                // Check distance
                case 0:
                    if (!outdoorNavigation.location.isActive()) {
                        AlertDialog.Builder gpsDialog = new AlertDialog.Builder(outdoorNavigation);
                        gpsDialog.setTitle(R.string.service_unavailable);
                        gpsDialog.setMessage(R.string.gps_disable);
                        gpsDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        gpsDialog.show();

                    } else {

                        AlertDialog.Builder distanceDialog = new AlertDialog.Builder(outdoorNavigation);
                        distanceDialog.setTitle(R.string.finish_distance);

                        float finish_d = outdoorNavigation.navigation.getDistanceToFinish();

                        Location cur_loc = outdoorNavigation.location.getLocation();
                        SchemeLocation next_loc = outdoorNavigation.navigation.getNextLocation();

                        float next_d = LocationUtil.calcDistanceBetween(next_loc, cur_loc);
                        distanceDialog.setMessage(getActivity().getString(R.string.distance_to_finish, (int) (finish_d + next_d)));

                        distanceDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog dDialog = distanceDialog.create();
                        dDialog.show();
                    }

                    break;

                // Check the statue of network
                case 1:
                    if (!outdoorNavigation.location.isActive()) {
                        AlertDialog.Builder gpsDialog = new AlertDialog.Builder(outdoorNavigation);
                        gpsDialog.setTitle(R.string.service_unavailable);
                        gpsDialog.setMessage(R.string.gps_disable);
                        gpsDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        gpsDialog.show();

                    } else {
                        AlertDialog.Builder networkDialog = new AlertDialog.Builder(outdoorNavigation);
                        networkDialog.setTitle(R.string.network_statue);


                        networkDialog.setMessage(getActivity().getString(R.string.c_net_status, (int) outdoorNavigation.location.getAccuracy()));


                        networkDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog nDialog = networkDialog.create();
                        nDialog.show();

                    }

                    break;

                // SOS sms
                case 2:

                    final SmsManager smsManager = SmsManager.getDefault();
                    final Handler mHandler = new Handler(Looper.getMainLooper());

                    AlertDialog.Builder dbuilder = new AlertDialog.Builder(outdoorNavigation);
                    dbuilder.setTitle(R.string.want_send_sos_sms).setMessage(R.string.sos_sms_info);
                    final NavigationService ns = outdoorNavigation.navigation;
                    final Scheme sch = outdoorNavigation.scheme;
                    final LocationService lcs = outdoorNavigation.location;
                    // Yes SOS!
                    dbuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            final String phoneNumber = sp.getString("sos_phone_number", "");

                            // Send sms
                            NavigationService navigationService = ns;
                            Connection connection = navigationService.getCurrentConnection();
                            String fromKey = "";
                            String toKey = "";
                            if (connection.getFromPoint() != null) {
                                fromKey = connection.getFromPoint().getKeywords();
                            }
                            if (connection.getTo() != null) {
                                toKey = connection.getToPoint().getKeywords();
                            }
                            String schemeName = sch.getName();
                            LocationService locationService = lcs;
                            Location location = locationService.getLocation();

                            double lon = 0.0;
                            double lat  = 0.0;
                            if (location != null) {
                                lon = location.getLongitude();
                                lat = location.getLatitude();
                            }

                            final String sms = String.format(getActivity().getString(R.string.outdoor_sms_message, fromKey, toKey, schemeName, lat, lon));
                            try {
                                smsManager.sendTextMessage(phoneNumber, null, sms, null, null);

                                AlertDialog.Builder dbuilder = new AlertDialog.Builder(outdoorNavigation);
                                dbuilder.setTitle(R.string.sms_sent);
                                dbuilder.setMessage(R.string.sms_success);
                                dbuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        }
                                );
                                dbuilder.show();
                            } catch (Exception e) {
                                AlertDialog.Builder dbuilder = new AlertDialog.Builder(outdoorNavigation);
                                dbuilder.setTitle(R.string.sms_not_send);
                                dbuilder.setMessage(R.string.sms_not_send_reason);
                                dbuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        }
                                );
                                dbuilder.show();
                            }
                        }});
                    // No Download!
                    dbuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick (DialogInterface dialog,int id){
                            dialog.cancel();
                        }});

                    AlertDialog dialog = dbuilder.create();
                    dialog.show();

                    break;

                // Call for help
                case 3:
                    Intent intent = new Intent(getActivity(), CallActivity.class);
                    startActivity(intent);
                    break;

                // Exit
                case 4:

                    AlertDialog.Builder ebuilder = new AlertDialog.Builder(outdoorNavigation);
                    ebuilder.setTitle(R.string.exit_navigation).setMessage(R.string.really_exit);

                    ebuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            // Start context
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }});

                    ebuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick (DialogInterface dialog,int id){
                            dialog.cancel();
                        }});

                    AlertDialog edialog = ebuilder.create();
                    edialog.show();

                    break;

                default:
            }
        }
    }
}
