package com.guoxingyuan.closeseewo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlertUtils {

    public static void update(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("inf", Context.MODE_PRIVATE);
        String flag = sharedPreferences.getString("version3.3", "error");
        if (flag.equals("error")) {
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String s = sdf.format(date);
            int j = Integer.parseInt(s);
            if (!(j > 20220830)) {
                String message = ReaderUtils.readAssetsTxt(context, "NewVersion");
                new AlertDialog.Builder(context)
                        .setTitle("版本更新")
                        .setMessage(message)
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            Intent intent = new Intent(context, DetailActivity.class);
                            context.startActivity(intent);
                        })
                        .setCancelable(false)
                        .show();
            }
            //3.3版本更新
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("version3.3", "3.3");
            editor.apply();
        }
    }

    private static final int REQUEST_CODE = 1;

    public static void applyForPermission(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("inf", Context.MODE_PRIVATE);
        int flag = sharedPreferences.getInt("permission", -1);
        if (flag==-1){
            new AlertDialog.Builder(context)
                    .setTitle("权限获取")
                    .setMessage("“隔空投送”功能需要获取存储权限以访问照片。系统会在你点击“同意”后申请权限，是否同意我们获取你的这项权限？")
                    .setCancelable(false)
                    .setPositiveButton("同意", (dialogInterface, i) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("permission",1);
                        editor.apply();
                        try {
                            String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            int permission = ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE");
                            if (permission != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions((Activity) context, PERMISSIONS_STORAGE,REQUEST_CODE );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("拒绝", (dialogInterface, i) -> {
                        Activity activity = (Activity) context;
                        activity.finish();
                    })
                    .show();
        }

    }
}
