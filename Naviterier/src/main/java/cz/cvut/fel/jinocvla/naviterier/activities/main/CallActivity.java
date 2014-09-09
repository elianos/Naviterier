package cz.cvut.fel.jinocvla.naviterier.activities.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.models.Contact;
import cz.cvut.fel.jinocvla.naviterier.services.ContactsService;
import cz.cvut.fel.jinocvla.naviterier.services.exception.FileException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.StorageUnavailableException;
import cz.cvut.fel.jinocvla.naviterier.services.impl.ContactsServiceImpl;

public class CallActivity extends Activity {

    public TwoLineFragmet listCall = new TwoLineFragmet();

    public List<Contact> contactList;

    public PhoneCallListener phoneCallListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        setupActionBar();

        phoneCallListener = new PhoneCallListener(this);


        this.listCall = TwoLineFragmet.getInstance();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, this.listCall)
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is pdirresent.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
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

    public static class TwoLineFragmet extends Fragment {

        List<HashMap<String, Object>> items;

        ListView mListView;

        SimpleAdapter adapter;

        public static TwoLineFragmet getInstance() {
            TwoLineFragmet twoLineFragmet = new TwoLineFragmet();
            return twoLineFragmet;
        }

        public TwoLineFragmet() {
        }

        public void setList(List<Contact> contactList) {
            this.items = new LinkedList<HashMap<String, Object>>();
            for (Contact contact : contactList) {
                items.add(contact.getListMap());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            ContactsService contactsService = new ContactsServiceImpl(getActivity());
            try {
                setList(contactsService.openContacts());
            } catch (StorageUnavailableException e) {
                e.getFinishDialog();
            } catch (FileException e) {
                e.getFinishDialog();
            } catch (MarshallingException e) {
                e.getFinishDialog();
            }

            mListView = (ListView) inflater.inflate(R.layout.fragment_call, container, false);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getItemAtPosition(position);

                    openItem((Contact) map.get("instance"));
                }
            });

            adapter = new SimpleAdapter(getActivity().getBaseContext(),
                    items,
                    R.layout.call_two_line_list_item ,
                    new String[] { "line1", "line2" },
                    new int[] {android.R.id.text1, android.R.id.text2});

            mListView.setAdapter(adapter);
            return mListView;
        }

        public void openItem(Contact contact) {
            // add PhoneStateListener
            CallActivity callActivity = (CallActivity) getActivity();
            PhoneCallListener pl = callActivity.phoneCallListener;
            TelephonyManager telephonyManager = (TelephonyManager) getActivity()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(pl,PhoneStateListener.LISTEN_CALL_STATE);

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(String.format("tel:%s", contact.getNumber())));
            startActivity(callIntent);
        }
    }

    public static class PhoneCallListener extends PhoneStateListener {

        private boolean isPhoneCalling = false;

        private Activity activity;

        String LOG_TAG = "LOGGING 123";

        public PhoneCallListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "OFFHOOK");

                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended,
                // need detect flag from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE");

                if (isPhoneCalling) {

                    Log.i(LOG_TAG, "restart app");

                    isPhoneCalling = false;
                    activity.finish();
                }

            }
        }
    }

}
