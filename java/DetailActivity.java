package com.guoxingyuan.closeseewo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class DetailActivity extends Activity {


    TextView mText;
    TextView mText1;



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
        /**
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT 全屏模式，内容下移，非全屏不受影响
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES 允许内容区域延伸到刘海区
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER 不允许内容延伸进刘海区
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(params);
        setContentView(R.layout.activity_detail);
        mText=findViewById(R.id.detail_text);
        mText1=findViewById(R.id.detail_text_1);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/pingfang.ttf");
        String text = ReaderUtils.readAssetsTxt(this,"MyLaw");
        mText.setText(text);
        mText.setTypeface(typeface);
        mText1.setTypeface(typeface);
        mText1.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://wwc.lanzout.com/b0e3wqk3e");
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(uri);
            Toast.makeText(DetailActivity.this,"密码为c7u1",Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

    }


}