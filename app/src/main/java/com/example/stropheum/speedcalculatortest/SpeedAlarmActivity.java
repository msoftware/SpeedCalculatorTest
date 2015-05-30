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

import java.util.Timer;
import java.util.TimerTask;

public class SpeedAlarmActivity extends ActionBarActivity {

    public SpeedCalculationService speedCalculator;
    boolean isBound = false;

    // Millisecond conversion values
    final int MILLI_TO_SEC = 1000;
    final int SEC_TO_HOUR = 3600;

    // Goal mile times for each part
    final double PART_ONE_GOAL_PACE = 8.0;

    double currentPace, goalPace;
    double speed;
    String paceText;
    Intent i;

    Vibrator vibrator;

    // Allow 15 seconds of error for time calculations
    final double MILE_TIME_ERROR = 0.25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_alarm);

        Intent i = new Intent(this, SpeedCalculationService.class);

        // Starts the service for calulating user's speed
        bindService(i, speedConnection, Context.BIND_AUTO_CREATE);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        double startTime, elapsedTime;
        double alertIntervalStart, alertIntervalElapsed;
        double goalPace, currentPace;

        String paceText = "Waiting for GPS signal";
        updatePaceText(paceText);

        // Once GPS connection is established, being the workout
        paceText = "Begin!";
        updatePaceText(paceText);

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
        stopService(new Intent(SpeedAlarmActivity.this, SpeedCalculationService.class));
        unbindService(speedConnection);
        finish();
        return;
    }

    ServiceConnection speedConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpeedCalculationBinder  binder = (SpeedCalculationBinder) service;
            isBound = true;
            speedCalculator = binder.getService();

            beginWorkout();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    /**
     * Method called when Speed Calculation Service is successfully bound
     */
    public void beginWorkout() {
        Timer speedTimer = new Timer();
        updateGoalPace(PART_ONE_GOAL_PACE);
        speedTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!speedCalculator.searchingForSignal()) {

                    // Forces GUI updates to happen on the Activity UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            speed = speedCalculator.getCurrentSpeed();
                            updateSpeed(speed);

                            currentPace = 60 / speed;
                            updateCurrentPace(currentPace);
                        }
                    });

                }
            }
        }, 0, 1000);

    }
}