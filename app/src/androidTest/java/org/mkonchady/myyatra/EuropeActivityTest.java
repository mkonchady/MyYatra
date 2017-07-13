
package org.mkonchady.myyatra;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.robotium.solo.Solo;

import org.mkonchady.myyatra.EuropeActivity;
import org.mkonchady.myyatra.PlaceActivity;

import java.util.ArrayList;

public class EuropeActivityTest extends  ActivityInstrumentationTestCase2<EuropeActivity> {
    private Solo solo = null;
    EuropeActivity placeActivity = null;
    private String TAG = "TestEuropeActivity";

    // calls startUp in CountryActivity
    public EuropeActivityTest() {
        super(EuropeActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(this.getInstrumentation(), this.getActivity());
        placeActivity = (EuropeActivity) solo.getCurrentActivity();
        placeActivity.setDISTANCE_IN_METERS(false);
    }

    public void testR() {
        Constants.testRun(placeActivity, 1000);
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    // dump the distance matrix to a file
    public void ttestWriteDistance() throws Exception {
        placeActivity.writeDistances(placeActivity.allCities, "europe_dist_temp.txt");
    }

    // check if the city file was read correctly
    public void ttestCityCodes() throws Exception {
        String code = "LON";
        Integer index = placeActivity.cityIndex.get(code);
        PlaceActivity.City city = placeActivity.allCities.get(index);
        assertEquals(code, city.getCode());
    }


    // check if the distance computation in Europe is correct
    public void ttestDistanceComp() throws Exception {
        // distance
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