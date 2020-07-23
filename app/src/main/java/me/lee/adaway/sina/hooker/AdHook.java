package me.lee.adaway.sina.hooker;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.hooker.base.BaseHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static me.lee.adaway.sina.utils.HookUtil.showToast;

public class AdHook extends BaseHook {

    private Boolean skipStartActivityAd;

    @Override
    protected void initPrefs() {
        skipStartActivityAd = prefs.getBoolean(String.valueOf(R.id.skip_start_activity_ad), false);
    }

    @Override
    protected void hookMain() {
        loadPrefs();
        if(skipStartActivityAd){
            skipStartActivityAd();
        }
    }

    private void skipStartActivityAd(){
        findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "checkPermission", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                showToast("已为您跳过启动广告!", false, 500L);
                param.setResult(false);
            }
        });
    }
}
