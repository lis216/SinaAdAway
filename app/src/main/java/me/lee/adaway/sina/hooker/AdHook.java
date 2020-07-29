package me.lee.adaway.sina.hooker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.LogUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdHook extends BaseHook {

    private Boolean skipStartActivityAd;
    private Boolean hideContentAd;
    private Boolean cancelHideContentHot;
    private Boolean hideDetailAd;
    private Boolean hideDetailShare;
    private Boolean hideCommentAd;

    @Override
    protected void initConfig() {
        skipStartActivityAd = config.getBoolean(String.valueOf(R.id.skip_start_activity_ad));
        hideContentAd = config.getBoolean(String.valueOf(R.id.hide_content_ad));
        hideDetailAd = config.getBoolean(String.valueOf(R.id.hide_detail_ad));
        hideDetailShare = config.getBoolean(String.valueOf(R.id.hide_detail_share));
        hideCommentAd = config.getBoolean(String.valueOf(R.id.hide_comment_ad));
        cancelHideContentHot = config.getBoolean(String.valueOf(R.id.cancel_hide_content_hot));
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (skipStartActivityAd) skipStartActivityAd();
        if (hideContentAd) hideContentAd();
        if (hideDetailAd) hideDetailAd();
        if (hideDetailShare) hideDetailShare();
        if (hideCommentAd) hideCommentAd();
    }

    private void skipStartActivityAd() {
        HookUtil.findAndHookMethod("com.weibo.mobileads.util.AdUtil", loader, "checkPermission", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
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
    }

    private List removeAd(ArrayList arrayList) {
        List list = new ArrayList();
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (!shouldRemove(obj)) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    private Boolean shouldRemove(Object mblog) {
        if (isPromotion(mblog)) return true;
        String text = (String) XposedHelpers.getObjectField(mblog, "text");
//        if (text != null) {
//            if (checkText(text, content_keyword)) return true;
//        }

        Object user = XposedHelpers.getObjectField(mblog, "user");
//        if (user != null) {
//            String name = (String) XposedHelpers.getObjectField(user, "screen_name");
//            if (checkText(name, user_keyword)) return true;
//        }

        Object retweeted = XposedHelpers.getObjectField(mblog, "retweeted_status");
        if (retweeted != null) {
            if (shouldRemove(retweeted)) return true;
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
            HookUtil.findAndHookConstructor("com.sina.weibo.feed.j.c", loader, JSONObject.class, new XC_MethodHook() {
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

        } catch (Exception e) {
            LogUtil.log(e.getMessage());
        }
    }
}
