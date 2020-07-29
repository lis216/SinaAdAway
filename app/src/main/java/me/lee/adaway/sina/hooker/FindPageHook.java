package me.lee.adaway.sina.hooker;

import de.robv.android.xposed.XC_MethodHook;
import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;
import me.lee.adaway.sina.utils.HookUtil;
import org.json.JSONObject;

import static me.lee.adaway.sina.utils.HookUtil.showToast;

public class FindPageHook extends BaseHook {

    private Boolean hideFindPageCarousel;

    @Override
    protected void initConfig() {
        hideFindPageCarousel = config.getBoolean(String.valueOf(R.id.hide_find_page_carousel));
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (hideFindPageCarousel) hideFindPageCarousel();
    }

    private void hideFindPageCarousel() {
        HookUtil.findAndHookConstructor("com.sina.weibo.models.CardList", loader, JSONObject.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                showToast("调用了");
            }
        });
    }
}
