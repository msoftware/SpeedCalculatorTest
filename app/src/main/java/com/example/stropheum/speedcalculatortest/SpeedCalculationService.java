package com.example.stropheum.speedcalculatortest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.location.LocationListener;

public class SpeedCalculationService extends Service {

    final int MILLI_TO_SEC = 1000; // Number of milliseconds in a second
    final int SEC_TO_HOUR = 3600;  // Number of seconds in an hour

    private final IBinder binder = new SpeedCalculationBinder();

    LocationManager  locationManager;
    LocationListener locationListener;
    boolean signalFound = false;

    // Tracks distance traveled between location calls
    double distanceTraveled = 0;
    double speed = 0;

    // Tracks the longitude and latitude of the previous and current location calls
    double lonNew, lonOld;
    double latNew, latOld;

    double startLatitude, startLongitude;
    double totalDistance;

    double speedSum = 0; // Sums speed calculations between speed update requests
    int ticks = 0;      // Tracks number of location reqeuests in between speed update requests

    public SpeedCalculationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            double startTime;
            double currentTime, deltaTime;

            public void onLocationChanged(Location location) {
                ticks++;

                if (!signalFound) {
                    // Prime old locations for first distance calculation
                    latOld = Math.toRadians(location.getLatitude());
                    lonOld = Math.toRadians(location.getLongitude());
                    // Initialize starting time
                    startTime = System.currentTimeMillis();
                    // Initialize starting location
                    startLatitude  = Math.toRadians(location.getLatitude());
                    startLongitude = Math.toRadians(location.getLongitude());

                    signalFound = true;
                }

                latNew = Math.toRadians(location.getLatitude());
                lonNew = Math.toRadians(location.getLongitude());

                // Calculate elapsed time (in hours) since last location request
                currentTime = System.currentTimeMillis();
                deltaTime = (currentTime - startTime) / MILLI_TO_SEC / SEC_TO_HOUR;

                // Compute distance traveled (in miles) since last location request
                distanceTraveled = haversine(latOld, lonOld, latNew, lonNew);

                // Compute speed (in miles/hour) since last location request
                speedSum += distanceTraveled / deltaTime;

                latOld = latNew;
                lonOld = lonNew;

                startTime = currentTime;
            }

            public void onStatusChanged(String Provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // This service runs until it is stopped
        //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
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
        final double EARTH_RADIUS_M = 3959.0;

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

    /**
     * Returns the current calculated speed of the user
     * @return the current speed in mph format
     */
    public double getCurrentSpeed() {
        speed = speedSum / ticks;
        speedSum = 0;
        ticks = 0;

        return speed;
    }

    /**
     * Computes the total distance traveled during workout
     * @return the total distance traveled (in miles)
     */
    public double getCurrentDistance() {
        double result = haversine(startLatitude, startLongitude, latNew, lonNew);
        return result;
    }

    /**
     * Resets speedsum and ticks
     * prevents average speed from carrying over between parts
     */
    public void resetValues() {
        speedSum = 0;
        ticks = 0;
    }

    /**
     * Method to check if GPS connection is established
     * @return true if first location check has been completed
     */
    public boolean searchingForSignal() {
        return !this.signalFound;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        //Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }

    // Binder class that will bind to the workout activities
    public class SpeedCalculationBinder extends Binder {
        SpeedCalculationService getService() {
            return SpeedCalculationService.this;
        }
    }

}