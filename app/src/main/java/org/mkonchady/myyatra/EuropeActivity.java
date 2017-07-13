package org.mkonchady.myyatra;

import android.os.Bundle;

public class EuropeActivity extends PlaceActivity {

    //private final String TAG = "EuropeActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(72.00f); setUpperLeftLon(-28.00f);
        setLowerRightLat(30.00f); setLowerRightLon(50.00f);
        setMIN_NODE_SEPARATION(350.0f);     // separation in kilometers
        startUp("europe_countries.xml", "europe_dist_matrix.txt", R.drawable.europe_map);
    }
}