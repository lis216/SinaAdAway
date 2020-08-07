package me.lee.adaway.sina.hooker;

import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
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

    @Override
    protected void initConfig() {
        hideSetting = config.getBoolean(String.valueOf(R.id.hide_setting));
        hideVisit = config.getBoolean(String.valueOf(R.id.hide_visit));
        hideChaohua = config.getBoolean(String.valueOf(R.id.hide_chaohua));
        hidePublicwelfare = config.getBoolean(String.valueOf(R.id.hide_publicwelfare));
        hideExamination = config.getBoolean(String.valueOf(R.id.hide_examination));
        hideDailv = config.getBoolean(String.valueOf(R.id.hide_dailv));
        hideDraft = config.getBoolean(String.valueOf(R.id.hide_draft));
        cancelHideSport = config.getBoolean(String.valueOf(R.id.cancel_hide_sport));
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
                                case "100505_-_managecard":
                                    if (hideDraft) hide = true;
                                    break;
                                case "100505_-_draft":
                                    if (cancelHideSport) {
                                        JSONArray cardGroup = card.getJSONArray("card_group");
                                        JSONArray newCardGroup = new JSONArray();
                                        for (int j = 0; j < cardGroup.length(); j++) {
                                            JSONObject cardTemp = cardGroup.getJSONObject(j);
                                            if ("100505_-_sport".equals(cardTemp.getString("cardid"))) {
                                                newCardGroup.put(cardTemp);
                                            }
                                        }
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
//            HookUtil.findAndHookMethod("com.sina.weibo.card.model.PageCardInfo", loader, "initFromJsonObject", JSONObject.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    JSONObject json = (JSONObject) param.args[0];
//                }
//            });

        } catch (Exception e) {

        }
    }
}
