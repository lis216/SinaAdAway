package me.lee.adaway.sina;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.lee.adaway.sina.constant.HookConstant;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseAppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

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
        initView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HookPackage.initSurportVersion();
                try {
                    String versionName = getPackageManager()
                            .getPackageInfo(HookConstant.HOOK_PACKAGE_NAME, 0).versionName;
                    String text = "微博版本: v" + versionName;
                    if (HookPackage.isSupport(versionName)) {
                        text = text + " 已适配!";
                    } else {
                        text = text + " 未适配, 部分功能可能无效!";
                    }
                    tips.setText(text);
                } catch (PackageManager.NameNotFoundException e) {
                    tips.setText("微博版本: Unknown");
                }
            }
        }).start();
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
            case R.id.hide_find_page_carousel:
            case R.id.cancel_hide_content_hot:
                break;
            default:
                if (b) {
                    Toast.makeText(this, "功能暂未实现!", Toast.LENGTH_SHORT).show();
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
        btn_zhifubao = (Button) findViewById(R.id.btn_zhifubao);
        btn_zhifubao.setOnClickListener(this);
        btn_weixin = (Button) findViewById(R.id.btn_weixin);
        btn_weixin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_zhifubao:

                donateAlipay(payCode);
                break;
            case R.id.btn_weixin:

                donateWeixin();
                break;
        }
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
