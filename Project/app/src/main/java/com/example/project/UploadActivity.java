package com.example.project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.project.model.UploadResponse;
import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.http.params.HttpConnectionParams;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.project.Constants.BASE_URL;

public class UploadActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024;
    private static final int REQUEST_CODE_COVER_IMAGE = 101;
    private static final String COVER_IMAGE_TYPE = "image/*";

    private static final int REQUEST_CODE_VIDEO = 102;
    private static final String VIDEO_TYPE = "video/*";
    private IApi api;
    private Uri coverImageUri;
    private Uri videoUri;
    private TextView videoPath;
    private TextView imagPath;
    private VideoView videopreview;
    private ImageView imagepreview;
    private Calendar calendar = Calendar.getInstance();

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initNetwork();
        setContentView(R.layout.activity_upload);
        videoPath = findViewById(R.id.videoPath);
        imagPath = findViewById(R.id.imagePath);
        videopreview = findViewById(R.id.videoPre);
        imagepreview = findViewById(R.id.imgPre);
        Time curtime = getCurrentTime();
        //夜间模式
        if(curtime.getHours()>18 || curtime.getHours() < 6){
            findViewById(R.id.uploadPage).setBackgroundColor(Color.BLACK);
            imagPath.setTextColor(Color.WHITE);
            videoPath.setTextColor(Color.WHITE);
        }
        findViewById(R.id.btn_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile(REQUEST_CODE_VIDEO, VIDEO_TYPE, "选择视频");
            }
        });

        findViewById(R.id.btn_cover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile(REQUEST_CODE_COVER_IMAGE, COVER_IMAGE_TYPE, "选择封面");
            }
        });

        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_CODE_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                videoUri = data.getData();
                Log.d(TAG, "onActivityResult: "+videoUri.toString());
                //coverSD.setImageURI(videoUri);
                videopreview.setVideoURI(videoUri);
                videopreview.start();
                videopreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(videopreview.isPlaying()){
                            videopreview.pause();
                        }
                        else{
                            videopreview.start();
                        }
                    }
                });
                videoPath.setText(videoUri.toString());

                if (videoUri != null) {
                    Log.d(TAG, "pick cover image " + videoUri.toString());
                } else {
                    Log.d(TAG, "uri2File fail " + data.getData());
                }

            } else {
                Log.d(TAG, "file pick fail");
            }
                break;
            case REQUEST_CODE_COVER_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    coverImageUri = data.getData();
                    Log.d(TAG, "onActivityResult: "+coverImageUri.toString());
                    imagPath.setText(coverImageUri.toString());
                    imagepreview.setImageURI(coverImageUri);
                    //coverSD.setImageURI(videoUri);
//                    videoPath.setText(coverImageUri.toString());
                    if (imagPath != null) {
                        Log.d(TAG, "pick cover image " + imagPath.toString());
                    } else {
                        Log.d(TAG, "uri2File fail " + data.getData());
                    }

                } else {
                    Log.d(TAG, "file pick fail");
                }
                break;
        }
    }

    private void initNetwork() {
        //TODO 3
        // 创建Retrofit实例
        // 生成api对象
        api = retrofit.create(IApi.class);
    }

    private void getFile(int requestCode, String type, String title) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    private void submit() {
        byte[] videoData = readDataFromUri(videoUri);
        if (videoData == null || videoData.length == 0) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] coverImageData = readDataFromUri(coverImageUri);
        if (coverImageData == null || coverImageData.length == 0) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        if ( coverImageData.length + videoData.length >= MAX_FILE_SIZE) {
            Toast.makeText(this, "文件过大", Toast.LENGTH_SHORT).show();
            return;
        }
        //TODO 5
        // 使用api.submitMessage()方法提交留言
        // 如果提交成功则关闭activity，否则弹出toast
//        MultipartBody.Part fromp = MultipartBody.Part.createFormData("from",Constants.USER_NAME);
//        MultipartBody.Part top = MultipartBody.Part.createFormData("to",to);
//        MultipartBody.Part contentp = MultipartBody.Part.createFormData("content",content);

//        MultipartBody.Part imagp = MultipartBody.Part.createFormData(
//                "image",
//                "cover.png",
//                RequestBody.create(MediaType.parse("multipart/form-data"), coverImageData));

        MultipartBody.Part coverImage = MultipartBody.Part.createFormData(
                "image",
                "cover.png",
                RequestBody.create(MediaType.parse("multipart/form-data"), coverImageData));
        MultipartBody.Part video = MultipartBody.Part.createFormData(
                "video",
                "content.mp4",
                RequestBody.create(MediaType.parse("multipart/form-data"), videoData));

        Call <UploadResponse> repos = api.submitMessage(Constants.STUDENT_ID,Constants.USER_NAME,"",coverImage, video , Constants.token);
        repos.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if(!response.isSuccessful()){
                    Log.d(TAG,"onResponse: " + "提交失败");
                    Toast.makeText(UploadActivity.this,"提交失败",Toast.LENGTH_SHORT ).show();
                }
                else{
                    Log.d(TAG, "onResponse: " + "提交成功");
                    Toast.makeText(UploadActivity.this,"提交成功",Toast.LENGTH_SHORT ).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Log.d(TAG,"onFailure:" + "提交失败");
                t.printStackTrace();
            }
        });
    }

    private byte[] readDataFromUri(Uri uri) {
        byte[] data = null;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            data = Util.inputStream2bytes(is);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

}
