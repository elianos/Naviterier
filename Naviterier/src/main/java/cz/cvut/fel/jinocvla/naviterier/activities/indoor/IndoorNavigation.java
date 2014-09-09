package cz.cvut.fel.jinocvla.naviterier.activities.indoor;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.main.MainActivity;
import cz.cvut.fel.jinocvla.naviterier.activities.main.SchemeActivity;
import cz.cvut.fel.jinocvla.naviterier.activities.outdoor.OutdoorNavigation;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Connection;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Point;
import cz.cvut.fel.jinocvla.naviterier.services.impl.DummyNavigationServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.NavigationService;
import cz.cvut.fel.jinocvla.naviterier.services.impl.NavigationServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.SchemeStorageService;
import cz.cvut.fel.jinocvla.naviterier.services.TextToSpeechService;
import cz.cvut.fel.jinocvla.naviterier.services.impl.SchemeStorageServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.TextToSpeechServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.utils.DialogException;
import cz.cvut.fel.jinocvla.naviterier.utils.GraphUtil;
import cz.cvut.fel.jinocvla.naviterier.utils.GraphUtilException;

public class IndoorNavigation extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SchemeActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private Scheme scheme;

    private Long from_id;

    private Long to_id;

    private Integer current_id;

    private Point from;

    private Point to;

    private NavigationService navigation;

    private TextToSpeechService textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_navigation);

        textToSpeech = new TextToSpeechServiceImpl(this);

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

        if (current_id != null) {
            navigation.goNextTimes(current_id);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        PlaceholderFragment pf = new PlaceholderFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container, pf)
                .commit();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        current_id = navigation.getPosition();

        // Store data context
        SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(this);
        schemeStorageService.storeContext(getSchemeActivity(), State.COMPLETE);

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
                Intent intent = new Intent(IndoorNavigation.this, MainActivity.class);
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
        super.onPause();

        current_id = navigation.getPosition();

        // Store data context
        SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(this);
        schemeStorageService.storeCurrentPosition(getSchemeActivity(), State.COMPLETE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public IndoorNavigation getIndoorNavigation() {
        return IndoorNavigation.this;
    }


    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
        return IndoorNavigation.class;
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
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {

        }

        public static PlaceholderFragment getInstance() {
            PlaceholderFragment placeholderFragment = new PlaceholderFragment();
            return placeholderFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_indoor_navigation, container, false);

            IndoorNavigation indoorNavigation = (IndoorNavigation) getActivity();

            final NavigationService navigationService = indoorNavigation.navigation;
            final TextToSpeechService textToSpeechService = indoorNavigation.textToSpeech;

            final TextView navigationText = (TextView)rootView.findViewById(R.id.current_step);
            navigationText.setText(navigationService.getCurrent());

            final Button previous = (Button)rootView.findViewById(R.id.btn_previous);
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (navigationService.hasPrevious()) {
                        navigationText.setText(navigationService.getPrevious());
                        textToSpeechService.read(navigationService.getCurrent());
                    }
                }
            });

            final Button next = (Button)rootView.findViewById(R.id.btn_next);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (navigationService.hasNext()) {
                        navigationText.setText(navigationService.getNext());
                        textToSpeechService.read(navigationService.getCurrent());
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
                                Intent intent = new Intent(getActivity(), IndoorFrom.class);
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
                    navigationText.setText(navigationService.getCurrent());
                    textToSpeechService.read(navigationService.getCurrent());
                }
            });

            return rootView;
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

    }

}
