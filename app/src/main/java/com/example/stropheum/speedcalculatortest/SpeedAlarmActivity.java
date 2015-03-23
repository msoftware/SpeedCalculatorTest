package com.example.stropheum.speedcalculatortest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class SpeedAlarmActivity extends ActionBarActivity {

    final int MILLI_TO_SEC = 1000;
    final int SEC_TO_HOUR = 3600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_alarm);

        LocationManager locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            Calendar calendar = Calendar.getInstance();

            final double EARTH_RADIUS_M = 3959;
            final double EARTH_RADIUS_KM = 6371;

            double lonNew, lonOld;
            double latNew, latOld;

            // Values to track display interval
            long intervalStart, interval;

            // Values to track computation interval
            long timeNew;
            long timeOld = System.currentTimeMillis();
            long deltaTime;

            double distance, speed;

            int ticks = 0;
            double speedSum = 0;

            boolean firstRun = true;


            public void onLocationChanged(Location location) {
                // Calculate change in time
                if (firstRun) {
                    intervalStart = System.currentTimeMillis();
                    timeOld = System.currentTimeMillis();
                    firstRun = false;
                }
                timeNew = System.currentTimeMillis();
                deltaTime = Math.abs(timeNew - timeOld);

                // Compute new positional coordinates
                lonNew = Math.toRadians(location.getLongitude());
                latNew = Math.toRadians(location.getLatitude());

                distance = haversine(latOld, lonOld, latNew, lonNew);

                // Add current speed and count number of ticks

                speedSum += (distance / deltaTime) * MILLI_TO_SEC * SEC_TO_HOUR;
                ticks++;
                interval = System.currentTimeMillis() - intervalStart; // Calculate display interval

                if (interval >= 10000) {
                    // Calculate average speed over time elapsed
                    speed = speedSum / ticks;

                    // Update values on screen
                    updateDisplay(latNew, lonNew, latOld, lonOld, distance, speed);
                    ticks = 0;
                    speedSum = 0;
                    intervalStart = System.currentTimeMillis();
                }

                // Store old Coordinates and time
                latOld = latNew;
                lonOld = lonNew;
                timeOld = timeNew;

            }



            public void onStatusChanged(String Provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

            private void updateDisplay(double latNew, double lonNew, double latOld, double lonOld, double distance, double speed) {
                final TextView latVal = (TextView) findViewById(R.id.LatVal);
                latVal.setText(String.valueOf(latNew));

                final TextView lonVal = (TextView) findViewById(R.id.LonVal);
                lonVal.setText(String.valueOf(lonNew));

                final TextView latVal2 = (TextView) findViewById(R.id.LatVal2);
                latVal2.setText(String.valueOf(latOld));

                final TextView lonVal2 = (TextView) findViewById(R.id.LonVal2);
                lonVal2.setText(String.valueOf(lonOld));

                final TextView distanceVal = (TextView) findViewById(R.id.DistanceVal);
                distanceVal.setText(String.valueOf(distance));

                final TextView speedVal = (TextView) findViewById(R.id.SpeedVal);
                speedVal.setText(String.valueOf(speed));
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
        final double EARTH_RADIUS_KM = 6371;

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
}
