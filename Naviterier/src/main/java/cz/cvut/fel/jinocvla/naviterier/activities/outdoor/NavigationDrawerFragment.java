package cz.cvut.fel.jinocvla.naviterier.activities.outdoor;


import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.main.CallActivity;
import cz.cvut.fel.jinocvla.naviterier.activities.main.MainActivity;
import cz.cvut.fel.jinocvla.naviterier.activities.main.SchemeActivity;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.SchemeLocation;
import cz.cvut.fel.jinocvla.naviterier.services.LocationService;
import cz.cvut.fel.jinocvla.naviterier.services.impl.LocationServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.NavigationService;
import cz.cvut.fel.jinocvla.naviterier.utils.LocationUtil;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {

            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mDrawerListView.setAdapter(new ArrayAdapter<String>(
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
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.closeDrawers();

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        switch (position) {

            // Check distance
            case 0:
                if (!mCallbacks.getLocationService().isActive()) {
                    AlertDialog.Builder gpsDialog = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
                    gpsDialog.setTitle(R.string.service_unavailable);
                    gpsDialog.setMessage(R.string.gps_disable);
                    gpsDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    gpsDialog.show();

                } else {

                    AlertDialog.Builder distanceDialog = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
                    distanceDialog.setTitle(R.string.finish_distance);

                    float finish_d = mCallbacks.getNavigationService().getDistanceToFinish();

                    Location cur_loc = mCallbacks.getLocationService().getLocation();
                    SchemeLocation next_loc = mCallbacks.getNavigationService().getNextLocation();

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
                if (!mCallbacks.getLocationService().isActive()) {
                    AlertDialog.Builder gpsDialog = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
                    gpsDialog.setTitle(R.string.service_unavailable);
                    gpsDialog.setMessage(R.string.gps_disable);
                    gpsDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    gpsDialog.show();

                } else {
                    AlertDialog.Builder networkDialog = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
                    networkDialog.setTitle(R.string.network_statue);


                    networkDialog.setMessage(getActivity().getString(R.string.c_net_status, (int) mCallbacks.getLocationService().getAccuracy()));


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

                AlertDialog.Builder dbuilder = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
                dbuilder.setTitle(R.string.want_send_sos_sms).setMessage(R.string.sos_sms_info);
                // Yes SOS!
                dbuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        final String phoneNumber = sp.getString("sos_phone_number", "");

                        // Send sms
                        NavigationService navigationService = mCallbacks.getNavigationService();
                        Connection connection = navigationService.getCurrentConnection();
                        String fromKey = "";
                        String toKey = "";
                        if (connection.getFromPoint() != null) {
                            fromKey = connection.getFromPoint().getKeywords();
                        }
                        if (connection.getTo() != null) {
                            toKey = connection.getToPoint().getKeywords();
                        }
                        String schemeName = mCallbacks.getSchemeName();
                        LocationService locationService = mCallbacks.getLocationService();
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

                            AlertDialog.Builder dbuilder = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
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
                            AlertDialog.Builder dbuilder = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
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

                AlertDialog.Builder ebuilder = new AlertDialog.Builder(mCallbacks.getOutdoorNavigation());
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {

        public NavigationService getNavigationService();

        public LocationService getLocationService();

        public OutdoorNavigation getOutdoorNavigation();

        public SchemeActivity getSchemeActivity();

        public String getSchemeName();

        public SmsManager getSmsManager();
    }
}
