package me.lee.adaway.sina.hooker;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MblogHook extends BaseHook {

    private boolean hideMblogBackground;
    private boolean hideMblogHeadPendant;
    private boolean hideMblogButtons;
    private Boolean hideDetailShare;
    private Boolean hideCommentFollowCard;
    private Boolean cancelHideContentHot;
    private Boolean hideMblogAd;

    @Override
    protected void initConfig() {
        hideDetailShare = getBoolean(R.id.hide_detail_share);
        hideCommentFollowCard = getBoolean(R.id.hide_comment_follow_card);
        hideMblogBackground = getBoolean(R.id.hide_mblog_background);
        hideMblogHeadPendant = getBoolean(R.id.hide_mblog_head_pendant);
        hideMblogButtons = getBoolean(R.id.hide_mblog_buttons);
        cancelHideContentHot = getBoolean(R.id.cancel_hide_content_hot);
        hideMblogAd = getBoolean(R.id.hide_mblog_ad);
    }

    @Override
    protected void hookMain() {
        loadConfig();
        filterMblog();
        if (hideCommentFollowCard) hideCommentFollowCard();
        if (hideDetailShare) hideDetailShare();
    }

    private void filterMblog() {
        String className = "com.sina.weibo.models.MBlogListBaseObject";
        HookUtil.findAndHookMethod(className, loader, "getTrends", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.setResult(Collections.EMPTY_LIST);
            }
        });
        HookUtil.findAndHookMethod(className, loader, "setTrends", List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.args[0] = Collections.EMPTY_LIST;
            }
        });
        HookUtil.findAndHookMethod(className, loader, "setStatuses", List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                ArrayList arrayList = (ArrayList) param.args[0];
                param.args[0] = filter(arrayList);
            }
        });
        HookUtil.findAndHookMethod(className, loader, "getStatuses", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                ArrayList arrayList = (ArrayList) param.getResult();
                param.setResult(filter(arrayList));

            }
        });
        HookUtil.findAndHookMethod(className, loader, "getStatusesCopy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                ArrayList arrayList = (ArrayList) param.getResult();
                param.setResult(filter(arrayList));

            }
        });
        HookUtil.findAndHookMethod(className, loader, "insetTrend", replaceNull());

        // 话题头回调显示
        //Class CardList = loader.loadClass("com.sina.weibo.models.CardList");
        //HookUtil.findAndHookMethod("com.sina.weibo.page.SearchResultActivity", loader, "netCallback", String.class, CardList, replaceNull());
        //HookUtil.findAndHookMethod("com.sina.weibo.page.NewCardListActivity", loader, "netCallback", String.class, CardList, replaceNull());
        // 从JSON加载卡片  入参所有的卡片数据(热门 本地 话题 以及某个话题的卡片)
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
                            if (cardType == 9 || cardType == 126) {
                                JSONObject mblog = card.optJSONObject("mblog");
                                if (hideMblogBackground) {
                                    mblog.put("pic_bg_new", null);
                                    mblog.put("pic_bg_type", null);
                                }
                                if (hideMblogHeadPendant) {
                                    JSONObject user = mblog.optJSONObject("user");
                                    user.put("avatar_extend_info", null);
                                    mblog.put("user", user);
                                }
                                if (hideMblogButtons) {
                                    mblog.put("buttons", null);
                                }
                                if (!shouldRemoveCardMblog(mblog)) {
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

    private List filter(ArrayList arrayList) {
        List list = new ArrayList();
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                //隐藏背景
                if (hideMblogBackground && XposedHelpers.getObjectField(obj, "pic_bg_new") != null && StringUtil.isNotEmpty((String) XposedHelpers.getObjectField(obj, "pic_bg_new"))) {
                    XposedHelpers.setObjectField(obj, "pic_bg_new", null);
                    XposedHelpers.setObjectField(obj, "pic_bg_type", null);
                }
                //隐藏头像挂件
                Object user = XposedHelpers.getObjectField(obj, "user");
                if (user != null && XposedHelpers.getObjectField(user, "avatar_extend_info") != null) {
                    if (hideMblogHeadPendant) {
                        XposedHelpers.setObjectField(user, "avatar_extend_info", null);
                    }
                }
                //隐藏关注按钮
                if (hideMblogButtons) {
                    XposedHelpers.setObjectField(obj, "buttons", null);
                }

                if (!shouldRemoveStatus(obj)) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    private Boolean shouldRemoveStatus(Object mblog) {
        if (mblog == null) return false;

        if (hideMblogAd && isContentAd(mblog)) return true;

        String text = (String) XposedHelpers.getObjectField(mblog, "text");
        if (StringUtil.isNotEmpty(text)) {
            if (HookUtil.checkText(text, config.getString(String.valueOf(R.id.filter_content_key_word)))) return true;
        }

        Object user = XposedHelpers.getObjectField(mblog, "user");
        if (user != null) {
            String name = (String) XposedHelpers.getObjectField(user, "screen_name");
            if (HookUtil.checkText(name, config.getString(String.valueOf(R.id.filter_user_key_word)))) return true;
        }

        return false;
    }

    private Boolean isContentAd(Object mblog) {
        if (isPromotion(mblog)) return true;

        int mblogtype = XposedHelpers.getIntField(mblog, "mblogtype");
        if (mblogtype != 0) {
            return true;
        }

        Object retweeted = XposedHelpers.getObjectField(mblog, "retweeted_status");
        if (retweeted != null) {
            if (shouldRemoveStatus(retweeted)) return true;
        }
        return false;
    }

    private Boolean isPromotion(Object mblog) {
        Object promotion = XposedHelpers.getObjectField(mblog, "promotion");

        if (promotion != null) {
            String ad_type = (String) XposedHelpers.getObjectField(promotion, "adtype");
            if (!cancelHideContentHot) {
                return true;
            } else {
                return "8" != ad_type;
            }
        }
        return false;
    }

    private Boolean shouldRemoveCardMblog(JSONObject cardMblog) {
        if (cardMblog == null) return false;

        if (hideMblogAd && isCardMblogAd(cardMblog)) return true;

        String text = cardMblog.optString("text");
        if (StringUtil.isNotEmpty(text)) {
            if (HookUtil.checkText(text, config.getString(String.valueOf(R.id.filter_content_key_word)))) return true;
        }

        JSONObject user = cardMblog.optJSONObject("user");
        if (user != null) {
            String name = user.optString("screen_name");
            if (HookUtil.checkText(name, config.getString(String.valueOf(R.id.filter_user_key_word)))) return true;
        }


        return false;
    }

    private Boolean isCardMblogAd(JSONObject cardMblog) {
        if (!cardMblog.isNull("promotion")) {
            JSONObject promotion = cardMblog.optJSONObject("promotion");
            String ad_type = promotion.optString("adtype");
            if (!cancelHideContentHot) {
                return true;
            } else {
                if ("8" != ad_type) {
                    return true;
                }
            }
        }

        if (!cardMblog.isNull("mblogtype") && cardMblog.optInt("mblogtype") != 0) {
            return true;
        }

        if (!cardMblog.isNull("retweeted_status")) {
            JSONObject retweeted = cardMblog.optJSONObject("retweeted_status");
            if (shouldRemoveCardMblog(retweeted)) return true;
        }

        return false;
    }

    private void hideCommentFollowCard() {
        HookUtil.findAndHookMethod("com.sina.weibo.feed.list.e", loader, "Y", replaceNull());
    }

    private void hideDetailShare() {
        HookUtil.findAndHookMethod("com.sina.weibo.feed.view.DetailWeiboHeaderView", loader, "ak", replaceNull());
    }
}
