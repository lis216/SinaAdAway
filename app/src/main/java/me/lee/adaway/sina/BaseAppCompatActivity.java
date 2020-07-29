package me.lee.adaway.sina;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.utils.FileUtil;
import me.lee.adaway.sina.utils.HookUtil;

import java.io.File;

import static me.lee.adaway.sina.utils.FileUtil.*;

public class BaseAppCompatActivity extends AppCompatActivity {

    private AlertDialog mActiveDialog = null;

    public SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public SharedPreferences.Editor getEditor() {
        return getPrefs().edit();
    }

    public void putBoolean(String key, Boolean b) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, b);
        editor.commit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWorldReadable();
    }

    public Boolean getBoolean(String key) {
        SharedPreferences prefs = getPrefs();
        return prefs.getBoolean(key, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkActiveState();
    }

    @Override
    public void onPause() {
        super.onPause();
        setWorldReadable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setWorldReadable();
    }

    private void checkActiveState() {
        new Handler().postDelayed(() -> {
            if (!isModuleActive() && !isModuleInExposedActive(this)) {
                showActiveDialog();
            }
        }, 500L);
    }

    private boolean isModuleActive() {
        // VirtualXposed 在某些机型上hook短方法有问题，这里认为添加日志增大方法长度确保能hook成功。
        Log.i("Xposed Log", "isModuleActive");
        return false;
    }

    private boolean isModuleInExposedActive(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return false;
        }

        Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
        try {
            Bundle result = contentResolver.call(uri, "active", null, null);
            if (result == null) {
                return false;
            }
            return result.getBoolean("active", false);
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isInXposedEnvironment() {
        boolean flag = false;
        try {
            ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedBridge");
            flag = true;
        } catch (ClassNotFoundException ignored) {
        }
        return flag;
    }

    private void showActiveDialog() {
        if (mActiveDialog == null) {
            mActiveDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.msg_module_not_active)
                    .setPositiveButton(R.string.btn_active, (dialog, which) -> {
                        if (isInXposedEnvironment()) {
                            openXposedInstaller();
                        } else {
                            openExposed();
                        }
                    })
                    .setNegativeButton(R.string.btn_ignore, null)
                    .create();
            mActiveDialog.show();
        } else {
            if (!mActiveDialog.isShowing()) {
                mActiveDialog.show();
            }
        }
    }

    private void openXposedInstaller() {
        String xposed = "de.robv.android.xposed.installer";
        if (HookUtil.isPackageInstalled(this, xposed)) {
            Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
            if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
                intent = getPackageManager().getLaunchIntentForPackage(xposed);
            }
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("section", "modules")
                        .putExtra("fragment", 1)
                        .putExtra("module", HookConstant.MODULE_PACKAGE_NAME);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, R.string.toast_xposed_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    private void openExposed() {
        String exposed = "me.weishu.exp";
        if (HookUtil.isPackageInstalled(this, exposed)) {
            Intent intent = new Intent(exposed + ".ACTION_MODULE_MANAGE");
            intent.setData(Uri.parse("package:" + HookConstant.MODULE_PACKAGE_NAME));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.toast_exposed_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    @SuppressLint({"SetWorldReadable", "WorldReadableFiles"})
    public void setWorldReadable() {
        if (getPrefsFile(this).exists()) {
            for (File file : new File[]{FileUtil.getDataDir(this), getPrefsDir(this), getPrefsFile(this), getSdDataDir()}) {
                file.setReadable(true, false);
                file.setExecutable(true, false);
            }
        }
    }
}
