package me.lee.adaway.sina.hooker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class CommonAdHook extends BaseHook {

    private Boolean removeAllAd;

    @Override
    protected void initConfig() {
        removeAllAd = getBoolean(R.id.remove_all_ad);
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (removeAllAd) {
            skipStartActivityAd();
            hideDetailAd();
            hideCommentAd();
            hideVideoPageAd();
            hideSVSAd();
        }

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


    private void hideCommentAd() {
        HookUtil.findAndHookMethod("com.sina.weibo.mpc.models.CommentDataMPC", loader, "New", JSONObject.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                JSONObject jsonObject = (JSONObject) param.args[0];
                if (jsonObject != null) {
                    if (jsonObject.getInt("type") != 0) {
                        param.args[0] = null;
                    }
//                    else {
//                        // TODO 评论区过滤
//                        JSONObject data = jsonObject.optJSONObject("data");
//                        if (data != null) {
//                            String text = data.getString("text");
//                        }
//                    }
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
                                    if (!HookUtil.checkText(text, config.getString(String.valueOf(R.id.filter_content_key_word)))) {
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
