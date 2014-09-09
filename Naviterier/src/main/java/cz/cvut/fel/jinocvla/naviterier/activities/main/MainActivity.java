package cz.cvut.fel.jinocvla.naviterier.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.indoor.IndoorMapSelection;
import cz.cvut.fel.jinocvla.naviterier.activities.outdoor.OutdoorMapSelection;
import cz.cvut.fel.jinocvla.naviterier.activities.settings.SettingsActivity;
import cz.cvut.fel.jinocvla.naviterier.services.SchemeStorageService;
import cz.cvut.fel.jinocvla.naviterier.services.impl.SchemeStorageServiceImpl;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button outdoorButton = (Button)findViewById(R.id.outdoor_navigation);
        outdoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OutdoorMapSelection.class);
                startActivity(intent);
            }
        });

        final Button indoorButton = (Button)findViewById(R.id.indoor_navigation);
        indoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, IndoorMapSelection.class);
                startActivity(intent);
            }
        });

        final Button continueButton = (Button)findViewById(R.id.last_navigation);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                SchemeStorageService schemeStorageService = SchemeStorageServiceImpl.getInstance(MainActivity.this);
                if (schemeStorageService.isSchemeStored(SchemeActivity.State.COMPLETE)) {
                    Intent intent;

                    intent = new Intent(MainActivity.this, schemeStorageService.navClass(SchemeActivity.State.COMPLETE));

                    startActivity(intent);
                }

            }
        });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
