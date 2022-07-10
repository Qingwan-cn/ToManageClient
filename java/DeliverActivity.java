package com.guoxingyuan.closeseewo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class DeliverActivity extends Activity {

    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private TextView mText4;
    private Button mBtn1;
    private Button mBtn2;
    private ImageView mImage;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    private String path;

    private String ip;
    private int port;

    public void setIpAndPort() {
        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        String portStr = intent.getStringExtra("port");
        int port = Integer.parseInt(portStr);
        this.ip = ip;
        this.port = port;
    }

    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 设置沉浸式
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        int uiVisibility = window.getDecorView().getSystemUiVisibility();
        uiVisibility |= flags;
        window.getDecorView().setSystemUiVisibility(uiVisibility);
        WindowManager.LayoutParams params = window.getAttributes();
        /*
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT 全屏模式，内容下移，非全屏不受影响
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES 允许内容区域延伸到刘海区
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER 不允许内容延伸进刘海区
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(params);
        //生成界面
        setContentView(R.layout.activity_deliver);
        setIpAndPort();
        //字体配置
        typeface = Typeface.createFromAsset(getAssets(), "fonts/pingfang.ttf");
        //控件注册
        mText1 = findViewById(R.id.deliver_title);
        mText2 = findViewById(R.id.deliver_text_1);
        mText3 = findViewById(R.id.deliver_text_2);
        mText4 = findViewById(R.id.deliver_text_3);
        mBtn1 = findViewById(R.id.deliver_btn_1);
        mBtn2 = findViewById(R.id.deliver_btn_2);
        mImage = findViewById(R.id.deliver_image);

        mText1.setTypeface(typeface);
        mText2.setTypeface(typeface);
        mText3.setTypeface(typeface);
        mText4.setTypeface(typeface);
        mBtn1.setTypeface(typeface);
        mBtn2.setTypeface(typeface);
        AlertUtils.applyForPermission(this);
        connect();
        setListener();

    }

    public Socket getSocket() {
        return socket;
    }

    private Socket socket;

    private void connect() {
        mText4.setText("正在连接...");
        Thread thread = new Thread(() -> {
            try {
                socket = new Socket(ip, 30000);
                boolean flag = socket.isConnected();
                if (flag) {
                    mText4.post(() -> mText4.setText("连接成功"));
                }
            } catch (IOException e) {
                mText4.post(() -> mText4.setText("连接失败，点击重试。"));
                String message = "连接失败，可能有以下原因:\r\n 1.IP地址及端口号输入不正确。\r\n 2.手机和PC未连接至同一无线局域网下。\r\n3.PC端未运行相应程序。\r\n若当前没有可用的无线局域网连接，请开启手机热点后使用PC连接手机热点。\r\n可通过PC及手机的网络设置详情界面查看IP地址。";
                Looper.prepare();
                new AlertDialog.Builder(DeliverActivity.this)
                        .setTitle("连接异常")
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
                Toast.makeText(DeliverActivity.this, "无法与指定的PC建立Socket连接。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        });
        thread.start();
    }

    private final int REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "这需要你的权限许可，否则无法使用。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //content类型的uri获取图片路径的方法
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //根据路径展示图片的方法
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mImage.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "照片展示失败。", Toast.LENGTH_SHORT).show();
        }
    }

    //安卓版本大于4.4的处理方法
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handImage(Intent data) {
        String path = null;
        Uri uri = data.getData();
        //根据不同的uri进行不同的解析
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                path = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
//                Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();

            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                path = getImagePath(contentUri, null);
//                Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();

            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            path = getImagePath(uri, null);
//            Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
//            Toast.makeText(this, "4", Toast.LENGTH_SHORT).show();
        }
        //展示图片
        setPath(path);
        displayImage(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2){
            //判断安卓版本
            if (resultCode == RESULT_OK&&data!=null){
                handImage(data);
            }
        }
    }


    //启动相册的方法
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    private void setListener(){
        OnClick onClick = new OnClick();
        mText4.setOnClickListener(onClick);
        mBtn1.setOnClickListener(onClick);
        mBtn2.setOnClickListener(onClick);
    }

    private class OnClick implements View.OnClickListener {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.deliver_text_3:
                    mText4.setText("正在连接...");
                    Thread thread = new Thread(() -> {
                        try {
                            socket = new Socket(ip, port);
                            if (socket.isConnected()) {
                                mText4.post(() -> {
                                    mText4.setText("连接成功");
                                });
                            }
                        } catch (IOException e) {
                            mText4.post(() -> {
                                mText4.setText("连接失败，点击重试。");
                            });
                        }
                    });
                    thread.start();
                    break;
                case R.id.deliver_btn_1:
                    openAlbum();
                    break;
                case R.id.deliver_btn_2:
                    sendBit(getSocket(),getPath());
                    break;
            }
        }
    }


    private Bitmap bitmap;
    private void sendBit(Socket socket,String path) {
        mText4.setText("点击连接");
        Thread thread = new Thread(() -> {
            try {
                if (socket.isClosed()){
                    Toast.makeText(DeliverActivity.this, "请连接PC。", Toast.LENGTH_SHORT).show();
                }else{
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    bitmap = BitmapFactory.decodeFile(path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //读取图片到ByteArrayOutputStream
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();
                    Log.d("byte len", String.valueOf(bytes.length));
                    out.write(bytes);
                    out.flush();
                    out.close();
                    socket.close();
                }
            } catch (IOException e) {
                Looper.prepare();
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }
        );
        thread.start();
        finish();
    }

}