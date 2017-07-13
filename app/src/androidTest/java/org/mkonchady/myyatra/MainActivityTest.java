package org.mkonchady.myyatra;

import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;

import org.mkonchady.myyatra.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo = null;
    MainActivity mainActvity = null;
    private String TAG = "MainActivity";

    public MainActivityTest() {
     super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(this.getInstrumentation(), getActivity());
        mainActvity = (MainActivity) solo.getCurrentActivity();

    }

    public void testMain() {
        assertEquals(true, true);

    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}