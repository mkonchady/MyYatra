package org.mkonchady.myyatra;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Constants {

    final static String TAG = "Constants";

    private Constants() {
        throw new AssertionError();
    }

    public static void testRun(PlaceActivity placeActivity, final int NUM_TRIALS) {

        for (int i = 0; i < NUM_TRIALS; i++) {

            ArrayList<PlaceActivity.City> cities = placeActivity.testRun();
            ArrayList<String> seen = new ArrayList<>();

            // check for duplicate cities
            for (int j = 0; j < cities.size() - 1; j++) {
                PlaceActivity.City city = cities.get(j);
                if (seen.contains(city.toString()))
                    Log.e(TAG, "Duplicate city" + dumpRoute(cities));
                else {
                    seen.add(city.toString());
                }
            }

            // check the start and end cities are the same
            String firstCity = cities.get(0).toString();
            String lastCity = cities.get(cities.size()-1).toString();
            if (!firstCity.equals(lastCity)) {
                Log.e(TAG, "Last and first do not match: " + dumpRoute(cities));
            }

            // check that the route does not intersect
            // build all the pairs of cities
            HashMap<PlaceActivity.City, PlaceActivity.City> cityPairs = new HashMap<>();
            for (int j = 1; j < cities.size()-1; j++) {
                PlaceActivity.City city1 = cities.get(j-1);
                PlaceActivity.City city2 = cities.get(j);
                cityPairs.put(city1, city2);
            }
            // check against all pairs
            for (int j = 1; j < cities.size(); j++) {
                PlaceActivity.City city1 = cities.get(j-1);
                PlaceActivity.City city2 = cities.get(j);

                Iterator it = cityPairs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    PlaceActivity.City city3 = (PlaceActivity.City) pair.getKey();
                    PlaceActivity.City city4 = (PlaceActivity.City) pair.getValue();
                    it.remove(); // avoids a ConcurrentModificationException
                    if (uniqueCities(city1, city2, city3, city4) && intersect(city1, city2, city3, city4))
                        Log.e(TAG, "Intersection at " + city1 + " " + city2 + " " + city3 + " " + city4);
                }
            }

            Log.d(TAG, "Finished " + i + " trials" + " " + dumpRoute(cities));
        }
    }


    public static boolean uniqueCities(PlaceActivity.City city1, PlaceActivity.City city2,
                                PlaceActivity.City city3, PlaceActivity.City city4) {
        if (city1.toString().equals(city2.toString())) return false;
        if (city1.toString().equals(city3.toString())) return false;
        if (city1.toString().equals(city4.toString())) return false;
        if (city2.toString().equals(city3.toString())) return false;
        if (city2.toString().equals(city4.toString())) return false;
        if (city3.toString().equals(city4.toString())) return false;
        return true;
    }

    // check if two lines intersect
    public static boolean intersect(PlaceActivity.City city1, PlaceActivity.City city2,
                             PlaceActivity.City city3, PlaceActivity.City city4) {
        double x1 = (double) city1.getLon(); double y1 = (double) city1.getLat();
        double x2 = (double) city2.getLon(); double y2 = (double) city2.getLat();
        double x3 = (double) city3.getLon(); double y3 = (double) city3.getLat();
        double x4 = (double) city4.getLon(); double y4 = (double) city4.getLat();
        return linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
    }


    /**
     * Copied from Java API code
     *
     * Tells whether the two line segments cross.
     *
     * @param x1
     *           the x coordinate of the starting point of the first segment.
     * @param y1
     *           the y coordinate of the starting point of the first segment.
     * @param x2
     *            the x coordinate of the end point of the first segment.
     * @param y2
     *            the y coordinate of the end point of the first segment.
     * @param x3
     *            the x coordinate of the starting point of the second segment.
     * @param y3
     *            the y coordinate of the starting point of the second segment.
     * @param x4
     *            the x coordinate of the end point of the second segment.
     * @param y4
     *            the y coordinate of the end point of the second segment.
     *
     * @return true, if the two line segments cross.
     */
    public static  boolean linesIntersect(double x1, double y1, double x2, double y2, double x3,
                                  double y3, double x4, double y4) {
        /*
         * A = (x2-x1, y2-y1) B = (x3-x1, y3-y1) C = (x4-x1, y4-y1) D = (x4-x3,
         * y4-y3) = C-B E = (x1-x3, y1-y3) = -B F = (x2-x3, y2-y3) = A-B Result
         * is ((AxB) (AxC) <=0) and ((DxE) (DxF) <= 0) DxE = (C-B)x(-B) =
         * BxB-CxB = BxC DxF = (C-B)x(A-B) = CxA-CxB-BxA+BxB = AxB+BxC-AxC
         */
        x2 -= x1; // A
        y2 -= y1;
        x3 -= x1; // B
        y3 -= y1;
        x4 -= x1; // C
        y4 -= y1;
        double AvB = x2 * y3 - x3 * y2;
        double AvC = x2 * y4 - x4 * y2;
        // Online
        if (AvB == 0.0 && AvC == 0.0) {
            if (x2 != 0.0) {
                return (x4 * x3 <= 0.0)
                        || ((x3 * x2 >= 0.0) && (x2 > 0.0 ? x3 <= x2 || x4 <= x2 : x3 >= x2
                        || x4 >= x2));
            }
            if (y2 != 0.0) {
                return (y4 * y3 <= 0.0)
                        || ((y3 * y2 >= 0.0) && (y2 > 0.0 ? y3 <= y2 || y4 <= y2 : y3 >= y2
                        || y4 >= y2));
            }
            return false;
        }
        double BvC = x3 * y4 - x4 * y3;
        return (AvB * AvC <= 0.0) && (BvC * (AvB + BvC - AvC) <= 0.0);
    }


    public static  String dumpRoute(ArrayList<PlaceActivity.City> cities) {
        StringBuilder out = new StringBuilder();
        for (int j = 0; j < cities.size(); j++)
            out.append(cities.get(j).toString() + " ");
        return out.toString();
    }
}
