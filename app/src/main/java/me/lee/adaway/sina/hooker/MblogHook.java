package me.lee.adaway.sina.hooker;

import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;

public class MblogHook extends BaseHook {

    private boolean hidePersonBackground;
    private boolean hidePersonHeadPendant;

    @Override
    protected void initConfig() {
        hidePersonBackground = config.getBoolean(String.valueOf(R.id.hide_person_background));
        hidePersonHeadPendant = config.getBoolean(String.valueOf(R.id.hide_person_head_pendant));
    }

    @Override
    protected void hookMain() {
        loadConfig();
        if (hidePersonHeadPendant) hidePersonHeadPendant();
        if (hidePersonBackground) hidePersonBackground();
    }

    private void hidePersonHeadPendant() {
        
    }

    private void hidePersonBackground() {

    }
}
