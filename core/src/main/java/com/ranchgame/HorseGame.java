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
    /** Debug aid: open the customize console immediately at startup. */
    public static boolean openConsoleOnStart;
    /** Debug aid: force appearance indices (coat,mane,tack,pad,shirt,pants,hair). */
    public static int[] presetLook;
    /**
     * True when the platform launcher detected a touch device (set by the
     * web launcher via navigator.maxTouchPoints; the backend's own
     * peripheral query is unreliable there).
     */
    public static boolean touchDevice;

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
