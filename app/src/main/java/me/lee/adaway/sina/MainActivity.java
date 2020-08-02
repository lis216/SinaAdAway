package me.lee.adaway.sina;

import android.Manifest;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.utils.StringUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseAppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Button btn_zhifubao;

    //  HTTPS://QR.ALIPAY.COM/FKX03133TLJFCY8UNXHC56
    private String payCode = "tsx09184knkwcepgfxrqocd";  //支付宝支付扫描码，只截取后面的
    private Button btn_weixin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyAuth();
        setContentView(R.layout.activity_main);
        View view = this.getWindow().getDecorView();
        List<View> allView = Childs(view, false);
        for (View viewTemp : allView) {
            if (viewTemp instanceof Switch) {
                ((Switch) viewTemp).setOnCheckedChangeListener(this);
                ((Switch) viewTemp).setChecked(getBoolean(String.valueOf(viewTemp.getId())));
            }
        }

        TextView tips = findViewById(R.id.tip_0);
        Button downloadBtn = findViewById(R.id.download_apk);
        initView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HookPackage.initPackage();
                try {
                    String tip = "下载(当前:" + BuildConfig.VERSION_NAME + "  最新: " + HookPackage.getLastVersionName() + ")";
                    downloadBtn.setText(tip);
                    String versionName = getPackageManager().getPackageInfo(HookConstant.HOOK_PACKAGE_NAME, 0).versionName;
                    String text = "版本: " + versionName + " - " + BuildConfig.VERSION_NAME + "(" + HookPackage.isPerfectSupport(versionName) + ")";
                    tips.setText(text);
                    if (StringUtil.isNotEmpty(HookPackage.getNotice())) {
                        tips.setText(HookPackage.getNotice());
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    tips.setText(R.string.tip_0);
                }
            }
        }).start();
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
        HookPackage.getConfig().put(String.valueOf(compoundButton.getId()), b);
        putBoolean(String.valueOf(compoundButton.getId()), b);
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
            case R.id.skip_start_activity_ad:
            case R.id.hide_top_hongbao:
            case R.id.auto_sign:
            case R.id.hide_hongbao:
            case R.id.auto_get_hongbao:
            case R.id.hide_content_ad:
            case R.id.hide_detail_ad:
            case R.id.hide_detail_share:
            case R.id.hide_comment_ad:
            case R.id.hide_find_page_nav:
            case R.id.hide_find_page_carousel:
            case R.id.cancel_hide_content_hot:
            case R.id.hide_person_head_pendant:
            case R.id.hide_person_background:
                break;
            default:
                if (b) {
                    compoundButton.setChecked(false);
                    putBoolean(String.valueOf(compoundButton.getId()), false);
                    HookPackage.getConfig().put(String.valueOf(compoundButton.getId()), false);
                }
                break;
        }
        HookPackage.saveConfig();
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
        String cacheWords = HookPackage.getConfig().getString(String.valueOf(viewId));
        input.setText(cacheWords);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setView(input).setNegativeButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String words = input.getText().toString();
                TextView view = findViewById(viewId);
                if (StringUtil.isNotEmpty(words)) {
                    HookPackage.getConfig().put(String.valueOf(viewId), words);
                    HookPackage.saveConfig();
                    view.setText(words);
                } else {
                    HookPackage.getConfig().remove(String.valueOf(viewId));
                    HookPackage.saveConfig();
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


    private void applyAuth() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTANCT = 101;
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for (String str : permissions) {
                if (MainActivity.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.this.requestPermissions(permissions, REQUEST_CODE_CONTANCT);
                    return;
                }
            }
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
        String cacheContentWords = HookPackage.getConfig().getString(String.valueOf(R.id.filter_content_key_word));
        if (StringUtil.isNotEmpty(cacheContentWords)) {
            contentKeyWord.setText(cacheContentWords);
        }
        // 用户过滤输入框
        TextView userKeyWord = findViewById(R.id.filter_user_key_word);
        userKeyWord.setOnClickListener(new MyOnClickListener());
        String cacheUserWords = HookPackage.getConfig().getString(String.valueOf(R.id.filter_user_key_word));
        if (StringUtil.isNotEmpty(cacheUserWords)) {
            userKeyWord.setText(cacheUserWords);
        }

        Button downloadBtn = findViewById(R.id.download_apk);
        downloadBtn.setOnClickListener(new MyOnClickListener());
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
