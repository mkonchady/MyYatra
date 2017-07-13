
package org.mkonchady.myyatra;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import org.mkonchady.myyatra.AfricaActivity;
import org.mkonchady.myyatra.PlaceActivity;

public class AfricaActivityTest extends  ActivityInstrumentationTestCase2<AfricaActivity> {
    private Solo solo = null;
    AfricaActivity placeActivity = null;
    private String TAG = "TestAfricaActivity";

    // calls startUp in CountryActivity
    public AfricaActivityTest() {
        super(AfricaActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(this.getInstrumentation(), this.getActivity());
        placeActivity = (AfricaActivity) solo.getCurrentActivity();
        placeActivity.setDISTANCE_IN_METERS(false);
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    // dump the distance matrix to a file
    public void testWriteDistance() throws Exception {
        placeActivity.writeDistances(placeActivity.allCities, "africa_dist_temp.txt");
    }

    // check if the city file was read correctly
    public void ttestCityCodes() throws Exception {
        String code = "LON";
        Integer index = placeActivity.cityIndex.get(code);
        PlaceActivity.City city = placeActivity.allCities.get(index);
        assertEquals(code, city.getCode());
    }


    // check if the distance computation between Libya and South Africa is correct
    public void ttestDistanceComp() throws Exception {
        // distance from Libya to South Africa
        float[] c = {26.55f, 18.04f, -30.03f, 21.33f}; // lat1 lon1 lat2 lon2
        float calcDist = placeActivity.getDistance(c[0], c[1], c[2], c[3]);
        assertEquals(6300.0f, calcDist, 60.0f);
    }

    // check if the distances between any pairs of cities is correct
    public void tttestCityDistance() throws Exception {
        String code1 = "LON"; String code2 = "INV";
        int index1 = placeActivity.cityIndex.get(code1);
        int index2 = placeActivity.cityIndex.get(code2);
        float dist = placeActivity.distances[index1][index2];
        assertEquals(1745.0f, dist, 40.0f);
    }

}