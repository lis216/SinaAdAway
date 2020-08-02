package me.lee.adaway.sina;

import com.alibaba.fastjson.JSONObject;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.hooker.AdHook;
import me.lee.adaway.sina.hooker.HomePageHook;
import me.lee.adaway.sina.hooker.MblogHook;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.FileUtil;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.HttpUtil;
import me.lee.adaway.sina.utils.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class HookPackage {

    private static boolean enabled = true;
    private static JSONObject versionEnableConfig = null;
    private static JSONObject versionConfig = null;
    private static String notice = null;
    private static Integer lastVersionCode = null;
    private static String lastVersionName = null;
    private static String remoteConfigUrl = "https://gitee.com/lis216/document/raw/master/SinaAdAway/config.json";
    private static List<String> versionList = new ArrayList<>();
    private static String versionName;
    private static int versionCode;
    private static ClassLoader loader;
    private static WeakReference<JSONObject> mConfig = new WeakReference<>(null);
    private List<BaseHook> hooks = new ArrayList<>();

    private HookPackage() {
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

    public static boolean initPackage() {
        try {
            String remoteConfigStr = HttpUtil.getPageContent(remoteConfigUrl);
            if (StringUtil.isNotEmpty(remoteConfigStr)) {
                JSONObject remoteConfigJson = JSONObject.parseObject(remoteConfigStr);
                if (remoteConfigJson == null) {
                    return false;
                }
                enabled = remoteConfigJson.getBoolean("enabled") == null ? true : remoteConfigJson.getBoolean("enabled");
                lastVersionCode = remoteConfigJson.getInteger("lastVersionCode");
                lastVersionName = remoteConfigJson.getString("lastVersionName");
                versionEnableConfig = remoteConfigJson.getJSONObject("versionEnableConfig");
                versionConfig = remoteConfigJson.getJSONObject("versionConfig");
                notice = remoteConfigJson.getString("notice");
                if (remoteConfigJson != null && StringUtil.isNotEmpty(remoteConfigJson.getString("supportVersion"))) {
                    String[] versionArr = remoteConfigJson.getString("supportVersion").split(",");
                    for (String version : versionArr) {
                        versionList.add(version.replaceAll(" ", ""));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
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
        hooks.add(new MblogHook());
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

    public static Integer getLastVersionCode() {
        return lastVersionCode;
    }

    public static String getLastVersionName() {
        return lastVersionName;
    }

    public static String isPerfectSupport(String versionName) {
        if (versionConfig == null) {
            return "未获取到插件版本信息";
        }
        if (versionConfig.getString(String.valueOf(BuildConfig.VERSION_CODE)).equals(versionName)) {
            return "最佳适配";
        } else {
            if (versionList == null || versionList.size() == 0) {
                return "非最佳适配:未获取到已适配版本信息";
            } else {
                if (versionList.contains(versionName)) {
                    return "非最佳适配:请下载对应版本插件";
                } else {
                    return "非最佳适配:请等待作者适配";
                }
            }

        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isVersionEnabled() {
        if (versionEnableConfig == null) {
            return true;
        }
        if (versionEnableConfig.get(String.valueOf(BuildConfig.VERSION_CODE)) != null) {
            return versionEnableConfig.getBoolean(String.valueOf(BuildConfig.VERSION_CODE));
        } else {
            return false;
        }

    }

    public static String getNotice() {
        return notice;
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
