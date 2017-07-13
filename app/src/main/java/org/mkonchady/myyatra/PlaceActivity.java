package org.mkonchady.myyatra;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common class for all maps
 */
public abstract class PlaceActivity extends Activity {

    private int NUMBER_OF_CITIES = 0;                       // No. of allCities in all
    public  int DIM;                                        // No. of allCities in game
    public  float MIN_NODE_SEPARATION;                      // Min. separation between allCities
    public  boolean DISTANCE_IN_METERS;                     // distance in meters
    public  int computer_time;                              // the time to compute a solution
    boolean vibrate_city;                                   // vibrate when a city is selected
    boolean offline = false;                                // run offline

    // Tables for the larger collection
    private int[] indarr;                                   // index to larger array
    public  float[][] all_distances = null;                 // Matrix of all distances
    public  String citiesFile = "";                         // File of all cities info
    public  String distanceFile = "";                       // File of all city distances
    public  ArrayList<City> allCities = new ArrayList<>();  // List of all cities with codes..
    public Map<String, Integer> cityIndex =                 // Hash of city name to integer
            new HashMap<>();

    // Tables for the smaller collection
    public float[][] distances;                             // matrix of distances
    public ArrayList<City> cities = new ArrayList<>();      // list of DIM random cities
    public ArrayList<City> compCities = new ArrayList<>();  // computer route cities
    public float bestDistance;                              // computer trip distance

    // latitude longitude for origin and lower right corner
    private float[]upperLeft = {0,0f, 0,0f};
    private float[]lowerRight = {0,0f, 0,0f};

    public float dlon = 0.0f;       // longitude per pixel
    public float dlat = 0.0f;       // latitude per pixel
    private Pattern re = null;      // RE pattern to extract lat/lon

    // GA constants
    private Thread thread = null;
    public GAManager gaManager;
    public int GA_STATUS = 0;
    public final int GA_START = 0;
    public final int GA_RUNNING = 1;
    public final int GA_FINISHED = 2;

    public YatraView view = null;
    public TextView statusView = null;
    final String TAG = "PlaceActivity";

    public void startUp(String citiesFile, String distanceFile, int mapName) {

        this.citiesFile = citiesFile;
        this.distanceFile = distanceFile;

        // build the bitmap from the map name and scale to the screen dimensions
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        // convert screen pixels to density independent pixels
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        // extract the scaled down version of the bitmap from mapName
        Bitmap bitmap = decodeSampledBitmapFromResource(getResources(), mapName, (int) (dpWidth), (int) (dpHeight) );
        float bitmapHeight = bitmap.getHeight();
        float bitmapWidth = bitmap.getWidth();
        float screenHeight = displayMetrics.heightPixels;
        float screenWidth = displayMetrics.widthPixels;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            screenHeight  = swap(screenWidth, screenWidth=screenHeight);
        }

        float scaleWidth = screenWidth / bitmapWidth;
        float scaleHeight = screenHeight / bitmapHeight;
        float scale = (scaleHeight >= scaleWidth) ? scaleWidth : scaleHeight;

        // scale the bitmap based on the screen dimensions
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, 0, 0);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) bitmapWidth, (int) bitmapHeight, matrix, false);

        dlat = getLatRange() / bitmap.getHeight();
        dlon = getLonRange() / bitmap.getWidth();

        // get the number of cities (DIM)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        DIM = Integer.parseInt(sharedPreferences.getString("DIM", "16"));
        indarr = new int[DIM];
        distances = new float[DIM][DIM];
        computer_time = Integer.parseInt(sharedPreferences.getString("Computer_Time", "2"));
        vibrate_city = Boolean.parseBoolean(sharedPreferences.getString("vibrate_city", "false"));

        // Create a Pattern object to extract lats and lons
        String pattern = "(\\d+\\.\\d+)(.)$";
        re = Pattern.compile(pattern);

        readCities(citiesFile);      // get the list of allCities, codes, lat, lon
        readDistances(distanceFile); // get the pre-computed pair wise distances

        // create the view and pass the bitmap
        if (!offline) {
            setContentView(R.layout.place_activity);
            view = (YatraView) this.findViewById(R.id.yatraView);
            view.setBitmap(bitmap);
            statusView = (TextView) this.findViewById(R.id.statusView1);

            // start a thread to run the GA, read files, etc.
            thread = new Thread(backGround, "Background");
            GA_STATUS = GA_START;
            thread.start();
        }

    }

    // start a thread to do the background processing
    private Runnable backGround = new Runnable() {
        @Override
        public void run() {
            backGroundProcessing();
        }
    };

    // handle the long running tasks in this thread
    public void backGroundProcessing() {

        // infinite loop till interruption
        while (true) {

            if (Thread.interrupted()) return;

            if (GA_STATUS == GA_START) {
                createCitySet();
                GA_STATUS = GA_RUNNING;
            }

            // run the GA
            if (GA_STATUS == GA_RUNNING) {
               gaManager.run_ga();
               for (int i = 0; i < DIM+1; i++)
                    compCities.add(cities.get(gaManager.bestRoute[i]));
               bestDistance = gaManager.bestDistance;
               GA_STATUS = GA_FINISHED;
            }

            if (GA_STATUS == GA_FINISHED) return;
        }
    }


    // assign a set of random cities
    public void createCitySet() {
        // pick DIM unique numbers from 0 to NUMBER_OF_CITIES
        int picked;
        for (int i = 0; i < DIM; i++) {
            do
                picked = (int) (Math.random() * NUMBER_OF_CITIES);
            while (!(unique(i, picked)));
            indarr[i] = picked;
        }

        // build the x, y, cities, and c arrays
        cities = new ArrayList<>();
        for (int i = 0; i < DIM; i++) {
            String code = allCities.get(indarr[i]).getCode();
            float x = allCities.get(indarr[i]).getLon();
            float y = allCities.get(indarr[i]).getLat();
            hemisphere h = allCities.get(indarr[i]).getLatLoc();
            half f = allCities.get(indarr[i]).getLonLoc();
            String cityName = allCities.get(indarr[i]).getName();
            cities.add(new City(cityName, code, x, y, h, f));
        }
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++)
                distances[i][j] = all_distances[indarr[i]][indarr[j]];
        gaManager = new GAManager(DIM, distances, computer_time);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopThread();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopThread();
    }

    //   @Override
    //   public void onPause() {
    //       super.onPause();
    //       stopThread();
    //   }

    private void stopThread() {
        // end the background thread, if any
        if ( (thread != null) && thread.isAlive() ){
            thread.interrupt();
            finish();
        }
        //gaManager = null;
        //bitmap.recycle();
    }

    // check if selected is unique and far enough from other allCities
    public boolean unique( int dim, int picked) {
        boolean flag = true;
        for (int i = 0; i < dim; i++) {
            if ( (indarr[i] == picked) ||  (all_distances[indarr[i]][picked] < MIN_NODE_SEPARATION) )
                flag = false;
        }
        return flag;
    }

    // Read the XML file containing the list of all cities
    public void readCities(String citiesFile) {

        Matcher m;
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            InputStream in_s = getApplicationContext().getAssets().open(citiesFile);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            City currentCity = null;

            // Get the city name, code, lon, and lat values for each city
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                String name;
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("Row")) {
                            String city = parser.getAttributeValue(null,"city");
                            String code = parser.getAttributeValue(null,"code");

                            // extract the latitude
                            String latString = parser.getAttributeValue(null, "lat");
                            m = re.matcher(latString);
                            float lat = (m.find())? Float.parseFloat(m.group(1)): 0.0f;
                            hemisphere h = (m.group(2).equalsIgnoreCase("N"))?
                                    hemisphere.N: hemisphere.S;

                            // extract the longitude
                            String lonString = parser.getAttributeValue(null, "lon");
                            m = re.matcher(lonString);
                            float lon = (m.find())? Float.parseFloat(m.group(1)): 0.0f;
                            half f = m.group(2).equalsIgnoreCase("E")?
                                    half.E: half.W;
                            currentCity = new City(city, code, lon, lat, h, f);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("Row") && currentCity != null){
                            allCities.add(currentCity);
                            cityIndex.put(currentCity.getCode(),
                                    //new Integer(allCities.size() - 1));
                                    allCities.size() - 1);
                        }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException xe) {
            Log.e(TAG, "Could not parse XML: " + xe.getMessage());
        } catch (IOException ie) {
            Log.e(TAG, "Could not read XML File: " + ie.getMessage());
        }
        NUMBER_OF_CITIES = allCities.size();

    }

    // read the pair wise distances into a matrix from an asset file
    public void readDistances(String distanceFile) {
        int size = allCities.size();
        all_distances = new float[size][size];
        try {
            InputStream in = getApplicationContext().getAssets().open(distanceFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            int x = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                int y = 0;
                for (String str : values) {
                    all_distances[x][y++] = Integer.parseInt(str);
                }
                x++;
            }
        } catch (IOException ie) {
            Log.e("TAG", "Could not read distance file: " + ie.getMessage());
        }

    }

    /* used to compute distances apriori for the distances file
       To locate the output file on Linux:
          cd ~/.android/avd/<avd name>.avd/sdcard.img
          sudo mount sdcard.img -o loop /mnt/sdcard
          cd /mnt/sdcard/Android/data/org.example.mkonchady.myyatra/files
     */
    public void writeDistances(ArrayList<City> cities, String FILENAME) {
        try {
            // dump the distance matrix to a SDCARD file
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(getExternalFilesDir(null), FILENAME);
                if(!file.exists()) {
                    if (!file.createNewFile()) {
                        throw new IOException();
                    }

                }
                FileOutputStream fos = new FileOutputStream(file, false);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < cities.size(); i++) {
                    for (int j = 0; j < cities.size(); j++) {
                        // write the pair wise distances, one line per city
                        float lat1 = cities.get(i).getLat();
                        lat1 = (cities.get(i).getLatLoc().equals(hemisphere.S))? -lat1: lat1;
                        float lat2 = cities.get(j).getLat();
                        lat2 = (cities.get(j).getLatLoc().equals(hemisphere.S))? -lat2: lat2;

                        float lon1 = cities.get(i).getLon();
                        lon1 = (cities.get(i).getLonLoc().equals(half.W) )? -lon1: lon1;
                        float lon2 = cities.get(j).getLon();
                        lon2 = (cities.get(j).getLonLoc().equals(half.W) )? -lon2: lon2;
                        float x = getDistance(lat1, lon1, lat2, lon2);
                        x = (x < 1.0) ? 0.0f : x;
                        x = (Double.isNaN(x)) ? 0.0f : x;
                        x = (isDISTANCE_IN_METERS())? (x * 1000): x;
                        //String out = String.format("%d",(long)x, Locale.getDefault());
                        String out = String.format(Locale.getDefault(), "%d", (long)x);
                        sb.append(out);
                        if (j < (cities.size() - 1)) sb.append(" ");
                    }
                    sb.append("\n");
                    fos.write(sb.toString().getBytes());
                    sb = new StringBuffer();
                }
                fos.close();
            }

        } catch (IOException ie) {
            Log.e("TAG", "Could not open output file for distances.");
        }
    }

    // calculate the great circle distance between any locations
    public float getDistance(float lat1, float lon1, float lat2, float lon2) {
        double x1 = Math.toRadians(lat1);
        double y1 = Math.toRadians(lon1);
        double x2 = Math.toRadians(lat2);
        double y2 = Math.toRadians(lon2);

        //Compute using the spherical law of cosines
        //double angle = Math.acos(Math.sin(x1) * Math.sin(x2)
        //                        + Math.cos(x1) * Math.cos(x2) * Math.cos(y1 - y2) );
        //angle = Math.toDegrees(angle);
        //double distance = 60 * angle;

        // Haverside formula
        double a = Math.pow( Math.sin( (x2 - x1) / 2 ), 2 )
                 + Math.cos(x1) * Math.cos(x2) * Math.pow( Math.sin( (y2 - y1) / 2 ), 2);

        // great circle distance in radians
        double angle = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
        angle = Math.toDegrees(angle);

        // distance = r * theta where theta is the angle between the 2 locations
        // and r is 60 nautical miles
        double distance = 60 * angle;

        // convert distance from nautical miles to kms.
        return (float) (distance * 1.852);
    }

    // from developer.android.com
    // return a sampled version of the bitmap to save memory
    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // for offline testing
    public ArrayList<City> testRun() {
        compCities = new ArrayList<>();
        createCitySet();
        gaManager.run_ga();
        for (int i = 0; i < DIM+1; i++)
            compCities.add(cities.get(gaManager.bestRoute[i]));
        return compCities;
    }

    public float swap(float a, float b) {
        return a;
    }

    public boolean isDISTANCE_IN_METERS() {
        return DISTANCE_IN_METERS;
    }
    public void setDISTANCE_IN_METERS(boolean DISTANCE_IN_METERS) {
        this.DISTANCE_IN_METERS = DISTANCE_IN_METERS;
    }
    public float getLonRange() {
        return Math.abs(upperLeft[1] - lowerRight[1]);
    }
    public float getLatRange() {
        return Math.abs(upperLeft[0] - lowerRight[0]);
    }
    public float getDlon() {
        return dlon;
    }
    public float getDlat() {
        return dlat;
    }
    public float getUpperLeftLon() {
        return upperLeft[1];
    }
    public float getUpperLeftLat() {
        return upperLeft[0];
    }
    public void setUpperLeftLon(float x) {
        upperLeft[1] = x;
    }
    public void setUpperLeftLat(float y) {
        upperLeft[0] = y;
    }
    public void setLowerRightLon(float x) {
        lowerRight[1] = x;
    }
    public void setLowerRightLat(float y) {
        lowerRight[0] = y;
    }
    public void setMIN_NODE_SEPARATION(float MIN_NODE_SEPARATION) {
        this.MIN_NODE_SEPARATION = MIN_NODE_SEPARATION;
    }

    public enum hemisphere  {N, S}
    public enum half {E, W}

    // Class for city information
    public class City {

        private String name;
        private String code;
        private float lon;
        private float lat;
        private hemisphere latLoc;
        private half lonLoc;

        City(String name, String code, float lon, float lat, hemisphere latLoc, half lonLoc) {
            this.name = name;
            this.code = code;
            this.lon = lon;
            this.lat = lat;
            this.latLoc = latLoc;
            this.lonLoc = lonLoc;
        }

        public String getName() { return name; }

        public String getCode() {
            return code;
        }

        public float getLon() {
            return lon;
        }

        public float getLat() {
            return lat;
        }

        public hemisphere getLatLoc() {
            return latLoc;
        }

        public half getLonLoc() {
            return lonLoc;
        }

        public String toString() {
            return (code);
        }

    }
}