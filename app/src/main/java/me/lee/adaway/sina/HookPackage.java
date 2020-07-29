package me.lee.adaway.sina;

import com.alibaba.fastjson.JSONObject;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.hooker.AdHook;
import me.lee.adaway.sina.hooker.FindPageHook;
import me.lee.adaway.sina.hooker.HomePageHook;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.FileUtil;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.HttpUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class HookPackage {

    private static String versionUrl = "https://gitee.com/lis216/document/raw/master/SinaAdAway/SurportVersions";
    private static List<String> suportVersion = new ArrayList<>();
    private static String versionName;
    private static int versionCode;
    private static ClassLoader loader;
    private static WeakReference<JSONObject> mConfig = new WeakReference<>(null);
    private List<BaseHook> hooks = new ArrayList<>();

    private HookPackage() {
    }

    static {
        suportVersion.add("10.7.2");
    }

    static HookPackage getInstance() {
        return new HookPackage();
    }

    public static JSONObject getConfig() {
        JSONObject config = mConfig.get();
        if (config == null) {
            config = loadConfig();
            mConfig = new WeakReference<>(config);
        }
        return config;
    }

    public static boolean initSurportVersion() {
        String versionStr = HttpUtil.getPageContent(versionUrl);
        if (versionStr == null) {
            return false;
        }
        String[] versionArr = versionStr.split(",");
        for (String version : versionArr) {
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
        return getConfig().getBoolean(String.valueOf(R.id.active_module));
    }

    void hookHandler(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!isHookSwitchOpened()) {
            HookUtil.showToast("SinaAdAway已安装未启用!");
            return;
        }
        versionName = HookUtil.getVersionName(HookConstant.HOOK_PACKAGE_NAME);
        versionCode = HookUtil.getVersionCode(HookConstant.HOOK_PACKAGE_NAME);
        loader = lpparam.classLoader;
        startAllHook();
    }

    private void startAllHook() {
        hooks.clear();
        hooks.add(new AdHook());
        hooks.add(new HomePageHook());
        hooks.add(new FindPageHook());
        for (BaseHook hook : hooks) {
            hook.startHook();
        }
    }

    public static String getVersionName() {
        return versionName;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static ClassLoader getLoader() {
        return loader;
    }

    public static JSONObject loadConfig() {
        JSONObject config = new JSONObject();
        String configStr = FileUtil.readFile(FileUtil.getSdDataDir() + "/config/config.json");
        if (configStr != null && configStr.length() > 0) {
            config = JSONObject.parseObject(configStr);
        }
        return config;
    }

    public static void saveConfig() {
        JSONObject config = getConfig();
        FileUtil.writeFile(FileUtil.getSdDataDir() + "/config/", "config.json", JSONObject.toJSONString(config), false);
    }
}
