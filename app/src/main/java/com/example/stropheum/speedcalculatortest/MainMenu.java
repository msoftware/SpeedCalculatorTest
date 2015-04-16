package com.example.stropheum.speedcalculatortest;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.view.View.OnClickListener;


public class MainMenu extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Initialize click listeners for workout buttons
        configureWorkoutButtonOne();
        configureWorkoutButtonTwo();
        configureWorkoutButtonThree();
        configureWorkoutButtonFour();
        configureWorkoutButtonFive();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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

    /**
     * Configures the click listener for workout button one
     */
    private void configureWorkoutButtonOne() {
        ImageButton wb1 = (ImageButton) findViewById(R.id.workoutButton1);
        wb1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SpeedAlarmActivity.class));
            }
        });
    }

    /**
     * Configures the click listener for workout button two
     */
    private void configureWorkoutButtonTwo() {
        ImageButton wb2 = (ImageButton) findViewById(R.id.workoutButton2);
        wb2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SpeedAlarmActivity.class));
            }
        });
    }

    /**
     * Configures the click listener for workout button three
     */
    private void configureWorkoutButtonThree() {
        ImageButton wb3 = (ImageButton) findViewById(R.id.workoutButton3);
        wb3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SpeedAlarmActivity.class));
            }
        });
    }

    /**
     * Configures the click listener for workout button four
     */
    private void configureWorkoutButtonFour() {
        ImageButton wb4 = (ImageButton) findViewById(R.id.workoutButton4);
        wb4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SpeedAlarmActivity.class));
            }
        });
    }

    /**
     * Configures the click listener for workout button five
     */
    private void configureWorkoutButtonFive() {
        ImageButton wb5 = (ImageButton) findViewById(R.id.workoutButton5);
        wb5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SpeedAlarmActivity.class));
            }
        });
    }
}
