package com.example.project.MyAnimation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.sql.Time;
import java.util.Calendar;

public class Love extends View {

    private Calendar calendar = Calendar.getInstance();

    private String TAG = "TAG";
    private Paint paint = new Paint();
    private Path path = new Path(); // 初始化 Path 对象

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

    public Love(Context context){
        this(context, null);
    }

    public Love(Context context, @Nullable AttributeSet attrs){
        this(context, attrs, 0);
    }

    public Love(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context,  attrs, defStyleAttr);
        init();
    }
    private void init() {

        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);


        path.addArc(450, 800, 550, 900, -225, 225);
        path.arcTo(550, 800, 650, 900, -180, 225, false);
        path.lineTo(550, 971);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }


    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

}
