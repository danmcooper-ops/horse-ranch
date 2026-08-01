package com.ranchgame;

import com.badlogic.gdx.Game;
import com.ranchgame.screens.RanchScreen;

/** Horse Ranch: ride around the ranch and jump the course. */
public class HorseGame extends Game {

    /**
     * Debug aid (desktop only): when set, the game renders for
     * {@link #screenshotDelay} seconds, writes a PNG here and exits.
     * Never set on the web build.
     */
    public static String screenshotPath;
    public static float screenshotDelay = 2.5f;

    @Override
    public void create() {
        setScreen(new RanchScreen());
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        super.dispose();
    }
}
