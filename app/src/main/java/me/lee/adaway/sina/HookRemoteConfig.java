package me.lee.adaway.sina;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HookRemoteConfig implements Serializable {
    private boolean enabled = false;
    private JSONObject versionEnableConfig = null;
    private JSONObject versionConfig = null;
    private String notice = null;
    private Integer lastVersionCode = null;
    private String lastVersionName = null;
    public static String remoteConfigUrl = "https://gitee.com/lis216/document/raw/master/SinaAdAway/config.json";
    private List<String> versionList = new ArrayList<>();

    public String isPerfectSupport(String versionName) {
        if (versionConfig == null) {
            return "未获取到插件版本信息";
        }
        if (BuildConfig.VERSION_CODE > lastVersionCode) {
            return "测试版本";
        }
        if (!isVersionEnabled()) {
            return "此版本插件已失效,请下载最新版";
        }
        if (versionConfig.getString(String.valueOf(BuildConfig.VERSION_CODE)) != null && versionConfig.getString(String.valueOf(BuildConfig.VERSION_CODE)).equals(versionName)) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVersionEnabled() {
        if (versionEnableConfig == null) {
            return true;
        }
        if (versionEnableConfig.get(String.valueOf(BuildConfig.VERSION_CODE)) != null) {
            return versionEnableConfig.getBoolean(String.valueOf(BuildConfig.VERSION_CODE));
        } else {
            return false;
        }

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public JSONObject getVersionEnableConfig() {
        return versionEnableConfig;
    }

    public void setVersionEnableConfig(JSONObject versionEnableConfig) {
        this.versionEnableConfig = versionEnableConfig;
    }

    public JSONObject getVersionConfig() {
        return versionConfig;
    }

    public void setVersionConfig(JSONObject versionConfig) {
        this.versionConfig = versionConfig;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public Integer getLastVersionCode() {
        return lastVersionCode;
    }

    public void setLastVersionCode(Integer lastVersionCode) {
        this.lastVersionCode = lastVersionCode;
    }

    public String getLastVersionName() {
        return lastVersionName == null ? "获取失败" : lastVersionName;
    }

    public void setLastVersionName(String lastVersionName) {
        this.lastVersionName = lastVersionName;
    }

    public List<String> getVersionList() {
        return versionList;
    }

    public void setVersionList(List<String> versionList) {
        this.versionList = versionList;
    }
}
