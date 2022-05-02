package com.guoxingyuan.closeseewo;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReaderUtils {


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String readAssetsTxt(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open("text/"+fileName+".txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }

}
