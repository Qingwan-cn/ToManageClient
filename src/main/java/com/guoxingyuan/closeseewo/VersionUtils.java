package com.guoxingyuan.closeseewo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionUtils {
    public static void update(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("inf",Context.MODE_PRIVATE);
        String flag = sharedPreferences.getString("version","error");
        if (flag.equals("error")){
            Date date= new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String s= sdf.format(date);
            int j = Integer.parseInt(s);
            if (!(j>20220601)){
                String message= ReaderUtils.readAssetsTxt(context,"NewVersion");
                new AlertDialog.Builder(context)
                        .setTitle("版本更新")
                        .setMessage(message)
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            Intent intent = new Intent(context,DetailActivity.class);
                            context.startActivity(intent);
                        })
                        .setCancelable(false)
                        .show();
            }
            //3.1版本更新
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("version","3.1");
            editor.apply();
        }
    }
}
