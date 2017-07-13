package org.mkonchady.myyatra;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
//import android.support.v7.appcompat.*;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class YatraView extends View  {

    private int DIM = 0;                                // no. of cities in the problem
    private static final int INVALID_POINTER_ID = -1;

    public Bitmap bitmap;                               // bitmap to hold the map
    public Bitmap workingBitmap;                        // bitmap to draw and display
    private int widthBitmap;
    private int heightBitmap;
    int widthCanvas;
    int heightCanvas;

   // private final int CITY_FONT = 40;                   // city font pixel 40
    private final int CITY_FONT = 12;
    private final int CITY_OVAL = 15;                   // city oval pixel
    private final float STROKE_WIDTH = 4.0f;
    private float ovalSize = 0f;

    private Matrix canvasForwardTransform = new Matrix();     // transform from map xy to canvas xy
    private Matrix canvasReverseTransform = new Matrix();     // transform from canvas xy to map xy

    // scale and position of canvas on map to handle drag
    private float mxScaleFactor = 1.0f;
    private float myScaleFactor = 1.0f;
    private float mPosX;       private float mPosY;

    private float mLastTouchX; private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;
    private ScaleGestureDetector mScaleDetector;
    private Vibrator vibrator = null;
    private int ROUTE_DISPLAY_TIME = 1500;              // show the user and comp. routes for 1.5 seconds

    // paints for text and ovals
    private Paint cityTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint compOvalPaint = new Paint();
    private Paint userOvalPaint = new Paint();
    private Paint compRoutePaint = new Paint();
    private Paint userRoutePaint = new Paint();
    private Paint backgroundPaint = new Paint();

    private int[] userRoute;
    private Canvas canvas1 = null;

    private PlaceActivity placeActivity;
    private Context context;
    private long prevTime, currTime;
    private long startTime = System.currentTimeMillis();

    // GA_STATUS indicators
    private int MAP_STATUS = 0;
    //private final int MAP_START = 0;
    private final int MAP_RUNNING = 1;
    private final int MAP_FINISHED = 2;

    private int autoCount = 0;                  // route node counter for auto run
    private int userCount = 0;                  // route node counter for player run
    private float userDistance = 0.0f;
    private boolean userClicked = false;
    private boolean shownDialog = false;
    private boolean showCompRoute = false;
    //private final String TAG = "YatraView";

    public YatraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        placeActivity = (PlaceActivity) context;
        DIM = placeActivity.DIM;
        prevTime = currTime = System.currentTimeMillis();

        // initialize the user's route
        userRoute = new int[DIM+1];
        for (int i = 0; i < DIM; i++) userRoute[i] = -1;

        cityTextPaint.setColor(ContextCompat.getColor(context, R.color.cityText));
        cityTextPaint.setFakeBoldText(true);
        cityTextPaint.setSubpixelText(true);
        cityTextPaint.setTextAlign(Paint.Align.CENTER);
        //setTextSize(cityTextPaint, CITY_FONT, "AAA");
        float scale = context.getResources().getDisplayMetrics().density;
        cityTextPaint.setTextSize((int) (CITY_FONT * scale));

        compOvalPaint.setColor(ContextCompat.getColor(context, R.color.comproute));
        compOvalPaint.setStyle(Paint.Style.FILL);
        setOvalSizeForWidth(compOvalPaint, CITY_OVAL);
        compRoutePaint.setColor(ContextCompat.getColor(context, R.color.comproute));
        compRoutePaint.setStrokeWidth(STROKE_WIDTH);

        userOvalPaint.setColor(ContextCompat.getColor(context, R.color.userroute));
        userOvalPaint.setStyle(Paint.Style.FILL);
        setOvalSizeForWidth(userOvalPaint, CITY_OVAL);
        userRoutePaint.setColor(ContextCompat.getColor(context, R.color.userroute));
        userRoutePaint.setStrokeWidth(STROKE_WIDTH);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.BLACK);

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        placeActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    // called from PlaceActivity, create a working bitmap to modify
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        widthBitmap = bitmap.getWidth();
        heightBitmap = bitmap.getHeight();
        workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas1 = new Canvas(workingBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //float save_mPosX;  float save_mPosY;
        super.onDraw(canvas);

        // initialize the working bitmap
        canvas1.drawRect(0.0f, 0.0f, (float) workingBitmap.getWidth(), (float) workingBitmap.getHeight(), backgroundPaint);

        // initial position of map
        if (MAP_STATUS < MAP_RUNNING) {
            widthCanvas = getWidth();         // width of canvas
            heightCanvas = getHeight();       // height of canvas
            if (widthCanvas < heightCanvas) {
                mxScaleFactor = (float) widthCanvas / widthBitmap;
                myScaleFactor = mxScaleFactor;
            } else {
                myScaleFactor = (float) heightCanvas / heightBitmap;
                mxScaleFactor = myScaleFactor;
            }
            mPosX = widthBitmap / 2 ; mPosY = heightBitmap / 2 ;
            //save_mPosX = mPosX; save_mPosY = mPosY;
            MAP_STATUS = MAP_RUNNING;
        }

        // draw on canvas1 to create the working bitmap, create the  blank map
        canvas1.drawBitmap(bitmap, 0.0f, 0.0f, backgroundPaint);

        // draw cities and codes on the working bitmap
        if (placeActivity.GA_STATUS >= placeActivity.GA_RUNNING) {
            drawCities(canvas1, placeActivity.cities);
        } else {
            pause(100); invalidate();
            return;
        }

        // check the user and computer status
        boolean user_finished = (MAP_STATUS == MAP_FINISHED);
        boolean comp_finished = (placeActivity.GA_STATUS == placeActivity.GA_FINISHED);

        // if user is not done, check for auto run and show the partial route
        if (!user_finished) {
            autoRunCheck(comp_finished);
            drawUserRoute(canvas1);
        } else {
            if (comp_finished) {    // user and computer are done
                if (!shownDialog) { // show the results window once
                    shownDialog = true;
                    drawUserRoute(canvas1);
                    float compDistance = placeActivity.bestDistance;
                    if (placeActivity.isDISTANCE_IN_METERS()) {
                        compDistance = compDistance / 1000;
                        userDistance = userDistance / 1000;
                    }

                    Intent intent = new Intent(placeActivity, ResultInfoActivity.class);
                    intent.putExtra("yours", userDistance);
                    intent.putExtra("computer", compDistance);
                    placeActivity.startActivity(intent);
                    placeActivity.statusView.setText(placeActivity.getText(R.string.tsp_restart));
                } else { // alternately show user and computer routes
                    currTime = System.currentTimeMillis();
                    if ((currTime - prevTime) > ROUTE_DISPLAY_TIME) {
                        prevTime = currTime;
                        showCompRoute = !showCompRoute;
                    }

                    if (showCompRoute) drawCompRoute(canvas1);
                    else drawUserRoute(canvas1);
                    placeActivity.statusView.setText(placeActivity.getText(R.string.tsp_restart));
                }
            } else {    // user is done, but comp is working
                String out = placeActivity.getText(R.string.tsp_progress) + " "  +
                        getTimeDurationHHMMSS(System.currentTimeMillis() - startTime);
                placeActivity.statusView.setText(out);
                drawUserRoute(canvas1);
            }
        }

        // build the forward / reverse transforms
        forwardTransform(); reverseTransform();
        canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), backgroundPaint);
        canvas.drawBitmap(workingBitmap, canvasForwardTransform, backgroundPaint);
        invalidate();

    }

    // transform from bitmap to canvas
    private void forwardTransform() {
        canvasForwardTransform.reset();
        canvasForwardTransform.postTranslate(-widthBitmap / 2.0f, -heightBitmap / 2.0f);
        canvasForwardTransform.postScale(mxScaleFactor, myScaleFactor);
        canvasForwardTransform.postTranslate(mPosX, mPosY);
    }

    // transform from canvas to bitmap
    private void reverseTransform() {
        canvasReverseTransform.reset();
        canvasReverseTransform.postTranslate(-mPosX, -mPosY);
        canvasReverseTransform.postScale(1.0f / mxScaleFactor, 1.0f / myScaleFactor);
        canvasReverseTransform.postTranslate(widthBitmap / 2.0f, heightBitmap / 2.0f);
    }

    // draw the cities and code
    private void drawCities(Canvas canvas, ArrayList<PlaceActivity.City> cities) {
        for (PlaceActivity.City city : cities) {
            float[] xy = getCanvasXY(city);
            String code = city.getCode();
            canvas.drawText(code, xy[0], xy[1], cityTextPaint);
            RectF rectF = new RectF(xy[0], xy[1], xy[0] + ovalSize, xy[1] + ovalSize);
            canvas.drawOval(rectF, compOvalPaint);
        }
    }

    // draw the computer route
    private void drawCompRoute(Canvas canvas) {
        for (int i = 0; i < DIM; i++) {
            PlaceActivity.City city1 = placeActivity.compCities.get(i);
            float[] xy1 = getCanvasXY(city1);
            RectF rectF = new RectF(xy1[0], xy1[1], xy1[0] + ovalSize, xy1[1] + ovalSize);
            canvas.drawOval(rectF, compOvalPaint);
            PlaceActivity.City city2 = placeActivity.compCities.get(i + 1);
            float[] xy2 = getCanvasXY(city2);
            rectF = new RectF(xy2[0], xy2[1], xy2[0] + ovalSize, xy2[1] + ovalSize);
            canvas.drawOval(rectF, compOvalPaint);
            canvas.drawLine(xy1[0] + ovalSize / 2, xy1[1] + ovalSize / 2,
                            xy2[0] + ovalSize / 2, xy2[1] + ovalSize / 2, compRoutePaint);
        }
    }

    // draw the user's route
    private void drawUserRoute(Canvas canvas) {
        userDistance = 0.0f;
        for (int i = 0; i < DIM; i++) {
            int from = userRoute[i];
            if (from >= 0) {
                PlaceActivity.City city1 = placeActivity.cities.get(from);
                float[] xy1 = getCanvasXY(city1);
                RectF rectF = new RectF(xy1[0], xy1[1], xy1[0] + ovalSize, xy1[1] + ovalSize);
                canvas.drawOval(rectF, userOvalPaint);
                if (userRoute[i+1] >= 0) {
                    int to = userRoute[i+1];
                    PlaceActivity.City city2 = placeActivity.cities.get(to);
                    float[] xy2 = getCanvasXY(city2);
                    rectF = new RectF(xy2[0], xy2[1], xy2[0] + ovalSize, xy2[1] + ovalSize);
                    canvas.drawOval(rectF, userOvalPaint);
                    canvas.drawLine(xy1[0] + ovalSize / 2, xy1[1] + ovalSize / 2,
                                    xy2[0] + ovalSize / 2, xy2[1] + ovalSize / 2, userRoutePaint);
                    userDistance += placeActivity.distances[from][to];
                }
            }
        }
    }

    // run without user input if user has not clicked in x seconds
    private void autoRunCheck(boolean comp_finished) {
        final int AUTORUN_DELAY = 7500;    // delay before auto run begins (in msec.)

        if (userClicked) return;
        currTime = System.currentTimeMillis();
        if ( (currTime - startTime) > AUTORUN_DELAY) {
            if (comp_finished) {                    // computer has found a solution
                if ((currTime - prevTime) > 250) {  // show it in steps of 0.25 seconds
                    autoCount += 1;
                    if (autoCount >= (DIM + 1)) {
                        autoCount = DIM + 1;
                        MAP_STATUS = MAP_FINISHED;
                    }                               // copy the comp. route to user route
                    for (int i = 0; i < autoCount; i++) {
                        userRoute[i] = placeActivity.gaManager.bestRoute[i];
                        userCount = i + 1;
                    }
                    prevTime = currTime;
                }
            } else {    // comp is not done yet
                String out = placeActivity.getText(R.string.tsp_progress) + " "  +
                        getTimeDurationHHMMSS(System.currentTimeMillis() - startTime);
                placeActivity.statusView.setText(out);
            }
        }
    }

    // set the text size for paint given desiredWidth
    private void setTextSize(Paint paint, float desiredWidth, String text) {

        // Get the bounds of the text
        final float testTextSize = 88f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    // calculate the oval size given desired width
    private void setOvalSizeForWidth(Paint paint, float desiredWidth) {
        // Get the bounds of the text
        final float testTextSize = 88f * mxScaleFactor;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds("A", 0, "A".length(), bounds);
        ovalSize = testTextSize * desiredWidth / bounds.width();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        userClicked = true;
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            // possible click on a city
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                if (MAP_STATUS == MAP_RUNNING) handleUserClick(x, y);
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                invalidate();
                break;
            }
            // a possible drag
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

               // checkBounds(x, y, 0.0f);
                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;
                    mPosX += dx;
                    mPosY += dy;
                    invalidate();
                }
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    // handle the user click on a node
    // wait till cities file has been read
    public void handleUserClick(final float pass_x, final float pass_y) {
        float map[] = {pass_x, pass_y};
        canvasReverseTransform.mapPoints(map);
        float x = map[0]; float y = map[1];

        // wait till the distance and cities files have been read
        if ( (userCount < DIM) &&
             (placeActivity.GA_STATUS >= placeActivity.GA_RUNNING) ) {

            int cityIndex = getClosestCity(x, y);   // get the closest city index
            if (cityIndex == -1) return;            // check for a valid city
            int dupCheck = duplicate(cityIndex);    // check for duplicate city
            if (dupCheck == -1) {
                userRoute[userCount] = cityIndex;
                if (++userCount == DIM) {
                    userRoute[DIM] = userRoute[0];
                    MAP_STATUS = MAP_FINISHED;
                }
                if (placeActivity.vibrate_city) vibrator.vibrate(25);
                String left = context.getString(R.string.left);
                int numLeft = (DIM - userCount);
                String toastText = (numLeft == 0)? placeActivity.getText(R.string.tsp_restart) + "":
                        placeActivity.cities.get(cityIndex).getName() + " ( " + numLeft + " " + left + " )";
                placeActivity.statusView.setText(toastText);
            } else { // zap the rest of the route, i.e. a correction
                userCount = dupCheck + 1;
                while (userCount < DIM) userRoute[userCount++] = -1;
                userCount = dupCheck + 1;   // restore last selected place
            }
        }
    }

    // get the index of the closest city
    public int getClosestCity(float x, float y) {
        final int T_RANGE = 40;  // touch pixel range

        int min_index = -1;
        float min_distance = 1000000000.0f;
        for (int i = 0; i < DIM; i++) {
            PlaceActivity.City city = placeActivity.cities.get(i);
            float[] xy = getCanvasXY(city); // get canvas coordinates for city
            if ((xy[0] - T_RANGE) < x && x < (xy[0] + T_RANGE) &&
                (xy[1] - T_RANGE) < y && y < (xy[1] + T_RANGE)) {
                float distance = Math.abs(x-xy[0]) + Math.abs(y-xy[1]);
                if (distance < min_distance) {
                    min_index = i;
                    min_distance = distance;
                }
            }
        }
        return min_index;
    }

    // check if the user is clicking on a selected node
    public int duplicate(int dup) {
        int a = -1;
        for (int i = 0; i < userCount; i++)
            if (userRoute[i] == dup) a = i;
        return a;
    }

    // Get the longitude and latitude for an X,Y location on the canvas
   //private float[] getLonLat(final float x, final float y) {
   //     float[]map = {x,y};
   //     canvasReverseTransform.mapPoints(map);
   //     float lonLat[] = new float[2];
   //     lonLat[0] = placeActivity.getUpperLeftLon() + (map[0] * placeActivity.getDlon());
   //     lonLat[1] = placeActivity.getUpperLeftLat() - (map[1] * placeActivity.getDlat());
   //     return lonLat;
   //}

    // Get the canvas X, Y location corresponding to the City
    private float[] getCanvasXY(final PlaceActivity.City city) {
        // get the longitude relative to the upper left corner longitude
        float lon = city.getLon();
        lon = (city.getLonLoc().equals(PlaceActivity.half.W))? -lon: lon;
        float dlon = lon - placeActivity.getUpperLeftLon();
        // get the latitude relative to the upper left corner latitude
        float lat = city.getLat();
        lat = (city.getLatLoc()).equals(PlaceActivity.hemisphere.S)? -lat: lat;
        float dlat = placeActivity.getUpperLeftLat() - lat;
        return ( new float[] { dlon / placeActivity.getDlon(), dlat / placeActivity.getDlat() });
    }

    // pure time duration must be in GMT timezone
    public String getTimeDurationHHMMSS(long milliseconds) {
        final String shortTimeFormat = "mm:ss";
        final SimpleDateFormat sdf_short_time = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
        // round up the milliseconds to the nearest second
        long millis = 1000 * ((milliseconds + 500) / 1000);
                sdf_short_time.setTimeZone(TimeZone.getTimeZone("GMT"));
        return (sdf_short_time.format(millis));
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Don't let the object get too small or too large.
            mxScaleFactor *= detector.getScaleFactor();
            mxScaleFactor = Math.max(0.1f, Math.min(mxScaleFactor, 5.0f));
            myScaleFactor *= detector.getScaleFactor();
            myScaleFactor = Math.max(0.1f, Math.min(myScaleFactor, 5.0f));
            invalidate();
            return true;
        }
    }

    // pause for wait msec.
    private void pause (long wait) {
        final String TAG = "YatraView";
        try {
            Thread.sleep(wait);
        } catch (InterruptedException ie) {
            Log.d(TAG, "Could not pause: " + ie.getMessage());

        }
    }

}