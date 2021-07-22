package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private String TAG = "TAG";

    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mHolder;
    private ImageView mImageView;
    private VideoView mVideoView;
    private Button mRecordButton;
    private TextView mLen;
    private int len = 0;
    private Camera.Parameters mParameters;
    private boolean isRecording = false;

    private Calendar calendar = Calendar.getInstance();
    private Time stt;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    private String mp4Path = "";

    private ValueAnimator valueAnimator;

    public static void startUI(Context context) {
        Intent intent = new Intent(context, CameraActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = findViewById(R.id.surfaceview);
        mImageView = findViewById(R.id.iv_img);
        mVideoView = findViewById(R.id.videoview);
        mRecordButton = findViewById(R.id.bt_record);
        mLen = findViewById(R.id.lenth);

        verifyStoragePermissions(this);

        Button rec = findViewById(R.id.bt_record);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueAnimator.end();
            }
        });

        valueAnimator = ValueAnimator.ofFloat(0,10);
        valueAnimator.setDuration(10000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(stt!=null){
                    mLen.setAlpha(1);

                    Time tmp = getCurrentTime();
                    len = (tmp.getMinutes()-stt.getMinutes()+(tmp.getHours() > stt.getHours() ? 60 : 0))
                            *60+tmp.getSeconds()-stt.getSeconds();
                    if(len>10){
                        len = 10;
                    }
                    mLen.setText(String.format("%02d s",len));
                }
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                mLen.setAlpha(0);
                mLen.setText("");
                record(mSurfaceView);
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        mHolder = mSurfaceView.getHolder();
        initCamera();
        mHolder.addCallback(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            initCamera();
        }
        mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }


    private void initCamera() {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.set("orientation", "portrait");
        parameters.set("rotation", 90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // todo 3.1 设置 camera 和 holder 建立关联
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        //停止预览效果
        mCamera.stopPreview();
        //重新设置预览效果
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // todo 3.2 释放相机
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void takePhoto(View view) {
        mCamera.takePicture(null, null, mPictureCallback);
    }

    //获取照片中的接口回调
    Camera.PictureCallback mPictureCallback  = new Camera.PictureCallback()

    {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // todo 3.3 显示拍照所得图片
                FileOutputStream fos = null;
                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
                        File.separator + "1.jpg";
                File file = new File(filePath);
                try {
                    fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.flush();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    Bitmap rotateBitmap = PathUtils.rotateImage(bitmap, filePath);
                    mImageView.setVisibility(View.VISIBLE);
                    mVideoView.setVisibility(View.GONE);
                    mImageView.setImageBitmap(rotateBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mCamera.startPreview();
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    };

    public void record(View view) {
        if (!isRecording) {
            // todo 3.4 开始录制
            if(prepareVideoRecorder()){
                stt = getCurrentTime();
                valueAnimator.start();

                mRecordButton.setText("停止");
                mMediaRecorder.start();
            }
            else{
                releaseMediaRecorder();
            }
        } else {
            // 停止录制
            mRecordButton.setText("录制");
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            if(valueAnimator.isRunning()){
                valueAnimator.end();
            }
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();

            AlertDialog alertDialog = new AlertDialog.Builder(CameraActivity.this)
                    .setTitle("提示")
                    .setMessage("是否上传刚刚录制的视频")
                    .setNegativeButton("暂不考虑", new DialogInterface.OnClickListener() {//添加取消
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Toast.makeText(ManeuWebActivity.this, "这是取消按钮", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("立即上传", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(CameraActivity.this,UploadActivity.class);
//                            intent.putExtra("fromCamera",1);
//                            intent.putExtra("videoPath",mp4Path);
                            startActivity(intent);
                        }
                    })
                    .create();
            alertDialog.setCancelable(false);//点击空白处之后弹出框不会消失
            alertDialog.show();

            // todo 3.5 播放录制的视频
            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mVideoView.setVideoPath(mp4Path);
            mVideoView.start();
        }
        isRecording = !isRecording;
    }

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mp4Path = getOutputMediaPath();
        mMediaRecorder.setOutputFile(mp4Path);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mMediaRecorder.setOrientationHint(90);

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        // todo
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }

    }

    private String getOutputMediaPath() {
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir, "IMG_" + timeStamp + ".mp4");

        Log.d(TAG, "getOutputMediaPath: "+mediaFile.toString());
        if (!mediaFile.exists()) {
            mediaFile.getParentFile().mkdirs();
        }
        return mediaFile.getAbsolutePath();
    }

    // 获取当前的时间：时、分、秒
    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }
}
