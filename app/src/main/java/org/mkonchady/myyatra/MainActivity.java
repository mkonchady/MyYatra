package org.mkonchady.myyatra;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class MainActivity extends Activity {

    final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main_activity);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_about:
                intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onResume() {

        super.onResume();

        // Handle the India button
        ImageButton ib = (ImageButton) findViewById(R.id.indiaButton);
        View.OnClickListener ibLis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IndiaActivity.class);
                startActivity(intent);
            }
        };
        assert ib != null;
        ib.setOnClickListener(ibLis);

        // Handle the US button
        ImageButton ub = (ImageButton) findViewById(R.id.usButton);
        View.OnClickListener ubLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, USActivity.class);
                startActivity(intent);
            }
        };
        assert ub != null;
        ub.setOnClickListener(ubLis);

        // Handle the UK button
        ImageButton bb = (ImageButton) findViewById(R.id.ukButton);
        View.OnClickListener bbLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UKActivity.class);
                startActivity(intent);
            }
        };
        assert bb != null;
        bb.setOnClickListener(bbLis);

        // Handle the France button
        ImageButton fb = (ImageButton) findViewById(R.id.franceButton);
        View.OnClickListener fbLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FranceActivity.class);
                startActivity(intent);
            }
        };
        assert fb != null;
        fb.setOnClickListener(fbLis);

        // Handle the DC button
        ImageButton db = (ImageButton) findViewById(R.id.dcButton);
        View.OnClickListener dbLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DCActivity.class);
                startActivity(intent);
            }
        };
        assert db != null;
        db.setOnClickListener(dbLis);

        // Handle the KA button
        ImageButton kb = (ImageButton) findViewById(R.id.kaButton);
        View.OnClickListener kbLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, KAActivity.class);
                startActivity(intent);
            }
        };
        assert kb != null;
        kb.setOnClickListener(kbLis);

        // Handle the Africa button
        ImageButton ab = (ImageButton) findViewById(R.id.africaButton);
        View.OnClickListener abLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AfricaActivity.class);
                startActivity(intent);
            }
        };
        assert ab != null;
        ab.setOnClickListener(abLis);

        // Handle the Europe button
        ImageButton eb = (ImageButton) findViewById(R.id.europeButton);
        View.OnClickListener ebLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EuropeActivity.class);
                startActivity(intent);
            }
        };
        assert eb != null;
        eb.setOnClickListener(ebLis);

        // Handle the Asia button
        ImageButton sb = (ImageButton) findViewById(R.id.asiaButton);
        View.OnClickListener sbLis = new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AsiaActivity.class);
                startActivity(intent);
            }
        };
        assert sb != null;
        sb.setOnClickListener(sbLis);

    }
}
