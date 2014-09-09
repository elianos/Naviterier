package cz.cvut.fel.jinocvla.naviterier.activities.indoor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
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
import cz.cvut.fel.jinocvla.naviterier.activities.outdoor.OutdoorFrom;
import cz.cvut.fel.jinocvla.naviterier.component.DownloadDialog;
import cz.cvut.fel.jinocvla.naviterier.models.MapScheme;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.services.ConnectionService;
import cz.cvut.fel.jinocvla.naviterier.services.MapService;
import cz.cvut.fel.jinocvla.naviterier.services.SchemeStorageService;
import cz.cvut.fel.jinocvla.naviterier.services.exception.FileException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.StorageUnavailableException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.UpdateException;
import cz.cvut.fel.jinocvla.naviterier.services.impl.ConnectionServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.MapServiceImpl;
import cz.cvut.fel.jinocvla.naviterier.services.impl.SchemeStorageServiceImpl;

public class IndoorMapSelection extends Activity implements SchemeActivity {

    private ListViewFragment listView;

    private MapService ms;

    private ConnectionService connectionService;

    private Scheme scheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ms);
        setupActionBar();
        this.connectionService = new ConnectionServiceImpl(this);
        this.ms = new MapServiceImpl(this, MapServiceImpl.INDOOR_CONTEXT);

        this.listView = ListViewFragment.getInstance();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, this.listView)
                    .commit();
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filtering).setMessage(R.string.filtering_tutor);

        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

//        builder.show();
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
        return null;
    }

    @Override
    public Long getTo() {
        return null;
    }

    @Override
    public Integer getCurrent() {
        return null;
    }

    @Override
    public Class getClassName() {
        return null;
    }

    @Override
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    @Override
    public void setFrom(Long from) {
        return;
    }

    @Override
    public void setTo(Long to) {
        return;
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

        List<MapScheme> items = new ArrayList<MapScheme>();

        MapService mapService;

        ConnectionService connectionService;

        ListView mListView;

        EditText mEditText;

        ArrayAdapter<MapScheme> adapter;

        public static ListViewFragment getInstance() {
            ListViewFragment listViewFragment = new ListViewFragment();
            return listViewFragment;
        }

        public ListViewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            IndoorMapSelection indoorMapSelection = (IndoorMapSelection) getActivity();
            this.mapService = indoorMapSelection.ms;
            this.connectionService = indoorMapSelection.connectionService;


            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.fragment_list_view, container, false);

            mListView = (ListView) ll.findViewById(R.id.listView);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openItem((MapScheme) parent.getItemAtPosition(position));
                }
            });

            adapter = new ArrayAdapter<MapScheme>(
                    getActivity().getBaseContext(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    this.items) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setEnabled(getItem(position).isLocaleCopy());
                    return view;
                }
            };

            mListView.setAdapter(adapter);


            mEditText = (EditText) ll.findViewById(R.id.textEdit);

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    adapter.getFilter().filter(charSequence);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            return ll;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            try {
                try {
                    items.addAll(mapService.getMapList());
                    adapter.notifyDataSetChanged();
                } catch (FileException e) {
                    final AlertDialog.Builder builder = e.preparedBuilder();
                    if (connectionService.checkConnection()) {
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mapService.tryDownloadFile(items, adapter);
                            }
                        });
                    } else {
                        builder.setMessage(R.string.maps_list_unavailable_exit);
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                getActivity().finish();
                            }
                        });
                    }
                    builder.show();
                } catch (MarshallingException e) {
                    e.getFinishDialog();
                } catch (UpdateException e) {
                    try {
                        items.addAll(mapService.tryUpdateList());
                        adapter.notifyDataSetChanged();
                    } catch (MarshallingException e1) {
                        e.getFinishDialog();
                    }
                    if (connectionService.checkConnection()) {
                        mapService.tryUpdateFile(items, adapter);
                    }
                }
            } catch (StorageUnavailableException e) {
                e.getFinishDialog();
            }
        }

        /**
         * Open item in list
         * @param mapScheme selected mapScheme
         */
        private void openItem(final MapScheme mapScheme) {
            // If mapScheme is not downloaded yet
            if (!mapScheme.isLocaleCopy()) {

                // Alert window to confirm download
                AlertDialog.Builder dbuilder = new AlertDialog.Builder(getActivity());
                dbuilder.setTitle(R.string.no_local_copy).setMessage(R.string.to_download);
                // Yes Download!
                dbuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Close dialog
                                dialog.cancel();
                                try {
                                    final DownloadManager dm = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                                    final long downloadId;

                                    downloadId = mapService.downloadMap(mapScheme, getActivity());


                                    final DownloadDialog downloadDialog = new DownloadDialog(getActivity(), downloadId);
                                    // Open progress bar
                                    downloadDialog.setTitle(R.string.please_wait);
                                    downloadDialog.setMessage(getActivity().getString(R.string.downloading));
                                    downloadDialog.setCancelable(true);
                                    downloadDialog.setProgressStyle(downloadDialog.STYLE_HORIZONTAL);
                                    downloadDialog.show();
                                    downloadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            if (downloadDialog.getProgress() == downloadDialog.getMax()) {
                                                new RefreshList().refreshListAfterDownload();
                                                mapScheme.setLocaleCopy(true);
                                            } else {
                                                AlertDialog.Builder dbuilder = new AlertDialog.Builder(getActivity());
                                                dbuilder.setTitle(R.string.download_failed);
                                                dbuilder.setMessage(R.string.data_not_saved);
                                                dbuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        }
                                                );
                                                AlertDialog dialog = dbuilder.create();
                                                dialog.show();
                                            }
                                        }
                                    });

                                    downloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialogInterface) {

                                        }
                                    });
                                    downloadDialog.show();
                                } catch (StorageUnavailableException e) {
                                    e.getFinishDialog();
                                }

                            }}
                );
                // No Download!
                dbuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick (DialogInterface dialog,int id){
                        dialog.cancel();
                    }});

                AlertDialog dialog = dbuilder.create();
                dialog.show();

            } else {
                try {
                    Intent intent = new Intent(getActivity(), IndoorFrom.class);

                    SchemeActivity schemeActivity = (SchemeActivity) getActivity();

                    schemeActivity.setScheme(mapService.openMap(mapScheme));

                    SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(getActivity());
                    schemeStorageService.storeContext(schemeActivity, SchemeActivity.State.PROGRESS);

                    startActivity(intent);
                } catch (StorageUnavailableException e) {
                    e.getCancelDialog();
                } catch (MarshallingException e) {
                    e.getCancelDialog();
                } catch (FileException e) {
                    e.getCancelDialog();
                }
            }

        }

        public class RefreshList {
            public void refreshList() {
                adapter.notifyDataSetChanged();
            }

            public void refreshListAfterDownload() {
                refreshList();

                AlertDialog.Builder dbuilder = new AlertDialog.Builder(getActivity());
                dbuilder.setTitle(R.string.download_complete);
                dbuilder.setMessage(R.string.data_saved);
                dbuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
                AlertDialog dialog = dbuilder.create();
                dialog.show();
            }
        }

    }

}
