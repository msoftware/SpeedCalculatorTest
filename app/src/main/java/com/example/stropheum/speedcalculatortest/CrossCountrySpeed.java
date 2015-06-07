package com.example.stropheum.speedcalculatortest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import com.example.stropheum.speedcalculatortest.SpeedCalculationService.SpeedCalculationBinder;

public class CrossCountrySpeed extends ActionBarActivity {

    // Allow 15 seconds of error for time calculations
    final double MILE_TIME_ERROR = 0.25;

    // Goal mile times for each part
    final double PART_ONE_GOAL_PACE   = 12.0;
    final double PART_TWO_GOAL_PACE   = 11.0;
    final double PART_THREE_GOAL_PACE = 10.0;
    final double PART_FOUR_GOAL_PACE  =  9.0;
    final double PART_FIVE_GOAL_PACE  =  8.0;


    // Duration for each part in milliseconds
    final int PART_ONE_DURATION   = 120000;
    final int PART_TWO_DURATION   = 120000;
    final int PART_THREE_DURATION = 120000;
    final int PART_FOUR_DURATION  = 120000;
    final int PART_FIVE_DURATION  = 120000;

    public SpeedCalculationService speedCalculator;
    boolean isBound = false;

    double currentPace, goalPace;
    double speed;

    // Tracks the time a part starts and how long it has been running for
    double timeStart, timeElapsed;

    // Tracks the start time and elapsed time of individual parts
    double partOneTimeStart,   partOneTimeElapsed;
    double partTwoTimeStart,   partTwoTimeElapsed;
    double partThreeTimeStart, partThreeTimeElapsed;
    double partFourTimeStart,  partFourTimeElapsed;
    double partFiveTimeStart,  partFiveTimeElapsed;

    // Value to determine if the part has run for the first time
    boolean partOneFirstRun,  partTwoFirstRun,  partThreeFirstRun,
            partFourFirstRun, partFiveFirstRun;

    String paceText;
    Intent i;

    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cross_country_speed);

        i = new Intent(this, SpeedCalculationService.class);

        // Starts the service for calulating user's speed
        bindService(i, speedConnection, Context.BIND_AUTO_CREATE);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        String paceText = "Waiting for GPS";
        updatePaceText(paceText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cross_country_speed, menu);
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
        stopService(new Intent(CrossCountrySpeed.this, SpeedCalculationService.class));
        unbindService(speedConnection);
        finish();
        return;
    }

    /**
     * Updates the display to show the current speed
     *
     * @param speed The current speed of the user
     */
    private void updateSpeed(double speed) {
        final TextView speedVal = (TextView) findViewById(R.id.SpeedVal);
        speedVal.setText(String.format("%.2f", speed));
    }

    /**
     * Updates the current estimated mile time
     *
     * @param currentPace User's current mile time
     */
    private void updateCurrentPace(double currentPace) {
        int minutes = (int) currentPace;
        if (minutes > 9999) {
            minutes = 9999;
        }
        int seconds = (int) (((currentPace * 100) % 100) * 0.6);
        final TextView emtVal = (TextView) findViewById(R.id.emtVal);
        emtVal.setText(String.format("%d:%02d", minutes, seconds));
    }

    /**
     * Updates the current goal mile time
     *
     * @param goalPace New goal mile time
     */
    private void updateGoalPace(double goalPace) {
        int minutes = (int) goalPace;
        int seconds = (int) (((goalPace * 100) % 100) * 0.6);
        final TextView gmtVal = (TextView) findViewById(R.id.gmtVal);
        gmtVal.setText(String.format("%d:%02d", minutes, seconds));
    }

    /**
     * Updates the current pace text
     *
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
        timeStart = System.currentTimeMillis(); // Reset alert interval

        if (currentPace > goalPace + MILE_TIME_ERROR) {
            paceText = "Speed up";
            vibrator.vibrate(300);

            try {
                Thread.sleep(300);
            } catch (Exception e) {
            }
            vibrator.vibrate(300);
            try {
                Thread.sleep(300);
            } catch (Exception e) {
            }
            vibrator.vibrate(300);

        } else if (currentPace < goalPace - MILE_TIME_ERROR) {
            paceText = "Slow Down";
            vibrator.vibrate(1000);
        } else {
            paceText = "Perfect Pace!";
        }
        updatePaceText(paceText);
    }

    ServiceConnection speedConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpeedCalculationService.SpeedCalculationBinder binder = (SpeedCalculationService.SpeedCalculationBinder) service;
            isBound = true;
            speedCalculator = binder.getService();

            partOneBegin();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    /**
     * Method called when Speed Calculation Service is successfully bound
     */
    public void partOneBegin() {
        partOneFirstRun = true;
        speedCalculator.resetValues();

        final Timer partOneTimer = new Timer();
        partOneTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!speedCalculator.searchingForSignal()) {

                    // Forces GUI updates to happen on the Activity UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (partOneFirstRun) {
                                timeStart = System.currentTimeMillis();
                                partOneTimeStart = System.currentTimeMillis();

                                paceText = "Begin!";
                                updatePaceText(paceText);

                                goalPace = PART_ONE_GOAL_PACE;
                                updateGoalPace(goalPace);

                                partOneFirstRun = false;
                            }

                            // Tracks the elapsed time since last alert
                            timeElapsed = System.currentTimeMillis() - timeStart;

                            // Tracks the total elapsed time of the workout part
                            partOneTimeElapsed = System.currentTimeMillis() - partOneTimeStart;

                            speed = speedCalculator.getCurrentSpeed();
                            updateSpeed(speed);

                            currentPace = 60 / speed;
                            updateCurrentPace(currentPace);

                            if (partOneTimeElapsed >= PART_ONE_DURATION) {
                                // Terminate current part and start the next
                                partOneTimer.cancel();
                                partTwoBegin();
                            }

                            if (timeElapsed >= 9750) {// slightly less than 10 second to account for loop intervals
                                paceAlert();
                            }
                        }
                    });

                }
            }
        }, 0, 5000); // Updates every 5 seconds

    }

    /**
     * Method called when part one is completed
     */
    public void partTwoBegin() {
        partTwoFirstRun = true;
        speedCalculator.resetValues();

        final Timer partTwoTimer = new Timer();
        partTwoTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!speedCalculator.searchingForSignal()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (partTwoFirstRun) {
                                timeStart = System.currentTimeMillis();
                                partTwoTimeStart = System.currentTimeMillis();

                                paceText = "Part Two!";
                                updatePaceText(paceText);

                                goalPace = PART_TWO_GOAL_PACE;
                                updateGoalPace(goalPace);

                                partTwoFirstRun = false;
                            }

                            // Tracks the elapsed time since last alert
                            timeElapsed = System.currentTimeMillis() - timeStart;

                            // Tracks the total elapsed time of the workout part
                            partTwoTimeElapsed = System.currentTimeMillis() - partTwoTimeStart;

                            speed = speedCalculator.getCurrentSpeed();
                            updateSpeed(speed);

                            currentPace = 60 / speed;
                            updateCurrentPace(currentPace);

                            if (partTwoTimeElapsed >= PART_TWO_DURATION) {
                                // Terminate current part and start the next
                                partTwoTimer.cancel();
                                partThreeBegin();
                            }

                            if (timeElapsed >= 9750) {// slightly less than 10 second to account for loop intervals
                                paceAlert();
                            }
                        }
                    });
                }
            }
        }, 0, 5000);
    }

    /**
     * Method called when part two is completed
     */
    public void partThreeBegin() {
        partThreeFirstRun = true;
        speedCalculator.resetValues();

        final Timer partThreeTimer = new Timer();
        partThreeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!speedCalculator.searchingForSignal()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (partThreeFirstRun) {
                                timeStart = System.currentTimeMillis();
                                partThreeTimeStart = System.currentTimeMillis();

                                paceText = "Part Three!";
                                updatePaceText(paceText);

                                goalPace = PART_THREE_GOAL_PACE;
                                updateGoalPace(goalPace);

                                partThreeFirstRun = false;
                            }

                            // Tracks the elapsed time since last alert
                            timeElapsed = System.currentTimeMillis() - timeStart;

                            // Tracks the total elapsed time of the workout part
                            partThreeTimeElapsed = System.currentTimeMillis() - partThreeTimeStart;

                            speed = speedCalculator.getCurrentSpeed();
                            updateSpeed(speed);

                            currentPace = 60 / speed;
                            updateCurrentPace(currentPace);

                            if (partThreeTimeElapsed >= PART_THREE_DURATION) {
                                // Terminate current part and start the next
                                partThreeTimer.cancel();
                                partFourBegin();
                            }

                            if (timeElapsed >= 9750) {// slightly less than 10 second to account for loop intervals
                                paceAlert();
                            }
                        }
                    });
                }
            }
        }, 0, 5000);
    }

    /**
     * Method called when part three is completed
     */
    public void partFourBegin() {
        partFourFirstRun = true;
        speedCalculator.resetValues();

        final Timer partFourTimer = new Timer();
        partFourTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!speedCalculator.searchingForSignal()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (partFourFirstRun) {
                                timeStart = System.currentTimeMillis();
                                partFourTimeStart = System.currentTimeMillis();

                                paceText = "Part Four!";
                                updatePaceText(paceText);

                                goalPace = PART_FOUR_GOAL_PACE;
                                updateGoalPace(goalPace);

                                partFourFirstRun = false;
                            }

                            // Tracks the elapsed time since last alert
                            timeElapsed = System.currentTimeMillis() - timeStart;

                            // Tracks the total elapsed time of the workout part
                            partFourTimeElapsed = System.currentTimeMillis() - partFourTimeStart;

                            speed = speedCalculator.getCurrentSpeed();
                            updateSpeed(speed);

                            currentPace = 60 / speed;
                            updateCurrentPace(currentPace);

                            if (partFourTimeElapsed >= PART_FOUR_DURATION) {
                                // Terminate current part and start the next
                                partFourTimer.cancel();
                                partFiveBegin();
                            }

                            if (timeElapsed >= 9750) {// slightly less than 10 second to account for loop intervals
                                paceAlert();
                            }
                        }
                    });
                }
            }
        }, 0, 5000);
    }

    /**
     * Method called when part four is completed
     */
    public void partFiveBegin() {
        partFiveFirstRun = true;
        speedCalculator.resetValues();

        final Timer partFiveTimer = new Timer();
        partFiveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!speedCalculator.searchingForSignal()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (partFiveFirstRun) {
                                timeStart = System.currentTimeMillis();
                                partFiveTimeStart = System.currentTimeMillis();

                                paceText = "Part Five!";
                                updatePaceText(paceText);

                                goalPace = PART_FIVE_GOAL_PACE;
                                updateGoalPace(goalPace);

                                partFiveFirstRun = false;
                            }

                            // Tracks the elapsed time since last alert
                            timeElapsed = System.currentTimeMillis() - timeStart;

                            // Tracks the total elapsed time of the workout part
                            partFiveTimeElapsed = System.currentTimeMillis() - partFiveTimeStart;

                            speed = speedCalculator.getCurrentSpeed();
                            updateSpeed(speed);

                            currentPace = 60 / speed;
                            updateCurrentPace(currentPace);

                            if (partFiveTimeElapsed >= PART_FIVE_DURATION) {
                                paceText = "Workout Done!";
                                updatePaceText(paceText);
                                // Terminate current part and start the next
                                partFiveTimer.cancel();
                            }

                            if (timeElapsed >= 9750) {// slightly less than 10 second to account for loop intervals
                                paceAlert();
                            }
                        }
                    });
                }
            }
        }, 0, 5000);
    }
}