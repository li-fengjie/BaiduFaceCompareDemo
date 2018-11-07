package com.example.lifen.baidufacecomparedemo.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifen.baidufacecomparedemo.R;
import com.example.lifen.baidufacecomparedemo.utils.AuthService;
import com.example.lifen.baidufacecomparedemo.utils.FaceMatch;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * 人脸对比 1：1
 *
 * @author LiFen
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE1 = 11;
    private static final int REQUEST_CODE2 = 12;
    ImageView mImageView1;
    ImageView mImageView2;
    Button mCompareBtn;
    TextView mResultText;
    private byte[] mImg1;
    private byte[] mImg2;
    String key = "";//api_key
    String secret ="";//api_secret
    private final static int i = 100;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == i){
                mResultText.setText((String)msg.obj);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView1 = (ImageView) findViewById(R.id.img1);
        mImageView2 = (ImageView) findViewById(R.id.img2);
        mCompareBtn = (Button) findViewById(R.id.compareBtn);
        mResultText = (TextView) findViewById(R.id.resultBtn);
        if(TextUtils.isEmpty(key) || TextUtils.isEmpty(secret)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("please enter key and secret");
            builder.setTitle("");
            builder.show();
            return;
        }
        mImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlbumActivity(REQUEST_CODE1);
            }
        });
        mImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlbumActivity(REQUEST_CODE2);
            }
        });
        mCompareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCompare();
            }
        });
    }

    private void startCompare() {
        if ("".equals(mImg1) || mImg1 == null || "".equals(mImg2) || mImg2 == null) {
            Toast.makeText(this, "请选择图片再比对", Toast.LENGTH_SHORT).show();
            return;
        }
        mResultText.setText("比对中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String accessToken = AuthService.getAuth(key,secret);
                    Log.i(TAG, "run: " +accessToken);
                    Log.i(TAG, "run: " + mImg1.toString());
                    Log.i(TAG, "run: " + mImg2.toString());
                    String result = FaceMatch.match(mImg1,mImg2,accessToken);
                    Message msg = new Message();
                    msg.what = i;
                    msg.obj = result;
                    handler.sendMessage(msg);
                }catch (Exception e){
                    Log.i(TAG, "startCompare: " + e.toString());
                }
            }
        }).start();
    }

    private void startAlbumActivity(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        Uri uri = data.getData();
        Log.e("uri", uri.toString());
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                /* 将Bitmap设定到ImageView */
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE1) {
            mImageView1.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datas = baos.toByteArray();
            mImg1 = datas;
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE2) {
            mImageView2.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datas = baos.toByteArray();
            mImg2 = datas;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
