package me.lee.adaway.sina.hooker;

import android.widget.LinearLayout;
import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.LogUtil;
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
                    LogUtil.log(card.toString());
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
                                    if (cancelHideSport) {

                                        JSONArray newCardGroup = new JSONArray();
                                        JSONArray cardGroup = card.getJSONArray("card_group");
//                                        for (int j = 0; j < cardGroup.length(); j++) {
//                                            JSONObject cardTemp = cardGroup.getJSONObject(j);
//                                            if ("100505_-_sport".equals(cardTemp.getString("cardid"))) {
//                                                newCardGroup.put(cardTemp);
//                                            }
//                                        }
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
                                        JSONObject cardTemp = new JSONObject(sport);
                                        newCardGroup.put(cardTemp);
                                        card.put("card_group", newCardGroup);
                                    } else {
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

        } catch (Exception e) {

        }
    }
}
