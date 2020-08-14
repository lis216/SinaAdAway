package me.lee.adaway.sina.hooker;

import me.lee.adaway.sina.R;
import me.lee.adaway.sina.hooker.base.BaseHook;

public class MblogHook extends BaseHook {

    private boolean hidePersonBackground;
    private boolean hidePersonHeadPendant;

    @Override
    protected void initConfig() {
        hidePersonBackground = getBoolean(R.id.hide_person_background);
        hidePersonHeadPendant = getBoolean(R.id.hide_person_head_pendant);
    }

    @Override
    protected void hookMain() {
        if (hidePersonHeadPendant) hidePersonHeadPendant();
        if (hidePersonBackground) hidePersonBackground();
    }

    private void hidePersonHeadPendant() {

    }

    private void hidePersonBackground() {

    }
}
