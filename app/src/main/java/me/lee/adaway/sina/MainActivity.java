package me.lee.adaway.sina;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.utils.FileUtil;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.StringUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static me.lee.adaway.sina.utils.FileUtil.*;


public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private AlertDialog mActiveDialog = null;

    private Button btn_zhifubao;
    private String payCode = "tsx09184knkwcepgfxrqocd";  //支付宝支付扫描码，只截取后面的
    private Button btn_weixin;

    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private int REQUEST_CODE = 101;


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


    public Boolean getBoolean(String key) {
        SharedPreferences prefs = getPrefs();
        return prefs.getBoolean(key, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWorldReadable();
        requestPermissions();
        setContentView(R.layout.activity_main);
        View view = this.getWindow().getDecorView();
        List<View> allView = Childs(view, false);
        for (View viewTemp : allView) {
            if (viewTemp instanceof Switch) {
                ((Switch) viewTemp).setOnCheckedChangeListener(this);
                ((Switch) viewTemp).setChecked(getBoolean(String.valueOf(viewTemp.getId())));
            }
        }
        initView();

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

    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit_question:
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse("https://support.qq.com/products/181702");
                    intent.setData(content_url);
                    startActivity(intent);
                    break;
                case R.id.btn_zhifubao:
                    donateAlipay(payCode);
                    break;
                case R.id.btn_weixin:
                    donateWeixin();
                    break;
                case R.id.filter_content_key_word:
                    inputTitleDialog("输入关键词", "含关键词的微博将被过滤", R.id.filter_content_key_word);
                    break;
                case R.id.filter_user_key_word:
                    inputTitleDialog("输入关键词", "用户名含关键词的微博将被过滤", R.id.filter_user_key_word);
                    break;
                case R.id.download_apk:
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 将文本内容放到系统剪贴板里。
                    cm.setText("6666");
                    Toast.makeText(MainActivity.this, "密码复制成功!", Toast.LENGTH_LONG).show();
                    Intent downloadIntent = new Intent();
                    downloadIntent.setAction("android.intent.action.VIEW");
                    Uri downloadUrl = Uri.parse("https://wws.lanzous.com/b01hk8zfc");
                    downloadIntent.setData(downloadUrl);
                    startActivity(downloadIntent);
                    break;
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        String key = String.valueOf(compoundButton.getId());
        Boolean value = b;
        switch (compoundButton.getId()) {
            case R.id.hide_icon:
                changeIconStatus(!b);
                break;
            case R.id.active_module:
                if (!b) {
                    View view = this.getWindow().getDecorView();
                    List<View> allView = Childs(view, false);
                    for (View viewTemp : allView) {
                        if (viewTemp instanceof Switch) {
                            viewTemp.setEnabled(false);
                            Switch hideIcon = findViewById(R.id.hide_icon);
                            hideIcon.setEnabled(true);
                            compoundButton.setEnabled(true);
                        }
                    }
                } else {
                    View view = this.getWindow().getDecorView();
                    List<View> allView = Childs(view, false);
                    for (View viewTemp : allView) {
                        if (viewTemp instanceof Switch) {
                            viewTemp.setEnabled(true);
                        }
                    }
                }
                break;
            case R.id.remove_all_ad:
            case R.id.hide_follow_recommend:
            case R.id.hide_top_hongbao:
            case R.id.auto_sign:
            case R.id.hide_hongbao:
            case R.id.auto_get_hongbao:
            case R.id.hide_detail_share:
            case R.id.hide_comment_follow_card:
            case R.id.hide_find_page_nav:
            case R.id.hide_find_page_carousel:
            case R.id.cancel_hide_content_hot:
            case R.id.hide_person_head_pendant:
            case R.id.hide_person_background:
            case R.id.hide_setting:
            case R.id.hide_visit:
            case R.id.hide_chaohua:
            case R.id.hide_publicwelfare:
            case R.id.hide_examination:
            case R.id.hide_dailv:
            case R.id.hide_draft:
            case R.id.cancel_hide_sport:
            case R.id.hide_member_icon:
                break;
            default:
                if (b) {
                    compoundButton.setChecked(false);
                    value = false;

                }
                break;
        }
        putBoolean(key, value);
        HookPackage.putLocalConfig(key, value);
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
        if (HookUtil.isPackageInstalled(this, "me.weishu.exp")) {
            Intent intent = new Intent("me.weishu.exp.ACTION_MODULE_MANAGE");
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

    private void changeIconStatus(boolean isShow) {
        final ComponentName aliasName = new ComponentName(this, getClass().getCanonicalName() + "Alias");
        final PackageManager packageManager = getPackageManager();
        int status = isShow ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (packageManager.getComponentEnabledSetting(aliasName) != status) {
            packageManager.setComponentEnabledSetting(aliasName, status, PackageManager.DONT_KILL_APP);
        }
    }

    private void inputTitleDialog(String title, String message, int viewId) {

        final EditText input = new EditText(this);
        input.setFocusable(true);
        String cacheWords = HookPackage.getLocalConfig().getString(String.valueOf(viewId));
        input.setText(cacheWords);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setView(input).setNegativeButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String words = input.getText().toString();
                TextView view = findViewById(viewId);
                if (StringUtil.isNotEmpty(words)) {
                    HookPackage.putLocalConfig(String.valueOf(viewId), words);
                    view.setText(words);
                } else {
                    HookPackage.removeLocalConfig(String.valueOf(viewId));
                    switch (viewId) {
                        case R.id.filter_content_key_word:
                            view.setText(R.string.filter_content_key_word);
                            break;
                        case R.id.filter_user_key_word:
                            view.setText(R.string.filter_user_key_word);
                            break;
                    }
                }

            }
        });
        builder.show();
    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String str : permissions) {
                if (MainActivity.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.this.requestPermissions(permissions, REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int index = 0; index < grantResults.length; index++) {
            if (PackageManager.PERMISSION_DENIED == grantResults[index])
                finish();
        }
    }

    /**
     * 获取当前View的所有子view
     */
    public static List<View> Childs(View view, boolean ContainsThis) {
        List<View> viewList = new ArrayList<View>();
        if (!viewList.contains(view)) viewList.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (!viewList.contains(child)) viewList.add(child);
                // 添加child的子节点
                List<View> subList = Childs(child, true);
                for (View v : subList) {
                    if (!viewList.contains(v)) viewList.addAll(subList);
                }
            }
        }
        if (!ContainsThis) viewList.remove(view);
        return viewList;
    }


    private void initView() {
        //支付宝捐赠
        btn_zhifubao = (Button) findViewById(R.id.btn_zhifubao);
        btn_zhifubao.setOnClickListener(new MyOnClickListener());
        // 微信捐赠
        btn_weixin = (Button) findViewById(R.id.btn_weixin);
        btn_weixin.setOnClickListener(new MyOnClickListener());
        // 问题反馈
        Button button = findViewById(R.id.submit_question);
        button.setOnClickListener(new MyOnClickListener());
        // 内容过滤
        TextView contentKeyWord = findViewById(R.id.filter_content_key_word);
        contentKeyWord.setOnClickListener(new MyOnClickListener());
        String cacheContentWords = HookPackage.getLocalConfig().getString(String.valueOf(R.id.filter_content_key_word));
        if (StringUtil.isNotEmpty(cacheContentWords)) {
            contentKeyWord.setText(cacheContentWords);
        }
        // 用户过滤输入框
        TextView userKeyWord = findViewById(R.id.filter_user_key_word);
        userKeyWord.setOnClickListener(new MyOnClickListener());
        String cacheUserWords = HookPackage.getLocalConfig().getString(String.valueOf(R.id.filter_user_key_word));
        if (StringUtil.isNotEmpty(cacheUserWords)) {
            userKeyWord.setText(cacheUserWords);
        }

        Button downloadBtn = findViewById(R.id.download_apk);
        downloadBtn.setOnClickListener(new MyOnClickListener());
        TextView tips = findViewById(R.id.tip_0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HookRemoteConfig remoteConfig = HookPackage.getRemoteConfig();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String tip = "下载(当前:" + BuildConfig.VERSION_NAME + "  最新: " + remoteConfig.getLastVersionName() + ")";
                            downloadBtn.setText(tip);
                            String versionName = getPackageManager().getPackageInfo(HookConstant.HOOK_PACKAGE_NAME, 0).versionName;
                            String text = "版本: " + versionName + " - " + BuildConfig.VERSION_NAME + "(" + remoteConfig.isPerfectSupport(versionName) + ")";
                            tips.setText(text);
                            if (StringUtil.isNotEmpty(remoteConfig.getNotice())) {
                                tips.setText(remoteConfig.getNotice());
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            tips.setText(R.string.tip_0);
                        }
                    }
                });

            }
        }).start();
    }


    /**
     * 需要提前准备好 微信收款码 照片，可通过微信客户端生成
     * wxp://f2f0j1REHFC8YJor7UUsS6N-1PZiFE2mhOht
     */
    private void donateWeixin() {
        InputStream weixinQrIs = getResources().openRawResource(R.raw.weixin);
        String qrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AndroidDonateSample" + File.separator +
                "weixin.png";
        WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs));
        WeiXinDonate.donateViaWeiXin(this, qrPath);
    }


    /**
     * 支付宝支付
     *
     * @param payCode 收款码后面的字符串；例如：收款二维码里面的字符串为 HTTPS://QR.ALIPAY.COM/FKX03133TLJFCY8UNXHC56 ，则
     *                payCode = FKX03133TLJFCY8UNXHC56
     *                注：不区分大小写
     */
    private void donateAlipay(String payCode) {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(this, payCode);
        }
    }


}
