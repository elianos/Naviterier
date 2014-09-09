package cz.cvut.fel.jinocvla.naviterier.activities.outdoor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.main.SchemeActivity;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.models.scheme.Point;
import cz.cvut.fel.jinocvla.naviterier.services.SchemeStorageService;
import cz.cvut.fel.jinocvla.naviterier.services.impl.SchemeStorageServiceImpl;

public class OutdoorTo extends Activity implements SchemeActivity {

    private ListViewFragment listView;

    private Scheme scheme;

    private Long from;

    private Long to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_to);
        setupActionBar();


        // Load data context
        SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(this);
        schemeStorageService.loadContext(getSchemeActivity(), State.PROGRESS);

        this.listView = ListViewFragment.getInstance();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, this.listView)
                    .commit();
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Scheme getScheme() {
        return this.scheme;
    }

    @Override
    public Long getFrom() {
        return this.from;
    }

    @Override
    public Long getTo() {
        return this.to;
    }

    @Override
    public Integer getCurrent() {
        return null;
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
        this.from = from;
    }

    @Override
    public void setTo(Long to) {
        this.to = to;
    }

    @Override
    public void setCurrent(Integer current) {
        return;
    }

    @Override
    public SchemeActivity getSchemeActivity() {
        return this;
    }


    public static class ListViewFragment extends Fragment {

        List<Point> items = new ArrayList<Point>();

        ListView mListView;

        EditText mEditText;

        ArrayAdapter<Point> adapter;

        public static ListViewFragment getInstance() {
            ListViewFragment listViewFragment = new ListViewFragment();
            return listViewFragment;
        }

        public ListViewFragment(){

        }

        public void setItems(List<Point> points) {
            this.items = points;
        }

        public void filter(CharSequence charSequence) {
            adapter.getFilter().filter(charSequence);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            SchemeActivity schemeActivity = (SchemeActivity) getActivity();

            // Store data context
            SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(getActivity());
            schemeStorageService.loadContext(schemeActivity, State.PROGRESS);

            setItems(schemeActivity.getScheme().getPoints());

            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.fragment_list_view, container, false);


            mListView = (ListView) ll.findViewById(R.id.listView);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openItem((Point) parent.getItemAtPosition(position));
                }
            });

            adapter = new ArrayAdapter<Point>(
                    getActivity().getBaseContext(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    this.items);

            mListView.setAdapter(adapter);


            mEditText = (EditText) ll.findViewById(R.id.textEdit);

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    filter(charSequence);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            adapter.notifyDataSetChanged();

            return ll;
        }

        public void openItem(Point point) {
            SchemeActivity schemeActivity = (SchemeActivity) getActivity();

            if (point.getId().equals(schemeActivity.getFrom())) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.navigation_inposible);
                builder.setMessage(R.string.same_from_to);
                builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();

            } else {
                schemeActivity.setTo(point.getId());

                Intent intent = new Intent(getActivity(), OutdoorNavigation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Store data context
                SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(getActivity());
                schemeStorageService.storeContext(schemeActivity, State.COMPLETE);

                startActivity(intent);
            }
        }

    }


}
