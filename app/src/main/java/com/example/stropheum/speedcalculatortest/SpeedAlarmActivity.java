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

            long timeNew;
            long timeOld = System.currentTimeMillis();
            long deltaTime;

            double distance, dLat, dLon, speed;
            double a, c;

            boolean firstRun = true;


            public void onLocationChanged(Location location) {
                // Calculate change in time
                if (firstRun) {
                    timeOld = System.currentTimeMillis();
                    firstRun = false;
                }
                timeNew = System.currentTimeMillis();
                deltaTime = Math.abs(timeNew - timeOld);

                // Compute new positional coordinates
                lonNew = location.getLongitude();
                latNew = location.getLatitude();

                if (deltaTime >= 10000) {

                    // Calculate distance traveled using Haversine formula
                    dLon = Math.toRadians(lonNew - lonOld);
                    dLat = Math.toRadians(latNew - latOld);
                    a = Math.sin(dLat/2.0) * Math.sin(dLat/2.0) +
                            Math.sin(dLon/2.0) * Math.sin(dLon/2.0) *
                                    Math.cos(latOld) * Math.cos(latNew);
                    c = 2.0 * Math.asin(Math.sqrt(a));
                    distance = EARTH_RADIUS_M * c;

                    // Compute feet/second
                    //double distanceFeet = distance * 5280;
                    int minutesPerMile = 10;
                    speed = distance / deltaTime * 1000 * 60 * minutesPerMile; // Convert nano to seconds, then minutes, then # minutes

                    // Update values on screen
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

                    // Store old Coordinates and time
                    latOld = latNew;
                    lonOld = lonNew;
                    timeOld = timeNew;
                }

            }



            public void onStatusChanged(String Provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

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
