// TODO: 06/04/18 Final Project for COIS 2240 SOFTWARE DESIGN AND MODELLING GROUP 10
//todo: Group 10 - Nikhil Pai Ganesh - 0595517 ; Saalar Faisal - 0580714 ; Gokhan Karasu - 0631945; Isaiah Mutekanga- //
// Description : Android App for a simple single player game with animations, Java Control and Database.
// The game produces hot air balloon and the user has to crash them before it reaches to the top of the screen to gain points.



package com.group10.android.balloonpopper;
// importing the libraries.

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.group10.android.balloonpopper.dialogs.MyAlertDialog;
import com.group10.android.balloonpopper.utils.PreferencesHelper;
import com.group10.android.balloonpopper.utils.SoundHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

// Main class named MainActivity. It extends AppCompatActivity
public class MainActivity extends AppCompatActivity
        implements Balloon.BalloonListener {

    private static final String TAG = "MainActivity"; // String Variable for implementing the main activity

    private static final int BalloonLevel = 12; // integer variable named BalloonLevel for changing the levels
    private static final int AmountOfPins = 3; // integer variable for number of pins in the game which basically works like lives

    private static final int MinimumDelayinAnimation = 500; // integer Variable to determine the minimum delay the animation that makes the balloons rise
    private static final int maximumDelayinAnimation = 1500; // integer Variable to determine the maximum delay in the animation that makes the ballon rise
    private static final int minimumTimeAnimation = 1000; // integerVariable for minimum time the balloons takes rise
    private static final int maximumTimeAnimation = 8000; // integer variable for maximum time the ballonos take to rise.
    private static final String NextLevel = "NextLevel"; // string variable to move to the next level
    private static final String start = "NextLevelStart_game"; // string variable to start the game.

    private ViewGroup content; //Variable for content
    private SoundHelper sound; // Variable for Sound
    private List<ImageView> pinImage = new ArrayList<>(); // Variable for array list of pin images
    private List<Balloon> balloonImage = new ArrayList<>(); // Variable for array list of balloon image
    private TextView Scores, Level; // Text variable to print the scores and level
    private Button go; // Button Variable for the user to play
    private String Next = start; // String Variable to show that the button changes from play game to next level after each level
    private boolean play; // Boolean string to either play the game or stop the game.
    private int[] mBalloonColors = new int[3];
    private int mNextColor, balloonImagePopped, // integer variable so multiple objects come at the same time
            ScrnWdth, ScrnHght,// variable to set the height and width of the screen
            usedPins = 0,  // to display the pins
            score  = 0, level = 1; // to display score and levels

    // Method: OnCreate
    // Method type: Void
    // Returns: boolean = false in reference to Get Background.
    // Description: Sets up background image and makes the game run in fullscreen.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.bground); //App background is set here

//      To load the layout for the activity (empty template)
        setContentView(R.layout.activity_main); // Obtaining the design from activity_main- XML file that contains the design and aspects of the app.


//      Get background reference.
        content = (ViewGroup) findViewById(R.id.content_view);
        if (content == null) throw new AssertionError();
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {// if the app is in action then set it to fullscreen
                    setToFullScreen();
                }
                return false; // // return false - other wise Don't
            }
        });
        setToFullScreen(); // calling the full screen method

//      Get screen dimensions from the layout.
        ViewTreeObserver viewTreeObserver = content.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ScrnWdth = content.getWidth(); // Obtaining the width of the screen
                    ScrnHght = content.getHeight(); // Obtaining the height of the screen
                }
            });
        }

//      For sound effects and background music.
        sound = new SoundHelper(this); // linking to the class SoundHelper
        sound.prepareMusicPlayer(this);

//      Design elements for the screen.

        // calling the image of the pin that we created in XML
        pinImage.add((ImageView) findViewById(R.id.pushpin1));
        pinImage.add((ImageView) findViewById(R.id.pushpin2));
        pinImage.add((ImageView) findViewById(R.id.pushpin3));

        // Portray the text for score change and level change
        Scores = (TextView) findViewById(R.id.score_display);
        Level = (TextView) findViewById(R.id.level_display);

//      Display current level and score
        updateDisplay(); // calling the changes we made in the display



//      Get button references
        go = (Button) findViewById(R.id.go_button); // button to play the game.

//      Handle button click
        if (go == null) throw new AssertionError(); // if the button isn't there then show error
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (play) { // if play is being clicked
                    stopGame(); // stop the game
                } else { // otherwise
                    switch (Next) { // if Next is clicked
                        case start: // in case start is being clicked
                            startGame(); // start the game
                            break;
                        case NextLevel: // if next level is clicked
                            start();  // start the next level of the ga,e
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        stopGame(); // stop game called
        super.onBackPressed();
    }

//Method: setToFullScreen
    //method type : void
    // description sets the app to full screen


    private void setToFullScreen() {

        // sets to full screen mode.
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    // Method: startGame
    // Description: Sets game logic

    private void startGame() {

        setToFullScreen();

//      Reset score and level
        score  = 0;
        level = 1;

//      Update display
        updateDisplay();
        go.setText(R.string.stop_game);

//      Reset pins
        usedPins = 0;
        for (ImageView pin : pinImage) {
            pin.setImageResource(R.drawable.pin);
        }

//      Start the first level
        start();

        sound.playMusic();
    }

    private void stopGame() {
        go.setText(R.string.play_game);
        play = false;
        gameOver(false);
    }

    private void start() {

//      Display the current level and score
        updateDisplay();
        go.setText(R.string.stop_game);

//      Reset flags for new level
        play = true;
        balloonImagePopped = 0;

//      integer arg for BalloonLauncher indicates the level
        BalloonLauncher mLauncher = new BalloonLauncher();
        mLauncher.execute(level);

    }

    @SuppressLint("StringFormatMatches")
    private void finishLevel() {
        PreferencesHelper.setCurrentScore(this, score );
        PreferencesHelper.setCurrentLevel(this, level);
        Toast.makeText(MainActivity.this,
                String.format(getString(R.string.you_finished_level_n), level),
                Toast.LENGTH_LONG).show();

        play = false;
        level++;
        go.setText(String.format("Start level %s", level));
        Next = NextLevel;
    }

    private void updateDisplay() {
        Scores.setText(String.valueOf(score ));
        Level.setText(String.valueOf(level));
    }

    private void launchBalloon(int x) {

//      Balloon is launched from activity upon progress update from the AsyncTask
//      Create new imageview and set its tint color
        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150, level);
        balloonImage.add(balloon);

//      Reset color for next balloon
        if (mNextColor + 1 == mBalloonColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

//      Set balloon vertical position and dimensions, add to container
        balloon.setX(x);
        balloon.setY(ScrnHght + balloon.getHeight());
        content.addView(balloon);

//      Lets balloon fly
        int duration = Math.max(minimumTimeAnimation, maximumTimeAnimation - (level * 1000));
        balloon.releaseBalloon(ScrnHght, duration);

    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {

//      Play sound, make balloon go away
        sound.playSound(balloon);
        content.removeView(balloon);
        balloonImage.remove(balloon);
        balloonImagePopped++;

//      If balloon pop was caused by user, it's a point; otherwise,
//      a balloon hit the top of the screen and it's a life lost
        if (userTouch) {
            score ++;
        } else {
            usedPins++;
            if (usedPins <= pinImage.size()) {
                pinImage.get(usedPins - 1)
                        .setImageResource(R.drawable.pin_off);
            }
            if (usedPins == AmountOfPins) {
                gameOver(true);
                return;
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.missed_that_one, Toast.LENGTH_SHORT).show();
            }
        }
        updateDisplay();
        if (balloonImagePopped == BalloonLevel) {
            finishLevel();
        }
    }

    private void gameOver(boolean allPinsUsed) {

        Toast.makeText(MainActivity.this, R.string.game_over, Toast.LENGTH_LONG).show();
        sound.stopMusic();

//      Clean up balloons
        for (Balloon balloon : balloonImage) {
            balloon.setPopped(true);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (Balloon balloon : balloonImage) {
                    content.removeView(balloon);
                }
                balloonImage.clear();
            }
        }, 2000);

//      Reset for a new game
        play = false;
        usedPins = 0;
        go.setText(R.string.play_game);
        Next = start;

        if (allPinsUsed) {

//          Manage high score locally
            if (PreferencesHelper.isTopScore(this, score )) {
                @SuppressLint("StringFormatMatches") String message = String.format(getString(R.string.your_top_score_is), score );
                PreferencesHelper.setTopScore(this, score );
                MyAlertDialog dialog = MyAlertDialog.newInstance(
                        getString(R.string.new_top_score),
                        message);
                dialog.show(getSupportFragmentManager(), null);
            }

            int completedLevel = level - 1;
            if (PreferencesHelper.isMostLevels(this, completedLevel)) {
                PreferencesHelper.setPrefMostLevels(this, completedLevel);
                @SuppressLint("StringFormatMatches") MyAlertDialog dialog = MyAlertDialog.newInstance(
                        getString(R.string.more_levels_than_ever),
                        String.format(getString(R.string.you_completed_n_levels), completedLevel));
                dialog.show(getSupportFragmentManager(), null);
            }

        }

    }

    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length != 1) {
                throw new AssertionError(
                        "Expected 1 param for current level");
            }

            int level = params[0];

//          level 1 = max delay; each ensuing level reduces delay by 500 ms
//            min delay is 250 ms
            int maxDelay = Math.max(MinimumDelayinAnimation, (maximumDelayinAnimation - ((level - 1) * 500)));
            int minDelay = maxDelay / 2;

//          Keep on launching balloons until either
//              1) we run out or 2) the play flag is set to false
            int balloonsLaunched = 0;
            while (play && balloonsLaunched < BalloonLevel) {

//              Get a random horizontal position for the next balloon
                Random random = new Random(new Date().getTime());
                int xPosition = random.nextInt( ScrnWdth - 200);
                publishProgress(xPosition);
                balloonsLaunched++;

//              Wait a random number of milliseconds before looping
                int delay = random.nextInt(minDelay) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//          This runs on the UI thread, so we can launch a balloon
//            at the randomized horizontal position
            int xPosition = values[0];
            launchBalloon(xPosition);
        }

    }
}

