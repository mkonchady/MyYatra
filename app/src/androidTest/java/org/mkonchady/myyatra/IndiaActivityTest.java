package org.mkonchady.myyatra;

import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;

public class IndiaActivityTest extends  ActivityInstrumentationTestCase2<IndiaActivity> {
    private Solo solo = null;
    IndiaActivity indiaActivity = null;
    private String TAG = "TestIndiaActivity";

    // calls startUp in CountryActivity
    public IndiaActivityTest() {
        super(IndiaActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(this.getInstrumentation(), this.getActivity());
        indiaActivity = (IndiaActivity) solo.getCurrentActivity();
    }

    public void testR() {
        Constants.testRun(indiaActivity, 1000);
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    // dump the distance matrix to a file
    public void ttestWriteDistance() throws Exception {
        indiaActivity.writeDistances(indiaActivity.allCities, "india_dist_temp.txt");
    }

    // check if the city file was read correctly
    public void ttestCityCodes() throws Exception {
        String code = "BLR";
        Integer index = indiaActivity.cityIndex.get(code);
        PlaceActivity.City city = indiaActivity.allCities.get(index);
        assertEquals(code, city.getCode());
    }


    // check if the distance computation between DEL and BLR is correct
    public void ttestDistanceComp() throws Exception {
        // distance from DEL to BLR
        float[] c = {28.62f, 77.23f, 12.57f, 77.08f}; // lat1 lon1 lat2 lon2
        float calcDist = indiaActivity.getDistance(c[0], c[1], c[2], c[3]);
        assertEquals(1745.0f, calcDist, 40.0f);
    }

    // check if the distances between any pairs of cities is correct
    public void tttestCityDistance() throws Exception {
        String code1 = "BLR"; String code2 = "DEL";
        int index1 = indiaActivity.cityIndex.get(code1);
        int index2 = indiaActivity.cityIndex.get(code2);
        float dist = indiaActivity.distances[index1][index2];
        assertEquals(1745.0f, dist, 40.0f);
    }


}