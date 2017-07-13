package org.mkonchady.myyatra;

import android.os.Bundle;

public class UKActivity extends PlaceActivity {

    //private final String TAG = "UKActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(59.50f); setUpperLeftLon(-11.50f);
        setLowerRightLat(50.70f); setLowerRightLon(2.5f);
        setMIN_NODE_SEPARATION(60.0f);
        startUp("uk_cities.xml", "uk_dist_matrix.txt", R.drawable.britain_map);
    }
}