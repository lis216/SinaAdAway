package me.lee.adaway.sina.hooker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.hooker.info.Classes;
import me.lee.adaway.sina.utils.LogUtil;

import java.util.List;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static me.lee.adaway.sina.utils.HookUtil.showToast;

public class HomeHook extends BaseHook {

    private Boolean hideTopHongbao;
    private Boolean autoSign;
    private Boolean hideHongbao;
    private Boolean autoGetHongbao;

    @Override
    protected void initPrefs() {
        hideTopHongbao = prefs.getBoolean(String.valueOf(R.id.hide_top_hongbao), false);
        autoSign = prefs.getBoolean(String.valueOf(R.id.auto_sign), false);
        hideHongbao = prefs.getBoolean(String.valueOf(R.id.hide_hongbao), false);
        autoGetHongbao = prefs.getBoolean(String.valueOf(R.id.auto_get_hongbao), false);
    }

    @Override
    protected void hookMain() {
        loadPrefs();
        if(hideTopHongbao) hideTopHongbao();
        if(autoSign) autoSign();
        if(hideHongbao) hideHongbao();
        if(autoGetHongbao) autoGetHongbao();
    }

    private void hideTopHongbao(){

        findAndHookMethod("com.sina.weibo.feed.home.titlebar.FeedTitleBarView", loader, "c", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
        });

        findAndHookMethod("com.sina.weibo.utils.bw", loader, "a", Context.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                showToast("右上角红包点击Hook");
                return null;
            }
        });
    }

    private void autoSign(){

    }

    private void hideHongbao(){

    }

    private void autoGetHongbao(){

    }
}
