package me.lee.adaway.sina.utils;

import android.content.Context;
import android.os.Environment;
import me.lee.adaway.sina.constant.HookConstant;

import java.io.*;

/**
 * Created by zpp0196 on 2018/5/27 0027.
 */

public class FileUtil {

    public static File getDataDir(Context context) {
        return new File(context.getApplicationInfo().dataDir);
    }

    public static File getPrefsDir(Context context) {
        return new File(getDataDir(context), "shared_prefs");
    }

    public static File getPrefsFile(Context context) {
        return new File(getPrefsDir(context), HookConstant.MODULE_PACKAGE_NAME + "_preferences.xml");
    }

    public static File getBackupPrefsFile() {
        return new File(getSdDataDir(), HookConstant.MODULE_PACKAGE_NAME + "_preferences.xml");
    }

    public static File getSdDataDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + HookConstant.MODULE_PACKAGE_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void writeFile(String filePath, String fileName, String content, boolean append) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(filePath + fileName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, append);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(content);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filePath) {
        BufferedReader reader = null;
        String laststr = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr;
    }
}
