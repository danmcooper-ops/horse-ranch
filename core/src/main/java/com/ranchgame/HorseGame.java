package com.ranchgame;

import com.badlogic.gdx.Game;
import com.ranchgame.screens.RanchScreen;

/** Horse Ranch: ride around the ranch and jump the course. */
public class HorseGame extends Game {

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
