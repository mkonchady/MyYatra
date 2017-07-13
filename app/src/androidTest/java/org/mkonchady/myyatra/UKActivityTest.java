package org.mkonchady.myyatra;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import org.mkonchady.myyatra.PlaceActivity;
import org.mkonchady.myyatra.UKActivity;

public class UKActivityTest extends  ActivityInstrumentationTestCase2<UKActivity> {
    private Solo solo = null;
    UKActivity placeActivity = null;
    private String TAG = "TestUKActivity";

    // calls startUp in CountryActivity
    public UKActivityTest() {
        super(UKActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(this.getInstrumentation(), this.getActivity());
        placeActivity = (UKActivity) solo.getCurrentActivity();
    }

    public void testR() {
        Constants.testRun(placeActivity, 1000);
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    // dump the distance matrix to a file
    public void ttestWriteDistance() throws Exception {
        placeActivity.writeDistances(placeActivity.allCities, "uk_dist_temp.txt");
    }

    // check if the city file was read correctly
    public void ttestCityCodes() throws Exception {
        String code = "LON";
        Integer index = placeActivity.cityIndex.get(code);
        PlaceActivity.City city = placeActivity.allCities.get(index);
        assertEquals(code, city.getCode());
    }


    // check if the distance computation between DC and LAX is correct
    public void ttestDistanceComp() throws Exception {
        // distance from DC to LAX
        float[] c = {38.55f, -77.04f, 34.03f, -118.90f}; // lat1 lon1 lat2 lon2
        float calcDist = placeActivity.getDistance(c[0], c[1], c[2], c[3]);
        assertEquals(3700.0f, calcDist, 60.0f);
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