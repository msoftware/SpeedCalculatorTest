package com.example.stropheum.speedcalculatortest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.os.Vibrator;
import com.example.stropheum.speedcalculatortest.SpeedCalculationService.SpeedCalculationBinder;

public class SpeedAlarmActivity extends ActionBarActivity {

    public SpeedCalculationService speedCalculator;
    boolean isBound = false;

    final int MILLI_TO_SEC = 1000;
    final int SEC_TO_HOUR = 3600;

    double currentPace, goalPace;
    String paceText;

    Vibrator vibrator;

    // Allow 15 seconds of error for time calculations
    final double MILE_TIME_ERROR = 0.25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_alarm);

        //Intent i = new Intent(this, SpeedCalculationService.class);

        //bindService(i, speedConnection, Context.BIND_AUTO_CREATE);
        //startService(i);

        // Starts the service for calulating user's speed
        //startService(new Intent(getBaseContext(), SpeedCalculationService.class)); // was below bind before

//        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//
//        double startTime, elapsedTime;
//        double alertIntervalStart, alertIntervalElapsed;
//        double speed, goalPace, currentPace;
//
//        String paceText = "Waiting for GPS signal";
//        updatePaceText(paceText);
//
//        if (isBound);
//        // Delays workout until the service finds a signal
//        //while (!speedCalculator.gpsSignalFound());
//
//        // Once GPS connection is established, being the workout
//        paceText = "Begin!";
//        updatePaceText(paceText);
//
//        ///////////////////
//        // Part One Begins
//        /////////////////
//        startTime = System.currentTimeMillis();
//        elapsedTime = 0;
//        alertIntervalStart = startTime; // Initialize 30 second timer on workout start
//
//        goalPace = 10.0;
//        updateGoalPace(goalPace);
//
//        do {
//            // Update time since last alert
//            alertIntervalElapsed = System.currentTimeMillis() - alertIntervalStart;
//
//            //speed = speedCalculator.getCurrentSpeed();
//            speed = 1;
//            currentPace = 60 / speed;
//
//            // Update speed and pace every second
//            if (elapsedTime >= 1.0 * MILLI_TO_SEC) {
//                updateSpeed(speed);
//                updateCurrentPace(currentPace);
//            }
//
//            // Alerts user if 30 seconds have gone by with no change
//            if (alertIntervalStart >= 30 * MILLI_TO_SEC) {
//                paceAlert();
//                alertIntervalStart = System.currentTimeMillis();
//            }
//
//            // If 5 seconds have elapsed and perfect pace, alert user
//            if (alertIntervalElapsed >= 5 * MILLI_TO_SEC && checkPace(currentPace, goalPace)) {
//                paceAlert();
//                alertIntervalStart = System.currentTimeMillis();
//            }
//
//            elapsedTime = System.currentTimeMillis() - startTime;
//        } while (elapsedTime < 120 * MILLI_TO_SEC);
//
//        paceText = "Workout Complete!";
//        updatePaceText(paceText);

//        ///////////////////
//        // Part Two Begins
//        /////////////////
//        startTime = System.currentTimeMillis();
//        elapsedTime = 0;
//        alertIntervalStart = startTime; // Initialize 30 second timer on workout start
//
//        goalPace = 6.0;
//
//        do {
//
//            elapsedTime = System.currentTimeMillis() - startTime;
//        } while (elapsedTime < 60 * MILLI_TO_SEC);
//
//

    }

    /**
     * Checks if the user is running in an acceptable range of the goal pace
     * @param currentPace Current speed of the user
     * @param goalPace Goal speed of the user
     * @return True if the pace is acceptable, false otherwise
     */
    private boolean checkPace(double currentPace, double goalPace) {
        boolean result = true;
        if (currentPace > goalPace + MILE_TIME_ERROR || currentPace < goalPace - MILE_TIME_ERROR) {
            result = false;
        }
        return result;
    }

    /**
     * Updates the display to show the current speed
     * @param speed The current speed of the user
     */
    private void updateSpeed(double speed) {
        final TextView speedVal = (TextView) findViewById(R.id.SpeedVal);
        speedVal.setText(String.format("%.2f", speed));
    }

    /**
     * Updates the current estimated mile time
     * @param currentPace User's current mile time
     */
    private void updateCurrentPace(double currentPace) {
        int minutes = (int)currentPace;
        int seconds = (int)(((currentPace * 100) % 100) * 0.6);
        final TextView emtVal = (TextView) findViewById(R.id.emtVal);
        emtVal.setText(String.format("%d:%02d", minutes, seconds));
    }

    /**
     * Updates the current goal mile time
     * @param goalPace New goal mile time
     */
    private void updateGoalPace(double goalPace) {
        int minutes = (int)goalPace;
        int seconds = (int)(((goalPace * 100) % 100) * 0.6);
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
    private void paceAlert() {
        if (currentPace > goalPace + MILE_TIME_ERROR) {
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
        } else if (currentPace < goalPace - MILE_TIME_ERROR) {
            paceText = "Slow Down";
            vibrator.vibrate(1000);
        } else {
            paceText = "Perfect Pace!";
        }
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
        // Terminate the speed calculation service
        stopService(new Intent(getBaseContext(), SpeedCalculationService.class));
        finish();
        return;
    }

    ServiceConnection speedConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpeedCalculationBinder  binder = (SpeedCalculationBinder) service;
            speedCalculator = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}