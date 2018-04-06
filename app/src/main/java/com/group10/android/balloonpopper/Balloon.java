// TODO: 06/04/18 :Final Project for COIS 2240 SOFTWARE DESIGN AND MODELLING GROUP 10
//todo: Group 10 - Nikhil Pai Ganesh - 0595517 ; Saalar Faisal - 0580714 ; Gokhan Karasu - 0631945; Isaiah Mutekanga- // 

package com.group10.android.balloonpopper;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.group10.android.balloonpopper.utils.PixelHelper;

// method  to implement inner class  and sets value when object is touched.
public class Balloon extends android.support.v7.widget.AppCompatImageView
        implements View.OnTouchListener,
        Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {

    public static final String TAG = "Balloon";



    private BalloonListener Listen;
    private ValueAnimator animation;
    private boolean pooped;

    public Balloon(Context context) {
        super(context);
    }

    // setting basic parameter
    public Balloon(Context context, int color, int rawHeight, int level) {
        super(context);

        this.Listen = (BalloonListener) context;

        this.setImageResource(com.group10.android.balloonpopper.R.drawable.balloon1);


        this.setColorFilter(color);

        int rawWidth = rawHeight / 2;

      // Calc balloon height and width as dp
        int dpHeight = PixelHelper.pixelsToDp(rawHeight, context);
        int dpWidth = PixelHelper.pixelsToDp(rawWidth, context);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(dpWidth, dpHeight);
        setLayoutParams(params);

        setOnTouchListener(this);
    }
    // value setting on balloon
    public void releaseBalloon(int screenHeight, int duration) {
        animation = new ValueAnimator();
        animation.setDuration(duration);
        animation.setFloatValues(screenHeight, 0f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setTarget(this);
        animation.addListener(this);
        animation.addUpdateListener(this);
        animation.start();
    }
    // when not popped
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (!pooped) {
            setY((Float) animation.getAnimatedValue());
        }
    }

    public interface BalloonListener {
        void popBalloon(Balloon balloon, boolean touched);
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
//      This means the balloon got to the top of the screen
        if (!pooped) {
            Listen.popBalloon(this, false);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//      Call the activity's popBalloon() method
//      Cancel the animation so the ValueAnimator doesn't keep going
//      Flip the popped flag
        if (!pooped && event.getAction() == MotionEvent.ACTION_DOWN) {
            Listen.popBalloon(this, true);
            pooped = true;
            animation.cancel();
        }
        return true;
    }

    public void setPopped(boolean popped) {
        this.pooped = popped;
    }

}
