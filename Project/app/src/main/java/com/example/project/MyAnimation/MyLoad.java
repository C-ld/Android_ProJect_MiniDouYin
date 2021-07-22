package com.example.project.MyAnimation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.sql.Time;
import java.util.Calendar;

public class MyLoad extends View {

    private Paint cirPaint = new Paint();
    private Paint shadowPaint = new Paint();
    private Paint starPaint = new Paint();

    private Calendar calendar = Calendar.getInstance();

    private float centerX;
    private float centerY;
    private float radius;
    private float starRadius;
    private boolean fly = false;
    private float wid = 0;
    private float starAng = 0;
    private Time sTime;
    private String TAG = "TAG";
    private float offset;
    private float starOffset = 0;

    private ValueAnimator vanim2;

    private float startX, startY, endX, endY;

    private ValueAnimator vanim;

    private boolean fstdraw = true;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    invalidate();
                    break;
            }
        }
    };

    public MyLoad(Context context){
        this(context, null);
    }

    public MyLoad(Context context, @Nullable AttributeSet attrs){
        this(context, attrs, 0);
    }

    public MyLoad(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context,  attrs, defStyleAttr);
        init();
    }
    private void init() {
        cirPaint.setColor(Color.YELLOW);
        cirPaint.setStrokeWidth(40);

        shadowPaint.setColor(Color.BLACK);
        cirPaint.setStrokeWidth(40);

        starPaint.setColor(Color.YELLOW);
        starPaint.setStrokeWidth(40);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        configWhenLayoutChanged();
    }

    private void configWhenLayoutChanged() {
        float newRadius = Math.min(getWidth(), getHeight()) / 4f;
        if (newRadius == radius) {
            return;
        }
        radius = newRadius;
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;

        startX = centerX - (float)(2*radius/Math.sqrt(2));
        startY = centerY - (float)(2*radius/Math.sqrt(2));
        endX = centerX - (float)(radius/Math.sqrt(2));
        endX = centerY - (float)(radius/Math.sqrt(2));
        starRadius = radius + 100;

        vanim = ValueAnimator.ofFloat(0,(float) (radius/Math.sqrt(2))+100);
        vanim.setDuration(2000);
        vanim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offset = (float)vanim.getAnimatedValue();
            }

        });
        vanim.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                fly = true;
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        vanim2 = ValueAnimator.ofFloat(0,2*(float)Math.PI);
        vanim2.setDuration(2000);
        vanim2.setInterpolator(new LinearInterpolator());
        vanim2.setRepeatCount(1);
        vanim2.setRepeatMode(ValueAnimator.RESTART);
        vanim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                starAng = (float)vanim2.getAnimatedValue();
                if(fly==true){
                    starRadius*=1.05;
                }
            }
        });
//        for (int degree = 0; degree < FULL_CIRCLE_DEGREE; degree += UNIT_DEGREE) {
//            double radians = Math.toRadians(degree);
//            float startX = (float) (centerX + (radius * (1 - 0.05f)) * Math.cos(radians));
//            float startY = (float) (centerX + (radius * (1 - 0.05f)) * Math.sin(radians));
//            float stopX = (float) (centerX + radius * Math.cos(radians));
//            float stopY = (float) (centerY + radius * Math.sin(radians));
//            unitLinePositions.add(new RectF(startX, startY, stopX, stopY));
//        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if(fstdraw==true){
            Log.d(TAG, "onDraw: ");
            vanim.setStartDelay(170);
            vanim.start();
            vanim2.setStartDelay(170);
            vanim2.start();
            fstdraw = false;
        }

        cirPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, cirPaint);

        shadowPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(startX+offset,startY+offset,radius,shadowPaint);

        starPaint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(centerX+starRadius*(float) Math.cos(starAng),centerY+starRadius*(float) Math.sin(starAng),30,starPaint);
        canvas.drawCircle(centerX+starRadius*(float) Math.cos(starAng+Math.PI*2/3),centerY+starRadius*(float) Math.sin(starAng+Math.PI*2/3),30,starPaint);
        canvas.drawCircle(centerX+starRadius*(float) Math.cos(starAng+Math.PI*4/3),centerY+starRadius*(float) Math.sin(starAng+Math.PI*4/3),30,starPaint);

        handler.sendEmptyMessage(1);
    }


    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

}
