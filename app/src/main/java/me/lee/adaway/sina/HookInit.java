package me.lee.adaway.sina;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.XResources;
import android.util.TypedValue;
import android.view.View;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.utils.HookUtil;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class HookInit implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

    /**
     * 查看是否激活
     *
     * @param loader
     */
    private void setModuleActive(ClassLoader loader) {
        findAndHookMethod(HookConstant.MODULE_PACKAGE_NAME + ".BaseAppCompatActivity", loader, "isModuleActive", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(HookConstant.MODULE_PACKAGE_NAME)) {
            setModuleActive(lpparam.classLoader);
        }
        if (lpparam.packageName.equals(HookConstant.HOOK_PACKAGE_NAME)) {
            findAndHookMethod(Instrumentation.class, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    if (HookUtil.isInMainProcess(context)) {
                        HookPackage.getInstance().hookHandler(lpparam);
                    }
                }
            });
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (resparam.packageName.equals(HookConstant.HOOK_PACKAGE_NAME)) {
            if (HookPackage.getLocalConfigBoolean(String.valueOf(R.id.hide_follow_recommend))) {
                resparam.res.setReplacement(HookConstant.HOOK_PACKAGE_NAME, "dimen", "feed_title_specialbg_width", new XResources.DimensionReplacement(0, TypedValue.COMPLEX_UNIT_DIP));
                resparam.res.hookLayout(HookConstant.HOOK_PACKAGE_NAME, "layout", "story_feed_horiz_photo_list", new XC_LayoutInflated() {
                    @Override
                    public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                        liparam.view.setVisibility(View.GONE);
                    }
                });
            }
        }

    }
}
