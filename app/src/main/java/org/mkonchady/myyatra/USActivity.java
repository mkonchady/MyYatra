package org.mkonchady.myyatra;

import android.os.Bundle;

public class USActivity extends PlaceActivity {
    //private final String TAG = "USActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(51.75f); setUpperLeftLon(-130.0f);
        setLowerRightLat(24.0f); setLowerRightLon(-85.5f);
        setMIN_NODE_SEPARATION(350.0f);
        startUp("us_cities.xml", "us_dist_matrix.txt", R.drawable.us_map);
    }
}