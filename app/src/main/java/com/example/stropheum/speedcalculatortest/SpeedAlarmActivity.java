package com.example.stropheum.speedcalculatortest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.os.Vibrator;


public class SpeedAlarmActivity extends ActionBarActivity {

    final int MILLI_TO_SEC = 1000;
    final int SEC_TO_HOUR = 3600;
    Vibrator vibrator;

    // Allow 15 seconds of error for time calculations
    final double MILE_TIME_ERROR = 0.25;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator  = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        setContentView(R.layout.activity_speed_alarm);

        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            double lonNew, lonOld;
            double latNew, latOld;

            // Values to track display interval
            long paceStartTime, paceDeltaTime;
            long speedStartTime, speedDeltaTime;

            // Values to track computation interval
            long timeNew;
            long timeOld = System.currentTimeMillis();
            long deltaTime;

            double distance, speed, mileTime;
            double goalMileTime = 2;

            int ticks = 0;
            double speedSum = 0;

            boolean firstRun = true;

            String paceText;


            public void onLocationChanged(Location location) {
                // Calculate change in time
                if (firstRun) {
                    paceStartTime = System.currentTimeMillis();
                    speedStartTime = System.currentTimeMillis();
                    //timeOld = System.currentTimeMillis();
                    lonOld = Math.toRadians(location.getLongitude());
                    latOld = Math.toRadians(location.getLongitude());
                    updateGmt(goalMileTime);
                    paceText = "Begin!";
                    updatePaceText(paceText);
                    firstRun = false;
                }
                timeNew = System.currentTimeMillis();
                deltaTime = Math.abs(timeNew - timeOld);

                // Compute new positional coordinates
                lonNew = Math.toRadians(location.getLongitude());
                latNew = Math.toRadians(location.getLatitude());

                distance = haversine(latOld, lonOld, latNew, lonNew);
                if (distance > 1000) { distance = 0; }

                // Add current speed and count number of ticks
                speedSum += (distance / deltaTime) * MILLI_TO_SEC * SEC_TO_HOUR;
                ticks++;
                paceDeltaTime = System.currentTimeMillis() - paceStartTime;
                speedDeltaTime = System.currentTimeMillis() - speedStartTime;

                if (paceDeltaTime >= 30000) { // Update all values and pace text
                    speed = speedSum / ticks;
                    mileTime = 60 / speed;

                    checkPace();

                    // Update values on screen
                    if (speed > 10000) {
                        speed = 0;
                    }
                    updateSpeed(speed);
                    updateEmt(mileTime);
                    updatePaceText(paceText);

                    ticks = 0;
                    speedSum = 0;
                    paceStartTime = System.currentTimeMillis();
                } else if (speedDeltaTime >= 5000) { // Update values without interfering with average
                    speed = speedSum / ticks;
                    mileTime = 60 / speed;

                    if (speed > 10000) {
                        speed = 0;
                    }
                    updateSpeed(speed);
                    updateEmt(mileTime);

                    // If player resumes proper pace, reset interval timer and prompt pace
                    if (mileTime > goalMileTime - MILE_TIME_ERROR &&
                        mileTime < goalMileTime + MILE_TIME_ERROR) {
                        checkPace();
                        //paceStartTime = System.currentTimeMillis();
                    }

                    speedStartTime = System.currentTimeMillis();
                }

            // Store old Coordinates and time
            latOld = latNew;
            lonOld = lonNew;
            timeOld = timeNew;

        }


            public void onStatusChanged(String Provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

            /**
             * Updates the current speed of the user
             * @param speed the current speed value
             */
            private void updateSpeed(double speed) {
                final TextView speedVal = (TextView) findViewById(R.id.SpeedVal);
                speedVal.setText(String.format("%.2f", speed));
            }

            /**
             * Updates the current estimated mile time
             * @param mileTime current EMT
             */
            private void updateEmt(double mileTime) {
                int minutes = (int)mileTime;
                int seconds = (int)(((mileTime * 100) % 100) * 0.6);
                final TextView emtVal = (TextView) findViewById(R.id.emtVal);
                emtVal.setText(String.format("%d:%02d", minutes, seconds));
            }

            /**
             * Updates the current goal mile time
             * @param goalMileTime new goal mile time
             */
            private void updateGmt(double goalMileTime) {
                int minutes = (int)goalMileTime;
                int seconds = (int)(((mileTime * 100) % 100) * 0.6);
                final TextView gmtVal = (TextView) findViewById(R.id.gmtVal);
                gmtVal.setText(String.format("%d:%02d", minutes, seconds));
            }

            /**
             * Updates the current pace text
             * @param paceText indicator for user;s current speed in relation to goal time
             */
            private void updatePaceText(String paceText) {
                final TextView pace = (TextView) findViewById(R.id.paceView);
                pace.setText(paceText);
            }

            /**
             * Checks current pace and assigns appropriate text
             */
            private void checkPace() {
                if (mileTime > goalMileTime + MILE_TIME_ERROR) {
                    paceText = "Speed up";
                    vibrator.vibrate(300);
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {}
                    vibrator.vibrate(300);
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {}
                    vibrator.vibrate(300);
                } else if (mileTime < goalMileTime - MILE_TIME_ERROR) {
                    paceText = "Slow Down";
                    vibrator.vibrate(1000);
                } else {
                    paceText = "Perfect Pace!";
                }
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    /**
     * Computes distance (in miles) between two coordinates using the haversine formula
     * @param lat1 latitude  of previous location
     * @param lon1 longitude of previous location
     * @param lat2 latitude  of current  location
     * @param lon2 longitude of current  location
     * @return distance in miles
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_M = 3959;

        double dLon, dLat, a, c, distance;

        // Calculate distance traveled using Haversine formula
        dLon = lon2 - lon1;
        dLat = lat2 - lat1;

        a = Math.sin(dLat/2.0) * Math.sin(dLat/2.0) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon/2.0) * Math.sin(dLon/2.0);
        System.out.println("a = " + a);

        c = 2.0 * Math.atan(Math.sqrt(a));
        System.out.println("c = " + c);
        distance = EARTH_RADIUS_M * c;

        return distance;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speed_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        locationManager.removeUpdates(locationListener);
        finish();
        return;
    }

}
