package me.lee.adaway.sina;

import com.alibaba.fastjson.JSONObject;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.hooker.AdHook;
import me.lee.adaway.sina.hooker.HomePageHook;
import me.lee.adaway.sina.hooker.MblogHook;
import me.lee.adaway.sina.hooker.UserCenterHook;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.FileUtil;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.HttpUtil;
import me.lee.adaway.sina.utils.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static me.lee.adaway.sina.utils.HookUtil.showToast;

public final class HookPackage {

    private static String versionName;
    private static int versionCode;
    private static ClassLoader loader;
    private static WeakReference<JSONObject> localConfig = new WeakReference<>(null);
    private static WeakReference<HookRemoteConfig> remoteConfig = new WeakReference<>(null);
    private List<BaseHook> hooks = new ArrayList<>();

    private HookPackage() {
    }

    static HookPackage getInstance() {
        return new HookPackage();
    }

    public static JSONObject getLocalConfig() {
        JSONObject config = localConfig.get();
        if (config == null) {
            config = new JSONObject();
            String configStr = FileUtil.readFile(FileUtil.getSdDataDir() + "/config/config.json");
            if (configStr != null && configStr.length() > 0) {
                config = JSONObject.parseObject(configStr);
            }
            localConfig = new WeakReference<>(config);
        }
        return config;
    }

    public static HookRemoteConfig getRemoteConfig() {
        HookRemoteConfig config = remoteConfig.get();
        if (config == null) {
            config = loadRemoteConfig();
            remoteConfig = new WeakReference<>(config);
        }
        return config;
    }


    private boolean isHookSwitchOpened() {
        return getLocalConfig().getBoolean(String.valueOf(R.id.active_module));
    }

    void hookHandler(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!isHookSwitchOpened()) {
            showToast("SinaAdAway已安装未启用!");
            return;
        }
        versionName = HookUtil.getVersionName(HookConstant.HOOK_PACKAGE_NAME);
        versionCode = HookUtil.getVersionCode(HookConstant.HOOK_PACKAGE_NAME);
        loader = lpparam.classLoader;
        startAllHook();
    }

    private void startAllHook() {
        HookRemoteConfig remoteConfig = getRemoteConfig();
        if (!remoteConfig.isEnabled() || !remoteConfig.isVersionEnabled()) {
            return;
        }
        hooks.clear();
        hooks.add(new AdHook());
        hooks.add(new HomePageHook());
        hooks.add(new MblogHook());
        hooks.add(new UserCenterHook());
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


    public static String getLocalConfigString(String key) {
        JSONObject config = getLocalConfig();
        return config.getString(key);
    }

    public static Boolean getLocalConfigBoolean(String key) {
        JSONObject config = getLocalConfig();
        return config.getBoolean(key);
    }

    public static void putLocalConfig(String key, Object object) {
        JSONObject config = getLocalConfig();
        config.put(key, object);
        FileUtil.writeFile(FileUtil.getSdDataDir() + "/config/", "config.json", JSONObject.toJSONString(config), false);
        localConfig = new WeakReference<>(null);
    }

    public static void removeLocalConfig(String key) {
        JSONObject config = getLocalConfig();
        config.remove(key);
        FileUtil.writeFile(FileUtil.getSdDataDir() + "/config/", "config.json", JSONObject.toJSONString(config), false);
    }

    public static HookRemoteConfig loadRemoteConfig() {
        HookRemoteConfig remoteConfig = new HookRemoteConfig();
        try {
            String remoteConfigStr = HttpUtil.getPageContent(HookRemoteConfig.remoteConfigUrl);
            if (StringUtil.isNotEmpty(remoteConfigStr)) {
                JSONObject remoteConfigJson = JSONObject.parseObject(remoteConfigStr);
                if (remoteConfigJson == null) {
                    return remoteConfig;
                }
                remoteConfig.setEnabled(remoteConfigJson.getBoolean("enabled") == null ? true : remoteConfigJson.getBoolean("enabled"));
                remoteConfig.setLastVersionCode(remoteConfigJson.getInteger("lastVersionCode"));
                remoteConfig.setLastVersionName(remoteConfigJson.getString("lastVersionName"));
                remoteConfig.setVersionEnableConfig(remoteConfigJson.getJSONObject("versionEnableConfig"));
                remoteConfig.setVersionConfig(remoteConfigJson.getJSONObject("versionConfig"));
                remoteConfig.setNotice(remoteConfigJson.getString("notice"));
                List<String> versionList = new ArrayList<>();
                if (remoteConfigJson != null && StringUtil.isNotEmpty(remoteConfigJson.getString("supportVersion"))) {
                    String[] versionArr = remoteConfigJson.getString("supportVersion").split(",");
                    for (String version : versionArr) {
                        versionList.add(version.replaceAll(" ", ""));
                    }
                }
                remoteConfig.setVersionList(versionList);
            }
            return remoteConfig;
        } catch (Exception e) {
            return remoteConfig;
        }
    }
}
