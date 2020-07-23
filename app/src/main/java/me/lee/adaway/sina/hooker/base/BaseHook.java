package me.lee.adaway.sina.hooker.base;

import android.view.View;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import me.lee.adaway.sina.HookPackage;
import me.lee.adaway.sina.constant.HookConstant;

import java.lang.reflect.Field;

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

    protected <T> T getObject(Object obj, Class<?> type, String name) {
        return getObject(obj.getClass(), type, name, obj);
    }

    protected <T> T getObject(Class clazz, Class<?> type, String name) {
        return getObject(clazz, type, name, null);
    }

    @SuppressWarnings ("unchecked")
    protected <T> T getObject(Class clazz, Class<?> type, String name, Object obj) {
        try {
            Field field = findField(clazz, type, name);
            return field == null ? null : (T) field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    protected Field findField(Class<?> clazz, Class<?> type, String name) {
        if (clazz != null && type != null && !name.isEmpty()) {
            Class<?> clz = clazz;
            do {
                for (Field field : clz.getDeclaredFields()) {
                    if (field.getType() == type && field.getName()
                            .equals(name)) {
                        field.setAccessible(true);
                        return field;
                    }
                }
            } while ((clz = clz.getSuperclass()) != null);
        }
        return null;
    }

    protected void hideView(Object obj, Class<?> type, String name) {
        View view = getObject(obj, type, name);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }


}
