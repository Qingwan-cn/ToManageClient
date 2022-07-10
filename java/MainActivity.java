package com.guoxingyuan.closeseewo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class MainActivity extends Activity {

    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private TextView mText4;
    private TextView mText5;
    private EditText mEdit1;
    private EditText mEdit2;
    private EditText mEdit3;
    private Button mBtn1;
    private Button mBtn2;

    private Typeface typeface;

    private Socket socket;

    public Socket getSocket() {
        return socket;
    }

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private int MODE_CODE;
    private final int MODE_CODE_FILE = 1;
    private final int MODE_CODE_DEMAND = 0;
    private final int MODE_CODE_DELIVER = 2;

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
        setContentView(R.layout.activity_main);

        //参数设置
        sharedPreferences = getSharedPreferences("inf", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        typeface = Typeface.createFromAsset(getAssets(), "fonts/pingfang.ttf");
        MODE_CODE = MODE_CODE_DEMAND;
        //控件注册
        mText1 = findViewById(R.id.main_text_1);
        mText2 = findViewById(R.id.main_text_2);
        mText3 = findViewById(R.id.main_text_3);
        mText4 = findViewById(R.id.main_text_4);
        mText5 = findViewById(R.id.main_text_5);
        mEdit1 = findViewById(R.id.main_edit_1);
        mEdit2 = findViewById(R.id.main_edit_2);
        mEdit3 = findViewById(R.id.main_edit_3);
        mBtn1 = findViewById(R.id.main_btn_1);
        mBtn2 = findViewById(R.id.main_btn_2);

        //字体设置
        mText1.setTypeface(typeface);
        mText2.setTypeface(typeface);
        mText3.setTypeface(typeface);
        mText4.setTypeface(typeface);
        mText5.setTypeface(typeface);
        mEdit1.setTypeface(typeface);
        mEdit2.setTypeface(typeface);
        mEdit3.setTypeface(typeface);
        mBtn1.setTypeface(typeface);
        mBtn2.setTypeface(typeface);


        String demand = ReaderUtils.readAssetsTxt(this, "demand");
        mText4.setHint(demand);
        mText5.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);


        //更新版本提示
        AlertUtils.update(this);

        autoFill();
        setListeners();

    }


    private void setListeners() {
        OnClick onClick = new OnClick();
        mBtn1.setOnClickListener(onClick);
        mBtn2.setOnClickListener(onClick);
        mText3.setOnClickListener(onClick);
        mText5.setOnClickListener(onClick);
    }


    public void setDemand() {
        String demand;
        String s = mEdit3.getText().toString();
        switch (s) {
            case "1":
                demand = "shutdown -s -t 0";
                break;
            case "2":
                demand = "taskkill /f /t /im EasiNote.exe";
                break;
            case "3":
                demand = "taskkill /f /t /im EasiCamera.exe";
                break;
            case "4":
                demand = "taskkill /f /t /im Video.UI.exe";
                break;
            case "5":
                demand = "taskkill /f /t /im Microsoft.Photos.exe";
                break;
            case "6":
                demand = "taskkill /f /t /im EXCEL.exe";
                break;
            case "7":
                demand = "taskkill /f /t /im WINWORD.EXE";
                break;
            case "8":
                demand = "taskkill /f /t /im POWERPNT.exe";
                break;
            default:
                demand = s;
        }
        this.demand = demand;
    }

    private String demand;

    private void autoFill() {
        String ip = sharedPreferences.getString("IP", null);
        String port = sharedPreferences.getString("port", null);
        mEdit1.setText(ip);
        mEdit2.setText(port);
    }

    private void connect() {
        String ip = mEdit1.getText().toString();
        String portStr = mEdit2.getText().toString();
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(portStr)) {
            Toast.makeText(MainActivity.this, "参数不能为空。", Toast.LENGTH_SHORT).show();
        } else {
            mText2.setText("正在连接...");
            editor.putString("IP", ip);
            editor.putString("port", portStr);
            editor.apply();
            int port = Integer.parseInt(portStr);
            Thread thread = new Thread(() -> {
                try {
                    socket = new Socket(ip, port);
                    boolean flag = socket.isConnected();
                    if (flag) {
                        mText2.post(() -> mText2.setText("连接成功"));
                    }
                } catch (IOException e) {
                    mText2.post(() -> mText2.setText("连接失败"));
                    String message = "连接失败，可能有以下原因:\r\n 1.IP地址及端口号输入不正确。\r\n 2.手机和PC未连接至同一无线局域网下。\r\n3.PC端未运行相应程序。\r\n若当前没有可用的无线局域网连接，请开启手机热点后使用PC连接手机热点。\r\n可通过PC及手机的网络设置详情界面查看IP地址。";
                    Looper.prepare();
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("连接异常")
                            .setMessage(message)
                            .setPositiveButton("确定", null)
                            .show();
                    Toast.makeText(MainActivity.this, "无法与指定的PC建立Socket连接。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            });
            thread.start();
        }


    }

    private DataOutputStream out;

    private void send(Socket socket) throws UnsupportedEncodingException {
        if (socket == null) {
            Toast.makeText(MainActivity.this, "请先连接PC。", Toast.LENGTH_SHORT).show();
        } else {
            mText2.setText("等待连接...");
            setDemand();
            String myDemand = demand;
            if (MODE_CODE == MODE_CODE_FILE) {
                myDemand = "file" + demand;
            }
            byte[] myDemandByte = myDemand.getBytes("gbk");
            Thread thread = new Thread(() -> {
                try {
                    out = new DataOutputStream(socket.getOutputStream());
                    out.write(myDemandByte);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "为避免重复发送，请先连接PC。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            });
            thread.start();
        }
    }

    private class OnClick implements View.OnClickListener {
        @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.main_btn_1:
                    //连接按钮
                    connect();
                    break;
                case R.id.main_btn_2:
                    //发送按钮
                    if (MODE_CODE == MODE_CODE_DELIVER) {
                        String ip = mEdit1.getText().toString();
                        String port = mEdit2.getText().toString();
                        //隔空投送模式，跳转界面
                        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
                            Toast.makeText(MainActivity.this, "参数不能为空。", Toast.LENGTH_SHORT).show();
                        } else {
                            if (socket==null){
                                Toast.makeText(MainActivity.this, "请连接PC。", Toast.LENGTH_SHORT).show();
                            }else{
                                sendCode(getSocket());
                                Intent intent = new Intent(MainActivity.this, DeliverActivity.class);
                                intent.putExtra("ip", ip);
                                intent.putExtra("port", port);
                                startActivity(intent);
                            }
                        }
                    } else {
                        try {
                            send(getSocket());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.main_text_3:
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    startActivity(intent);
                    break;
                case R.id.main_text_5:
                    //切换模式
                    switch (MODE_CODE) {
                        case MODE_CODE_DEMAND:
                            //切换为文件模式
                            MODE_CODE = MODE_CODE_FILE;
                            mText5.setText("文件");
                            mBtn2.setText("send");
                            mText2.setText("等待连接...");
                            break;
                        case MODE_CODE_FILE:
                            //切换为隔空投送模式
                            MODE_CODE = MODE_CODE_DELIVER;
                            mText5.setText("隔空投送");
                            mText2.setText("请在此界面连接PC后再跳转。");
                            mBtn2.setText("jump");
                            break;
                        case MODE_CODE_DELIVER:
                            //切换为指令模式
                            MODE_CODE = MODE_CODE_DEMAND;
                            mText5.setText("指令");
                            mBtn2.setText("send");
                            mText2.setText("等待连接...");
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    }


    private void sendCode(Socket socket){
        Thread thread = new Thread(() -> {
            try {
                out = new DataOutputStream(socket.getOutputStream());
                byte[] bytes = "deliver".getBytes("gbk");
                out.write(bytes);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }

}