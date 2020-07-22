package me.lee.adaway.sina;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.HttpUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HookPackage {

    private static String versionUrl = "https://gitee.com/lis216/document/raw/master/SinaAdAway/SurportVersions";

    public static List<String> suportVersion = new ArrayList<>();

    static {
        suportVersion.add("10.7.2");
    }

    public static String versionName;
    public static int versionCode;
    public static ClassLoader loader;
    public static WeakReference<XSharedPreferences> mSharedPrefs = new WeakReference<>(null);
    private List<BaseHook> hooks = new ArrayList<>();

    private HookPackage() {
    }


    static HookPackage getInstance() {
        return new HookPackage();
    }

    public static XSharedPreferences getXSharedPrefs() {
        XSharedPreferences prefs = mSharedPrefs.get();
        if (prefs == null) {
            prefs = new XSharedPreferences(HookConstant.MODULE_PACKAGE_NAME);
            mSharedPrefs = new WeakReference<>(prefs);
        }
        return prefs;
    }

    public static boolean initSurportVersion() {
        String versionStr = HttpUtil.getPageContent(versionUrl);
        if(versionStr == null){
            return false;
        }
        String[] versionArr = versionStr.split(",");
        for (String version : versionArr){
            suportVersion.add(version);
        }
        return true;
    }

    public static boolean isSupport(String versionName) {
        if (suportVersion.contains(versionName)) {
            return true;
        }
        return false;
    }

    private boolean isHookSwitchOpened() {
        return getXSharedPrefs().getBoolean("active_module", false);
    }

    void hookHandler(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!isHookSwitchOpened()) return;
        versionName = HookUtil.getVersionName(HookConstant.HOOK_PACKAGE_NAME);
        versionCode = HookUtil.getVersionCode(HookConstant.HOOK_PACKAGE_NAME);
        loader = lpparam.classLoader;
        startAllHook();
    }

    private void startAllHook() {
        hooks.clear();
        //hooks.add(new CommentListHook());
        for (BaseHook hook : hooks) {
            hook.startHook();
        }
    }

}
