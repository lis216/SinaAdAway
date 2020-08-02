package me.lee.adaway.sina.hooker;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;

public class MblogHook extends BaseHook {

    private boolean hidePersonBackground;
    private boolean hidePersonHeadPendant;

    @Override
    protected void initConfig() {
        hidePersonBackground = config.getBoolean(String.valueOf(R.id.hide_person_background));
        hidePersonHeadPendant = config.getBoolean(String.valueOf(R.id.hide_person_head_pendant));
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (hidePersonHeadPendant) hidePersonHeadPendant();
        if (hidePersonBackground) hidePersonBackground();
    }

    private void hidePersonHeadPendant() {
        HookUtil.findAndHookConstructor("com.sina.weibo.feed.view.MblogTitleView", loader, Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    private void hidePersonBackground() {

    }
}
