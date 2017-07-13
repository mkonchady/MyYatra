package org.mkonchady.myyatra;

import android.os.Bundle;

public class KAActivity extends PlaceActivity {

    //private final String TAG = "KAActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(18.75f); setUpperLeftLon(74.00f);
        setLowerRightLat(11.50f); setLowerRightLon(78.75f);
        setMIN_NODE_SEPARATION(50.0f);     // separation in kilometers
        startUp("ka_cities.xml", "ka_dist_matrix.txt", R.drawable.ka_map);
    }
}