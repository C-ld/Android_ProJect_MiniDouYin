package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;


import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.http.Url;





public class PlayerActivity extends AppCompatActivity {

    private String mp4Path = "";

    static int pos = -1;

    private String TAG = "TAG";
    File file;
    String videoUrl;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    SurfaceView surfaceView;
//    Button startBtn,pauseBtn,stopBtn;
    MediaPlayer mediaPlayer;
    int position;

    long mLastTime=0;
    long mCurTime=0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //Toast.makeText(PlayerActivity.this,"这是单击事件",Toast.LENGTH_LONG).show();
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                    else{
                        mediaPlayer.start();
                    }
                    break;
                case 2:
//                    Toast.makeText(PlayerActivity.this,"这是双击事件",Toast.LENGTH_LONG).show();
                    //Love!
                    View love = findViewById(R.id.like);
                    love.setAlpha(1);
                    love.animate().alpha(0);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        videoUrl = getIntent().getStringExtra("videoUrl");
        fileSet();

        if(pos == -1){
            pos = getIntent().getIntExtra("pos",0);
            download();

        }
        else {
            int curpos = getIntent().getIntExtra("pos",0);
            if(pos == curpos){
                Log.d(TAG, "onCreate: Same file");
                videoUrl = mp4Path;
            }
            else{
                pos = curpos;
                download();
            }
        }

        verifyStoragePermissions(this);

        mediaPlayer = new MediaPlayer();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        //  设置播放时打开屏幕
        surfaceView.getHolder().setKeepScreenOn(true);
        //  为SurfaceView添加监听
        surfaceView.getHolder().addCallback(new SurfaceListener());
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                else{
                    mediaPlayer.start();
                }
            }
        });

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastTime=mCurTime;
                mCurTime= System.currentTimeMillis();
                if(mCurTime-mLastTime<300){//双击事件
                    mCurTime =0;
                    mLastTime = 0;
                    handler.removeMessages(1);
                    handler.sendEmptyMessage(2);
                }else{//单击事件
                    handler.sendEmptyMessageDelayed(1, 310);
                }
            }
        });

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.d(TAG, "onCompletion: is end");
//                Toast.makeText(PlayerActivity.this,"recycle",Toast.LENGTH_SHORT).show();
//                mediaPlayer.pause();
//            }
//        });

    }


    private void download(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "fileSet: "+mp4Path);
                try {
                    URL url = new URL(videoUrl);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(
                            url.openStream(), 8192);// 1024*8

                    // Output stream
                    OutputStream output = new FileOutputStream(mp4Path);
                    int count;
                    byte data[] = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        // writing data to file
                        output.write(data, 0, count);
                    }
                    // flushing output
                    output.flush();
                    // closing streams
                    output.close();
                    input.close();
                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                } finally {
                    Log.d(TAG, "run: dl finish");
                    Looper.prepare();
                    Toast.makeText(PlayerActivity.this,"Download already!",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    videoUrl = mp4Path;
                }
            }
        }).start();
    }

    private void play(){
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        try {
            //  MediaPlayer设播放的资源
            mediaPlayer.setDataSource(videoUrl);
            //  将视频画面输出到SurfaceView
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.prepare();
            //  以下两句是 To access the DisplayMetrics members
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            //  设置视频保持纵横比缩放到占满屏幕
            surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(metrics.widthPixels
                    ,mediaPlayer.getVideoHeight() * metrics.widthPixels
                    /mediaPlayer.getVideoWidth()));
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class SurfaceListener implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(position >= 0){
                play();
                //  从指定位置播放
                mediaPlayer.seekTo(position);
                position = 0;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
    //当跳到其他页面时,保存当前的播放位置
    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer.isPlaying()){
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        //  释放MediaPlayer所占资源
        mediaPlayer.release();
    }
    private void fileSet() {
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        try {
            File fileStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            file = new File(fileStorageDir, "temp.mp4");
            if(!file.exists()){
                file.createNewFile();
            }
            mp4Path = file.getAbsolutePath();
        }catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getOutputMediaPath: "+file.toString());
    }


}
