package me.lee.adaway.sina.hooker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import me.lee.adaway.sina.MainActivity;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserCenterHook extends BaseHook {

    private Boolean hideSetting;
    private Boolean hideVisit;
    private Boolean hideChaohua;
    private Boolean hidePublicwelfare;
    private Boolean hideExamination;
    private Boolean hideDailv;
    private Boolean hideDraft;
    private Boolean cancelHideSport;
    private Boolean hideMemberIcon;
    private Boolean hideSafety;
    private Boolean hideHongbaook;
    private Boolean hideRecommend;

    @Override
    protected void initConfig() {
        hideSetting = getBoolean(R.id.hide_setting);
        hideVisit = getBoolean(R.id.hide_visit);
        hideChaohua = getBoolean(R.id.hide_chaohua);
        hidePublicwelfare = getBoolean(R.id.hide_publicwelfare);
        hideExamination = getBoolean(R.id.hide_examination);
        hideDailv = getBoolean(R.id.hide_dailv);
        hideDraft = getBoolean(R.id.hide_draft);
        cancelHideSport = getBoolean(R.id.cancel_hide_sport);
        hideMemberIcon = getBoolean(R.id.hide_member_icon);
        hideSafety = getBoolean(R.id.hide_safety);
        hideHongbaook = getBoolean(R.id.hide_hongbaook);
        hideRecommend = getBoolean(R.id.hide_recommend);
    }

    @Override
    protected void hookMain() {
        try {
            loadConfig();
            HookUtil.findAndHookMethod("com.sina.weibo.card.model.CardGroup", loader, "initFromJsonObject", JSONObject.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    JSONObject card = (JSONObject) param.args[0];
                    boolean hide = false;
                    if (!card.isNull("card_type") && card.getInt("card_type") == 11) {
                        if (!card.isNull("card_group")) {
                            JSONArray cardGroup = card.getJSONArray("card_group");
                            if (hideSetting && cardGroup.length() == 1) {
                                if (cardGroup.getJSONObject(0).getInt("card_type") == 19) {
                                    hide = true;
                                }
                            }
                        }
                        if (!card.isNull("cardid")) {
                            switch (card.getString("cardid")) {
                                case "100505_-_recentlyuser":
                                    if (hideVisit) hide = true;
                                    break;
                                case "100505_-_publicwelfare":
                                    if (hidePublicwelfare) hide = true;
                                    break;
                                case "100505_-_examination":
                                    if (hideExamination) hide = true;
                                    break;
                                case "100505_-_2020dailv":
                                    if (hideDailv) hide = true;
                                    break;
                                case "100505_-_chaohua":
                                    if (hideChaohua) hide = true;
                                    break;
                                case "100505_-_managecard":
                                    if (hideDraft) hide = true;
                                    break;
                                case "100505_-_draft":
                                    JSONArray newCardGroup = new JSONArray();
                                    JSONArray cardGroup = card.getJSONArray("card_group");
//                                        for (int j = 0; j < cardGroup.length(); j++) {
//                                            JSONObject cardTemp = cardGroup.getJSONObject(j);
//                                            if ("100505_-_sport".equals(cardTemp.getString("cardid"))) {
//                                                newCardGroup.put(cardTemp);
//                                            }
//                                        }
                                    if (cancelHideSport) {
                                        String sport = "{\n" +
                                                "\"card_type\": 4 ,\n" +
                                                "\"itemid\": \"100505_-_sport\" ,\n" +
                                                "\"cardid\": \"100505_-_sport\" ,\n" +
                                                "\"display_arrow\": 1 ,\n" +
                                                "\"bold\": 1 ,\n" +
                                                "\"title\": \"微博运动\" ,\n" +
                                                "\"desc\": \"微博运动\" ,\n" +
                                                "\"desc_extr\": \"运动红包天天抽\" ,\n" +
                                                "\"card_type_name\": \"微博运动\" ,\n" +
                                                "\"unread_id\": \"health\" ,\n" +
                                                "\"pic\": \"https:\\/\\/h5.sinaimg.cn\\/upload\\/100\\/888\\/2020\\/04\\/13\\/mine_icon_sport.png\" ,\n" +
                                                "\"scheme\": \"sinaweibo:\\/\\/healthcenter\" ,\n" +
                                                "\"actionlog\":  {\n" +
                                                "\"act_code\": \"2265\" ,\n" +
                                                "\"ext\": \"usertype:9000\" ,\n" +
                                                "\"cardid\": \"100505_-_sport\" ,\n" +
                                                "\"fid\": \"1005055338737670_-_sport\"\n" +
                                                "},\n" +
                                                "\"openurl\": \"\"\n" +
                                                "}";
                                        JSONObject sportCard = new JSONObject(sport);
                                        newCardGroup.put(sportCard);
                                    }
                                    //添加模块入口
                                    String setting = "{\n" +
                                            "\"card_type\": 4 ,\n" +
                                            "\"itemid\": \"100505_-_SinaAdAway\" ,\n" +
                                            "\"cardid\": \"100505_-_SinaAdAway\" ,\n" +
                                            "\"display_arrow\": 1 ,\n" +
                                            "\"bold\": 1 ,\n" +
                                            "\"title\": \"个性化设置\" ,\n" +
                                            "\"desc\": \"个性化设置\" ,\n" +
                                            "\"desc_extr\": \"SinaAdAway模块设置\" ,\n" +
                                            "\"card_type_name\": \"个性化设置\" ,\n" +
                                            "\"pic\": \"https://h5.sinaimg.cn/upload/1008/253/2018/08/07/compose_built_compose_icon_qa.png\" ,\n" +
                                            "\"openurl\": \"\"\n" +
                                            "}";

                                    JSONObject settingCard = new JSONObject(setting);
                                    newCardGroup.put(settingCard);
                                    card.put("card_group", newCardGroup);
                                    break;
                                case "100505_-_safety":
                                    if (hideSafety) {
                                        hide = true;
                                    }
                                    break;
                                case "100505_-_hongbaook":
                                    if (hideHongbaook) {
                                        hide = true;
                                    }
                                    break;
                                case "1005051004":
                                    if (hideRecommend) {
                                        hide = true;
                                    }
                                    break;
                            }
                        }
                    }
                    if (hide) {
                        param.args[0] = null;
                    } else {
                        param.args[0] = card;
                    }
                }
            });
            if (hideMemberIcon) {
                HookUtil.findAndHookMethod("com.sina.weibo.page.view.UserInfoHeaderView", loader, "g", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hideView(param.thisObject, LinearLayout.class, "E");
                    }
                });
            }
            Class UserInfoActivity = loader.loadClass("com.sina.weibo.page.UserInfoActivity");
            //添加事件
            HookUtil.findAndHookMethod("com.sina.weibo.page.UserInfoActivity$5", loader, "onItemClick", AdapterView.class, View.class, int.class, long.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Activity activity = getObject(param.thisObject, UserInfoActivity, "b");
                    Object view = param.args[1];
                    Object pageCardInfo = XposedHelpers.getObjectField(view, "h");
                    String itemid = (String) XposedHelpers.getObjectField(pageCardInfo, "itemid");
                    if ("100505_-_SinaAdAway".equals(itemid)) {
                        Intent intent = new Intent(HookConstant.MODULE_PACKAGE_NAME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(new ComponentName(HookConstant.MODULE_PACKAGE_NAME, MainActivity.class.getName()));
                        activity.startActivity(intent);
                    }
                }
            });


        } catch (Exception e) {

        }
    }
}
