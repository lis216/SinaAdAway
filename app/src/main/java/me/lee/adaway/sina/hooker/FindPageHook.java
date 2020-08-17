package me.lee.adaway.sina.hooker;

import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class FindPageHook extends BaseHook {


    private Boolean hideFindPageCarousel;
    private Boolean hideFindPageNav;
    private Boolean hideHotSearchShopping;

    @Override
    protected void initConfig() {
        hideFindPageCarousel = getBoolean(R.id.hide_find_page_carousel);
        hideFindPageNav = getBoolean(R.id.hide_find_page_nav);
        hideHotSearchShopping = getBoolean(R.id.hide_hot_search_shopping);
    }

    @Override
    protected void hookMain() {
        loadConfig();
        hideFindPageCard();
        hideHotSearchShopping();
    }

    private void hideFindPageCard() {
        HookUtil.findAndHookMethod("com.sina.weibo.models.CardList", loader, "initFromJsonObject", JSONObject.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                JSONObject json = (JSONObject) param.args[0];
                JSONArray jsonArray = json.getJSONArray("cards");
                JSONArray newJsonArray = new JSONArray();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject card = jsonArray.getJSONObject(i);
                        if (card != null) {
                            if (card.isNull("card_type")) {
                                newJsonArray.put(card);
                                continue;
                            }
                            int cardType = card.getInt("card_type");
                            if (cardType == 11) {
                                if (!card.isNull("show_type") && !card.isNull("card_group")) {
                                    int showType = card.getInt("show_type");
                                    JSONArray cardGroup = card.getJSONArray("card_group");
                                    JSONArray newCardGroup = new JSONArray();
                                    if (showType == 3 && cardGroup.length() == 2) {
                                        if (!hideFindPageNav && cardGroup.getJSONObject(0).getInt("card_type") == 19 && !cardGroup.getJSONObject(0).isNull("posid")) {
                                            newCardGroup.put(cardGroup.getJSONObject(0));
                                        }
                                        if (!hideFindPageCarousel && cardGroup.getJSONObject(1).getInt("card_type") == 118) {
                                            newCardGroup.put(cardGroup.getJSONObject(1));
                                        }
                                        if (!hideFindPageNav || !hideFindPageCarousel) {
                                            card.put("card_group", newCardGroup);
                                            newJsonArray.put(card);
                                        }
                                    } else {
                                        newJsonArray.put(card);
                                    }
                                } else {
                                    newJsonArray.put(card);
                                }
                            } else {
                                newJsonArray.put(card);
                            }
                        }
                    }
                }
                json.put("cards", newJsonArray);
                param.args[0] = json;
            }
        });
    }

    private void hideHotSearchShopping() {
        HookUtil.findAndHookMethod("com.sina.weibo.models.Page", loader, "parse", JSONObject.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                JSONObject jsonParam = (JSONObject) param.args[0];
                JSONArray cards = jsonParam.optJSONArray("cards");
                JSONArray newCards = new JSONArray();
                if (cards != null) {
                    for (int i = 0; i < cards.length(); i++) {
                        JSONObject card = cards.getJSONObject(i);
                        if (card != null) {
                            JSONArray cardGroup = card.optJSONArray("card_group");
                            if (cardGroup != null && cardGroup.length() > 0) {
                                JSONObject childCard = cardGroup.getJSONObject(0);
                                if (hideHotSearchShopping) {
                                    if (!childCard.optString("desc").equals("微博潮物")) {
                                        newCards.put(card);
                                    }
                                } else {
                                    newCards.put(card);
                                }
                            } else {
                                newCards.put(card);
                            }
                        }
                    }
                }
                jsonParam.put("cards", newCards);
                JSONObject pageInfo = jsonParam.optJSONObject("pageInfo");
                if (pageInfo != null) {
                    JSONArray toolBars = pageInfo.optJSONArray("toolbar_menus");
                    JSONArray newToolBars = new JSONArray();
                    if (toolBars != null && toolBars.length() > 0) {
                        for (int i = 0; i < toolBars.length(); i++) {
                            JSONObject toolBar = toolBars.optJSONObject(i);
                            if (hideHotSearchShopping) {
                                if (toolBar != null && (!"潮物".equals(toolBar.optString("desc")) && toolBar.optString("pic").indexOf("morden_goods.png") < 0)) {
                                    newToolBars.put(toolBar);
                                }
                            } else {
                                newToolBars.put(toolBar);
                            }
                        }
                        pageInfo.put("toolbar_menus", newToolBars);
                    }
                }
            }
        });
    }
}
