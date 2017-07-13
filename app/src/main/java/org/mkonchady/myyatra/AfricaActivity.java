package org.mkonchady.myyatra;

import android.os.Bundle;

public class AfricaActivity extends PlaceActivity {

    //private final String TAG = "AfricaActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(38.00f); setUpperLeftLon(-20.00f);
        setLowerRightLat(-35.00f); setLowerRightLon(60.00f);
        setMIN_NODE_SEPARATION(600.0f);     // separation in kilometers
        startUp("africa_countries.xml", "africa_dist_matrix.txt", R.drawable.africa_map);
    }
}