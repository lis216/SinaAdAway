package me.lee.adaway.sina.hooker.base;

import android.view.View;
import com.alibaba.fastjson.JSONObject;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import me.lee.adaway.sina.HookPackage;
import me.lee.adaway.sina.constant.HookConstant;

import java.lang.reflect.Field;

import static de.robv.android.xposed.XposedBridge.log;

public abstract class BaseHook {
    protected JSONObject config;
    protected ClassLoader loader;

    public BaseHook() {
        this.loader = HookPackage.getLoader();
        this.config = HookPackage.getConfig();
    }

    protected abstract void hookMain();

    public final void startHook() {
        HookPackage.initPackage();
        if (!HookPackage.isEnabled()) {
            return;
        }
        if (!HookPackage.isVersionEnabled()) {
            return;
        }
        try {
            hookMain();
        } catch (Throwable t) {
            log(t);
        }
    }

    protected boolean disableHook() {
        return HookPackage.getVersionName().compareTo(HookConstant.DISABLE_VERSION) < 0;
    }

    protected final void loadConfig() {
        this.config = HookPackage.loadConfig();
        initConfig();
    }

    protected void initConfig() {
    }

    protected <T> T getObject(Object obj, Class<?> type, String name) {
        return getObject(obj.getClass(), type, name, obj);
    }

    protected <T> T getObject(Class clazz, Class<?> type, String name) {
        return getObject(clazz, type, name, null);
    }

    @SuppressWarnings("unchecked")
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

    protected XC_MethodHook replaceNull() {
        return replaceObj(null);
    }

    protected XC_MethodHook replaceFalse() {
        return replaceObj(false);
    }

    protected XC_MethodHook replaceObj(Object result) {
        return XC_MethodReplacement.returnConstant(result);
    }

}
