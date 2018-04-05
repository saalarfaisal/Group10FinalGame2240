package com.group10.android.balloonpopper;

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

public class MainActivity extends AppCompatActivity
        implements Balloon.BalloonListener {

    private static final String TAG = "MainActivity";

    private static final int BalloonLevel = 12;
    private static final int AmountOfPins = 3;

    private static final int MinimumDelayinAnimation = 500;
    private static final int maximumDelayinAnimation = 1500;
    private static final int minimumTimeAnimation = 1000;
    private static final int maximumTimeAnimation = 8000;
    private static final String NextLevel = "NextLevel";
    private static final String start = "NextLevelStart_game";

    private ViewGroup content;
    private SoundHelper sound;
    private List<ImageView> pinImage = new ArrayList<>();
    private List<Balloon> balloonImage = new ArrayList<>();
    private TextView Scores, Level;
    private Button go;
    private String Next = start;
    private boolean play;
    private int[] mBalloonColors = new int[10];
    private int mNextColor, balloonImagePopped,
            ScrnWdth, ScrnHght,
            usedPins = 0,
            mScore = 0, mLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

//      Load the activity layout, which is an empty canvas
        setContentView(R.layout.activity_main);

//      Get background reference.
        content = (ViewGroup) findViewById(R.id.content_view);
        if (content == null) throw new AssertionError();
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setToFullScreen();
                }
                return false;
            }
        });
        setToFullScreen();

//      After the layout is complete, get screen dimensions from the layout.
        ViewTreeObserver viewTreeObserver = content.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ScrnWdth = content.getWidth();
                    ScrnHght = content.getHeight();
                }
            });
        }

//      Initialize sound helper class that wraps SoundPool for audio effects
        sound = new SoundHelper(this);
        sound.prepareMusicPlayer(this);

//      Initialize display elements
        pinImage.add((ImageView) findViewById(R.id.pushpin1));
        pinImage.add((ImageView) findViewById(R.id.pushpin2));
        pinImage.add((ImageView) findViewById(R.id.pushpin3));
        Scores = (TextView) findViewById(R.id.score_display);
        Level = (TextView) findViewById(R.id.level_display);

//      Display current level and score
        updateDisplay();



//      Get button references
        go = (Button) findViewById(R.id.go_button);

//      Handle button click
        if (go == null) throw new AssertionError();
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (play) {
                    stopGame();
                } else {
                    switch (Next) {
                        case start:
                            startGame();
                            break;
                        case NextLevel:
                            start();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        stopGame();
        super.onBackPressed();
    }

    private void setToFullScreen() {

        //      Set full screen mode
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void startGame() {

        setToFullScreen();

//      Reset score and level
        mScore = 0;
        mLevel = 1;

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
        mLauncher.execute(mLevel);

    }

    @SuppressLint("StringFormatMatches")
    private void finishLevel() {
        PreferencesHelper.setCurrentScore(this, mScore);
        PreferencesHelper.setCurrentLevel(this, mLevel);
        Toast.makeText(MainActivity.this,
                String.format(getString(R.string.you_finished_level_n), mLevel),
                Toast.LENGTH_LONG).show();

        play = false;
        mLevel++;
        go.setText(String.format("Start level %s", mLevel));
        Next = NextLevel;
    }

    private void updateDisplay() {
        Scores.setText(String.valueOf(mScore));
        Level.setText(String.valueOf(mLevel));
    }

    private void launchBalloon(int x) {

//      Balloon is launched from activity upon progress update from the AsyncTask
//      Create new imageview and set its tint color
        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150, mLevel);
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
        int duration = Math.max(minimumTimeAnimation, maximumTimeAnimation - (mLevel * 1000));
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
            mScore++;
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
            if (PreferencesHelper.isTopScore(this, mScore)) {
                @SuppressLint("StringFormatMatches") String message = String.format(getString(R.string.your_top_score_is), mScore);
                PreferencesHelper.setTopScore(this, mScore);
                MyAlertDialog dialog = MyAlertDialog.newInstance(
                        getString(R.string.new_top_score),
                        message);
                dialog.show(getSupportFragmentManager(), null);
            }

            int completedLevel = mLevel - 1;
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

