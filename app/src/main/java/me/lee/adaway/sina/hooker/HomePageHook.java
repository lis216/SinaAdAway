package me.lee.adaway.sina.hooker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.HookPackage;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import me.lee.adaway.sina.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static me.lee.adaway.sina.utils.HookUtil.showToast;

public class HomePageHook extends BaseHook {

    private Boolean hideTopHongbao;
    private Boolean autoSign;
    private Boolean hideHongbao;
    private Boolean autoGetHongbao;
    private Boolean hideFollowRecommend;

    private String signDate = "";
    private boolean close = false;

    @Override
    protected void initConfig() {
        hideTopHongbao = getBoolean(R.id.hide_top_hongbao);
        autoSign = getBoolean(R.id.auto_sign);
        hideHongbao = getBoolean(R.id.hide_hongbao);
        autoGetHongbao = getBoolean(R.id.auto_get_hongbao);
        hideFollowRecommend = getBoolean(R.id.hide_follow_recommend);
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (hideTopHongbao) hideTopHongbao();
        if (hideHongbao) hideFloatHongbao();
        if (autoGetHongbao) autoGetHongbao();
        if (hideFollowRecommend) hideFollowRecommend();
    }

    private void hideTopHongbao() {
        try {
            // 搜反编译 /* compiled from: FeedRedPacketCommonStrategy */
            Class FeedRedPacket = loader.loadClass("com.sina.weibo.models.FeedRedPacket");
            HookUtil.findAndHookMethod("com.sina.weibo.utils.bw", loader, "a", Context.class, ViewGroup.class, FeedRedPacket, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    ViewGroup viewGroup = (ViewGroup) param.args[1];
                    viewGroup.setVisibility(View.GONE);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    if (autoSign && !formatter.format(new Date()).equals(config.getString("signDate"))) {
                        signDate = formatter.format(new Date());
                        HookPackage.putLocalConfig("signDate", signDate);
                        loadConfig();
                        close = true;
                        viewGroup.callOnClick();
                    }
                }
            });

            HookUtil.findAndHookMethod("com.sina.weibo.view.CommonTitleBar", loader, "setButtonClickListener", View.OnClickListener.class, View.OnClickListener.class, View.OnClickListener.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    TextView view = getObject(param.thisObject, TextView.class, "c");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    if (close && formatter.format(new Date()).equals(config.getString("signDate"))) {
                        close = false;
                        new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    view.callOnClick();
                                } catch (InterruptedException e) {
                                }
                            }
                        }.start();
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.log(e);
        }

    }

    private void hideFloatHongbao() {
        HookUtil.findAndHookMethod("com.sina.weibo.floatingwindow.FloatingWindowManagerImp", loader, "genContainer", replaceNull());
    }

    private void autoGetHongbao() {
        HookUtil.findAndHookMethod("com.sina.weibo.feed.q.a", loader, "a", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                int i = (int) param.args[0];
                int type = getObject(param.thisObject, int.class, "s");
                // type  2:正在刷 3:完成
                if (type == 3) {
                    HookUtil.callMethod(param.thisObject, "d");
                    showToast("已自动领取红包!");
                }
            }
        });
    }

    private void hideFollowRecommend() {
        HookUtil.findAndHookMethod("com.sina.weibo.story.photo.StoryFeedComponent", loader, "showStoryList", List.class, replaceNull());
    }
}
