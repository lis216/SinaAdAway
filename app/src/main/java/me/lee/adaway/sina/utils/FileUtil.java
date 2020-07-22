package me.lee.adaway.sina.utils;

import android.content.Context;
import android.os.Environment;
import me.lee.adaway.sina.constant.HookConstant;

import java.io.File;
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

}
