package com.ranchgame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ranchgame.HorseGame;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Horse Ranch");
        config.setWindowedMode(1280, 800);
        config.useVsync(true);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        new Lwjgl3Application(new HorseGame(), config);
    }
}
