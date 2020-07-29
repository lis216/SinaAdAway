package me.lee.adaway.sina;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class HookInit implements IXposedHookLoadPackage {

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
            HookPackage.getInstance().hookHandler(lpparam);
        }
    }

}
