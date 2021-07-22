package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.project.MyAnimation.MyLoad;
import com.example.project.model.Message;
import com.example.project.model.MessageListResponse;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private RecyclerView recyclerView;
    private boolean loading = true;
    private FeedAdapter adapter = new FeedAdapter(MainActivity.this);
    private final static int PERMISSION_REQUEST_CODE = 1001;

    private Calendar calendar = Calendar.getInstance();

    private static final int REQUEST_CODE_ADD = 1002;

    protected final Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case 1:
                    View v1 = findViewById(R.id.loadday);
                    //夜间模式
                    if(getCurrentTime().getHours()>18 || getCurrentTime().getHours() < 6){
                        v1 = findViewById(R.id.load);
                    }
                    RelativeLayout v2 = findViewById(R.id.MainPage);
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(v1,"alpha",1,0);
                    anim1.setDuration(1000);
                    anim1.setInterpolator(new AccelerateInterpolator());
                    anim1.start();
                    ObjectAnimator anim2 = ObjectAnimator.ofFloat(v2,"alpha",0,1);
                    anim2.setDuration(1000);
                    anim2.setInterpolator(new AccelerateInterpolator());
                    anim2.start();
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(anim1,anim2);
                    animatorSet.start();
                    v1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            animatorSet.end();
                        }
                    });
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            recyclerView = findViewById(R.id.rv_list);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) {}
                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });
                    break;
            }
        }
    };

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev){
//        return loading;//consume
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        Time curtime = getCurrentTime();
        //夜间模式
        View v = findViewById(R.id.loadday);
        if(curtime.getHours()>18 || curtime.getHours() < 6){
            findViewById(R.id.Main).setBackgroundColor(Color.BLACK);
            v.setAlpha(0);
            v = findViewById(R.id.load);
            v.setAlpha(1);
        }

//        recyclerView = findViewById(R.id.rv_list);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(adapter);

        handler.sendMessageDelayed(android.os.Message.obtain(handler,1),2000);

        getData(null);

        Button rec = findViewById(R.id.record);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customCamera(v);
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
        Button upload = findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,UploadActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_mine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(Constants.STUDENT_ID);
            }
        });

        findViewById(R.id.btn_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(null);
            }
        });

        adapter.setOnItemClickListener(new FeedAdapter.OnItemClickListener(){
            @Override
            public void onClick(int position) {
                //Toast.makeText(MainActivity.this, "click " + position, Toast.LENGTH_SHORT).show();
//                ActivityCompat.requestPermissions(MainActivity.this, new
//                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Toast.makeText(MainActivity.this,"Loading...",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                Message message = adapter.getItem(position);
//                intent.putExtra("username",message.getUsername());
                intent.putExtra("videoUrl",message.getvideoUrl());
//                intent.putExtra("creatAt", message.getCreatedAt());
//                intent.putExtra("updateAt",message.getUpdatedAt());
                intent.putExtra("pos", position);
//                intent.putExtra("listUrl",(Serializable)adapter.getData());
                startActivity(intent);
            }
        });
    }

    public void customCamera(View view) {
        requestPermission();
    }

    private void recordVideo() {
        CameraActivity.startUI(this);
    }

    private void requestPermission() {
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (hasCameraPermission && hasAudioPermission) {
            recordVideo();
        } else {
            List<String> permission = new ArrayList<String>();
            if (!hasCameraPermission) {
                permission.add(Manifest.permission.CAMERA);
            }
            if (!hasAudioPermission) {
                permission.add(Manifest.permission.RECORD_AUDIO);
            }
            ActivityCompat.requestPermissions(this, permission.toArray(new String[permission.size()]), PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermission = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        if (hasPermission) {
            recordVideo();
        } else {
            Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData(String studentId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessageListResponse message = baseGetReposFromRemote(studentId, Constants.token);
                Log.d(TAG, "run: "+message.success);
                if(message.feeds != null && !message.feeds.isEmpty()){
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setData(message.feeds);
                        }
                    });
                }
            }
        }).start();
    }

    public MessageListResponse baseGetReposFromRemote(String studentID, String token)
    {
        String urlStr;
        if(studentID != null){
            urlStr = String.format(Constants.BASE_URL + "video?student_id=%s",studentID);
        }
        else{
            urlStr = String.format(Constants.BASE_URL + "video");
        }
        MessageListResponse ret = null;
        try{
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);

            conn.setRequestMethod("GET");
            conn.setRequestProperty("token", token);
            Log.d("Linking","-------");

            if(conn.getResponseCode() == 200){
                Log.d("Linking","Successful!");
                InputStream in = conn.getInputStream();
                BufferedReader reader = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                }

                ret = new Gson().fromJson(reader, new TypeToken<MessageListResponse>(){
                }.getType());

                reader.close();
                in.close();
            }else{
                Log.d("Linking","Failed!");
                //错误处理
            }
            conn.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
        }
        return ret;
    }

    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }
}
