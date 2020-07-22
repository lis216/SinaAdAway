package me.lee.adaway.sina.hooker.base;

import de.robv.android.xposed.XSharedPreferences;
import me.lee.adaway.sina.HookPackage;
import me.lee.adaway.sina.constant.HookConstant;

import static de.robv.android.xposed.XposedBridge.log;

public abstract class BaseHook {
    protected XSharedPreferences prefs;
    protected ClassLoader loader;

    public BaseHook() {
        this.prefs = HookPackage.getXSharedPrefs();
        this.loader = HookPackage.loader;
    }

    protected abstract void hookMain();

    public final void startHook() {
        if (disableHook()) return;
        try {
            hookMain();
        } catch (Throwable t) {
            log(t);
        }
    }

    protected boolean disableHook() {
        return HookPackage.versionName.compareTo(HookConstant.DISABLE_VERSION) < 0;
    }

    protected final void loadPrefs() {
        prefs = HookPackage.getXSharedPrefs();
        prefs.reload();
        initPrefs();
    }

    protected void initPrefs() {
    }
}
