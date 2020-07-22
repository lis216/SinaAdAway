package me.lee.adaway.sina;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.utils.HookUtil;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static me.lee.adaway.sina.utils.HookUtil.*;

public class HookInit implements IXposedHookLoadPackage {

    /**
     * 查看是否激活
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

    private void setHeaderTips(ClassLoader loader) {
        findAndHookMethod(HookConstant.MODULE_PACKAGE_NAME + ".BaseAppCompatActivity", loader, "setHeaderTips", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String text = "微博版本: 未安装!";
                if(HookUtil.isPackageInstalled(getCurrentApplication(), HookConstant.HOOK_PACKAGE_NAME)){
                    String versionName = HookUtil.getVersionName(HookConstant.HOOK_PACKAGE_NAME);
                    text = "微博版本: v" + versionName;
                    if(HookPackage.isSupport(versionName)){
                        text = text + " 已适配!";
                    } else {
                        text = text + " 未适配, 部分功能可能无效!";
                    }
                }
                param.args[0] = text;

            }
        });
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(HookConstant.MODULE_PACKAGE_NAME)) {
            setModuleActive(lpparam.classLoader);
            setHeaderTips(lpparam.classLoader);
        }


        if (lpparam.packageName.equals(HookConstant.HOOK_PACKAGE_NAME)) {

        }
    }

}
