package org.mkonchady.myyatra;

import android.os.Bundle;

public class AsiaActivity extends PlaceActivity {

    //private final String TAG = "AsiaActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(55.00f); setUpperLeftLon(30.00f);
        setLowerRightLat(-10.00f); setLowerRightLon(150.00f);
        setMIN_NODE_SEPARATION(800.0f);     // separation in kilometers
        startUp("asia_countries.xml", "asia_dist_matrix.txt", R.drawable.asia_map);
    }

}