package org.mkonchady.myyatra;

import android.os.Bundle;

public class DCActivity extends PlaceActivity {

    //private final String TAG = "DCActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpperLeftLat(39.04f); setUpperLeftLon(-77.16f);
        setLowerRightLat(38.80f); setLowerRightLon(-76.84f);
        setMIN_NODE_SEPARATION(1000.0f);     // separation in meters
        setDISTANCE_IN_METERS(true);
        startUp("dc_places.xml", "dc_dist_matrix.txt", R.drawable.dc_map);
    }
}