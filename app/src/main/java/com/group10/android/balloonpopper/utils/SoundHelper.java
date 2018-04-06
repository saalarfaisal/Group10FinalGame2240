// TODO: 06/04/18  Final Project for COIS 2240 SOFTWARE DESIGN AND MODELLING GROUP 10
//todo: Group 10 - Nikhil Pai Ganesh - 0595517 ; Saalar Faisal - ; Gokhan Karasu - 0631945; Isaiah Mutekanga- // 
package com.group10.android.balloonpopper.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.View;

import com.group10.android.balloonpopper.R;

public class SoundHelper {

    private final static String TAG = "SoundHelper";

    private MediaPlayer musicPlayer;
    private SoundPool soundPool;
    private int soundID;
    private boolean mLoaded;
    private float mVolume;

    public SoundHelper(Activity activity) {

        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actVolume / maxVolume;

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

//      Build the SoundPool object
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(6).build();
        } else {
            //noinspection deprecation
            soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded = true;
            }
        });
        soundID = soundPool.load(activity, R.raw.balloon_pop, 1);
    }

    public void playSound(View v) {

        // Is the sound loaded does it already play?
        if (mLoaded) {
            soundPool.play( soundID, mVolume, mVolume, 1, 0, 1f);
        }
    }

    public void prepareMusicPlayer(Context context) {
        musicPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background_music);
        musicPlayer.setVolume(.5f, .5f);
        musicPlayer.setLooping(true);
    }

    public void playMusic() {
        if (musicPlayer != null) {
            musicPlayer.start();
        }
    }

    public void stopMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }

}
