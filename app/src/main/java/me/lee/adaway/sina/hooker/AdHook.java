package me.lee.adaway.sina.hooker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

public class AdHook extends BaseHook {

    private Boolean removeAllAd;
    private Boolean cancelHideContentHot;
    private Boolean hideDetailShare;
    private Boolean hideFindPageCarousel;
    private Boolean hideFindPageNav;
    private Boolean hideCommentFollowCard;

    @Override
    protected void initConfig() {
        removeAllAd = getBoolean(R.id.remove_all_ad);
        hideDetailShare = getBoolean(R.id.hide_detail_share);
        cancelHideContentHot = getBoolean(R.id.cancel_hide_content_hot);
        hideFindPageCarousel = getBoolean(R.id.hide_find_page_carousel);
        hideFindPageNav = getBoolean(R.id.hide_find_page_nav);
        hideCommentFollowCard = getBoolean(R.id.hide_comment_follow_card);
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (removeAllAd) {
            skipStartActivityAd();
            hideContentAd();
            hideDetailAd();
            hideCommentAd();
            hideVideoPageAd();
            hideSVSAd();
        }
        if (hideCommentFollowCard) hideCommentFollowCard();
        if (hideDetailShare) hideDetailShare();
    }

    private void skipStartActivityAd() {
        HookUtil.findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "checkPermission", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });
        HookUtil.findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "setAds", List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = Collections.EMPTY_LIST;
            }
        });
        HookUtil.findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "getAds", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(Collections.EMPTY_LIST);
            }
        });
        HookUtil.findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "getFlashAd", Context.class, List.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });
        HookUtil.findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "getFlashAdInfo", String.class, Context.class, List.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });
    }

    private void hideContentAd() {
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
                param.args[0] = removeAd(arrayList);
            }
        });
        HookUtil.findAndHookMethod(className, loader, "getStatuses", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                ArrayList arrayList = (ArrayList) param.getResult();
                param.setResult(removeAd(arrayList));

            }
        });
        HookUtil.findAndHookMethod(className, loader, "getStatusesCopy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                ArrayList arrayList = (ArrayList) param.getResult();
                param.setResult(removeAd(arrayList));

            }
        });
        HookUtil.findAndHookMethod(className, loader, "insetTrend", replaceNull());

        try {

            // 话题头回调显示
            //Class CardList = loader.loadClass("com.sina.weibo.models.CardList");
            //HookUtil.findAndHookMethod("com.sina.weibo.page.SearchResultActivity", loader, "netCallback", String.class, CardList, replaceNull());
            //HookUtil.findAndHookMethod("com.sina.weibo.page.NewCardListActivity", loader, "netCallback", String.class, CardList, replaceNull());
            // 卡片初始化  在此移除会导致空指针
//            HookUtil.findAndHookMethod("com.sina.weibo.card.model.CardMblog", loader, "initFromJsonObject", JSONObject.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    JSONObject json = (JSONObject) param.args[0];
//                    JSONObject optJSONObject = json.optJSONObject("mblog");
//                    if (shouldRemoveCardMblog(optJSONObject)) {
//                        param.args[0] = null;
//                        showToast("被移除");
//                    }
//                }
//            });
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
                                    JSONObject optJSONObject = card.optJSONObject("mblog");
                                    if (!shouldRemoveCardMblog(optJSONObject)) {
                                        newJsonArray.put(card);
                                    }
                                } else if (cardType == 11) {
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
        } catch (Exception e) {
        }

    }

    private List removeAd(ArrayList arrayList) {
        List list = new ArrayList();
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                String text = (String) XposedHelpers.getObjectField(obj, "text");
                if (!shouldRemoveStatus(obj)) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    private Boolean shouldRemoveStatus(Object mblog) {
        if (isPromotion(mblog)) return true;

        int mblogtype = XposedHelpers.getIntField(mblog, "mblogtype");
        if (mblogtype != 0) {
            return true;
        }

        String text = (String) XposedHelpers.getObjectField(mblog, "text");
        if (StringUtil.isNotEmpty(text)) {
            if (checkText(text, config.getString(String.valueOf(R.id.filter_content_key_word)))) return true;
        }

        Object user = XposedHelpers.getObjectField(mblog, "user");
        if (user != null) {
            String name = (String) XposedHelpers.getObjectField(user, "screen_name");
            if (checkText(name, config.getString(String.valueOf(R.id.filter_user_key_word)))) return true;
        }

        Object retweeted = XposedHelpers.getObjectField(mblog, "retweeted_status");
        if (retweeted != null) {
            if (shouldRemoveStatus(retweeted)) return true;
        }
        return false;
    }

    private Boolean shouldRemoveCardMblog(JSONObject cardMblog) {
        if (cardMblog == null) return false;
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

        String text = cardMblog.optString("text");
        if (StringUtil.isNotEmpty(text)) {
            if (checkText(text, config.getString(String.valueOf(R.id.filter_content_key_word)))) return true;
        }

        JSONObject user = cardMblog.optJSONObject("user");
        if (user != null) {
            String name = user.optString("screen_name");
            if (checkText(name, config.getString(String.valueOf(R.id.filter_user_key_word)))) return true;
        }

        if (!cardMblog.isNull("retweeted_status")) {
            JSONObject retweeted = cardMblog.optJSONObject("retweeted_status");
            if (shouldRemoveCardMblog(retweeted)) return true;
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

    private Boolean checkText(String str, String keyWords) {
        if (StringUtil.isEmpty(keyWords)) {
            return false;
        }
        String[] keyArr = keyWords.split(",");
        if (keyArr.length <= 0) {
            return false;
        }
        for (String strTemp : keyArr) {
            if (str.indexOf(strTemp) != -1) {
                return true;
            }
        }
        return false;
    }


    private void hideDetailAd() {
        /**
         * 直接创建的时候设为不可见
         */
        HookUtil.findAndHookConstructor("com.sina.weibo.page.view.TrendsView", loader, Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                RelativeLayout relativeLayout = (RelativeLayout) param.thisObject;
                relativeLayout.setVisibility(View.GONE);
            }
        });
        HookUtil.findAndHookConstructor("com.sina.weibo.page.view.TrendsView", loader, Context.class, AttributeSet.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                RelativeLayout relativeLayout = (RelativeLayout) param.thisObject;
                relativeLayout.setVisibility(View.GONE);
            }
        });

        /**
         * 微博详情里创建时隐藏
         */
        try {
            Class Trend = loader.loadClass("com.sina.weibo.models.Trend");
            HookUtil.findAndHookMethod("com.sina.weibo.feed.view.DetailWeiboHeaderView", loader, "a", Trend, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    final Object obj = param.thisObject;
                    Class TrendsView = loader.loadClass("com.sina.weibo.page.view.TrendsView");
                    hideView(obj, TrendsView, "bt");

                }
            });
        } catch (Exception e) {

        }

    }


    private void hideDetailShare() {
        HookUtil.findAndHookMethod("com.sina.weibo.feed.view.DetailWeiboHeaderView", loader, "ak", replaceNull());
    }

    private void hideCommentAd() {
        try {
            //评论网络请求
            /*Class  paramClass = loader.loadClass("com.sina.weibo.feed.m.e");
            findAndHookMethod("com.sina.weibo.feed.business.j", loader, "a", paramClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    showToast("进来了");
                    Class s = loader.loadClass("com.sina.weibo.feed.j.s");
                }

            });*/
            HookUtil.findAndHookMethod("com.sina.weibo.mpc.models.CommentDataMPC", loader, "New", JSONObject.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    JSONObject jsonObject = (JSONObject) param.args[0];
                    if (jsonObject != null) {
                        if (jsonObject.getInt("type") != 0) {
                            param.args[0] = null;
                        }
                    }
                }
            });
            HookUtil.findAndHookMethod("com.sina.weibo.view.CommonLoadMoreView", loader, "a", Context.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LinearLayout view = getObject(param.thisObject, LinearLayout.class, "e");
                    view.setVisibility(View.GONE);
                }
            });
//            HookUtil.findAndHookConstructor("com.sina.weibo.feed.j.c", loader, JSONObject.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                    JSONObject jsonObject = (JSONObject) param.args[0];
//                    if (jsonObject != null) {
//                        if (jsonObject.getInt("type") != 0) {
//                            param.args[0] = null;
//                        }
//                    }
//                }
//            });

        } catch (Exception e) {

        }
    }

    private void hideCommentFollowCard() {
        HookUtil.findAndHookMethod("com.sina.weibo.feed.list.e", loader, "Y", replaceNull());
    }

    private void hideVideoPageAd() {
        try {
            Class ExpandableBannerView = loader.loadClass("com.sina.weibo.video.detail2.view.ExpandableBannerView");
            Class ExpandableInfo = loader.loadClass("com.sina.weibo.models.MBlogListObject$ExpandableInfo");
            HookUtil.findAndHookMethod("com.sina.weibo.video.detail2.view.a", loader, "a", ExpandableInfo, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    hideView(param.thisObject, ExpandableBannerView, "d");
                    hideView(param.thisObject, ExpandableBannerView, "e");
                }
            });
        } catch (Exception e) {

        }

    }

    private void hideSVSAd() {
        // 短视频请求地址
        // tiny_ranklist_home 榜单  // tiny_ranklist_hots 热门  //tiny_stream_video_list推荐
        HookUtil.findAndHookMethod("com.sina.weibo.story.stream.request.get.SVSRecommendListRequest", loader, "parse", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] != null) {
                    JSONObject jsonObject = new JSONObject((String) param.args[0]);
                    JSONArray jsonArray = jsonObject.optJSONArray("statuses");
                    JSONArray newJsonArray = new JSONArray();
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject status = jsonArray.optJSONObject(i);
                            if (status.isNull("ad_state") || status.optInt("ad_state") != 1) {
                                String text = status.optString("text");
                                if (StringUtil.isNotEmpty(text)) {
                                    if (!checkText(text, config.getString(String.valueOf(R.id.filter_content_key_word)))) {
                                        newJsonArray.put(status);
                                    }
                                }

                            }
                        }
                        jsonObject.put("statuses", newJsonArray);
                    }
                    param.args[0] = jsonObject.toString();
                }
            }
        });
    }
}
