package org.mkonchady.myyatra;

import android.os.Bundle;

public class IndiaActivity extends PlaceActivity {

    //private final String TAG = "IndiaActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(38.76f); setUpperLeftLon(69.52f);
        setLowerRightLat(5.73f); setLowerRightLon(96.10f);
        setMIN_NODE_SEPARATION(125.0f);
        startUp("india_cities.xml", "india_dist_matrix.txt", R.drawable.india_map);
    }
}