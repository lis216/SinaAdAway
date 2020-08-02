package me.lee.adaway.sina.hooker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.HookPackage;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import static me.lee.adaway.sina.utils.HookUtil.showToast;

public class HomePageHook extends BaseHook {

    private Boolean hideTopHongbao;
    private Boolean autoSign;
    private Boolean hideHongbao;
    private Boolean autoGetHongbao;

    private String signDate = "";
    private boolean close = false;

    @Override
    protected void initConfig() {
        hideTopHongbao = config.getBoolean(String.valueOf(R.id.hide_top_hongbao));
        autoSign = config.getBoolean(String.valueOf(R.id.auto_sign));
        hideHongbao = config.getBoolean(String.valueOf(R.id.hide_hongbao));
        autoGetHongbao = config.getBoolean(String.valueOf(R.id.auto_get_hongbao));
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (hideTopHongbao) hideTopHongbao();
        if (hideHongbao) hideFloatHongbao();
        if (autoGetHongbao) autoGetHongbao();
    }

    private void hideTopHongbao() {
        try {
            Class FeedRedPacket = loader.loadClass("com.sina.weibo.models.FeedRedPacket");
            HookUtil.findAndHookMethod("com.sina.weibo.utils.bx", loader, "a", Context.class, ViewGroup.class, FeedRedPacket, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    ViewGroup viewGroup = (ViewGroup) param.args[1];
                    viewGroup.setVisibility(View.GONE);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    if (autoSign && !formatter.format(new Date()).equals(config.getString("signDate"))) {
                        viewGroup.callOnClick();
                        signDate = formatter.format(new Date());
                        HookPackage.getConfig().put("signDate", signDate);
                        HookPackage.saveConfig();
                        loadConfig();
                        close = true;
                    }
                }
            });

            HookUtil.findAndHookMethod("com.sina.weibo.view.CommonTitleBar", loader, "setButtonClickListener", View.OnClickListener.class, View.OnClickListener.class, View.OnClickListener.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    TextView view = getObject(param.thisObject, TextView.class, "c");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    if (close && formatter.format(new Date()).equals(config.getString("signDate"))) {
                        close = false;
                        new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    view.callOnClick();
                                } catch (InterruptedException e) {
                                }
                            }
                        }.start();
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.log("");
        }

    }

    private void hideFloatHongbao() {
        HookUtil.findAndHookMethod("com.sina.weibo.floatingwindow.FloatingWindowManagerImp", loader, "genContainer", replaceNull());
    }

    private void autoGetHongbao() {
        HookUtil.findAndHookMethod("com.sina.weibo.feed.q.a", loader, "a", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                int i = (int) param.args[0];
                int type = getObject(param.thisObject, int.class, "s");
                // type  2:正在刷 3:完成
                if (type == 3) {
                    HookUtil.callMethod(param.thisObject, "d");
                    showToast("已自动领取红包!");
                }
            }
        });
    }

}
