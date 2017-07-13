package org.mkonchady.myyatra;

import android.os.Bundle;

public class FranceActivity extends PlaceActivity {

    //private final String TAG = "FranceActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(51.00f); setUpperLeftLon(-6.00f);
        setLowerRightLat(43.50f); setLowerRightLon(7.0f);
        setMIN_NODE_SEPARATION(75.0f);
        startUp("france_cities.xml", "france_dist_matrix.txt", R.drawable.france_map);
    }
}