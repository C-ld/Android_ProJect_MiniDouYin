package com.example.project;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback  {
    private final SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //surface第一次创建时回调
        //打开相机
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //surface变化的时候回调(格式/大小)
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //surface销毁的时候回调
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
