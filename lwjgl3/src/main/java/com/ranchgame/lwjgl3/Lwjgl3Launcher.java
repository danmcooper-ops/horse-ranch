package com.ranchgame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ranchgame.HorseGame;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired(args)) return;
        // debug: --screenshot <path> [seconds] renders briefly, saves a PNG and exits
        for (int i = 0; i < args.length - 1; i++) {
            if ("--screenshot".equals(args[i])) {
                HorseGame.screenshotPath = args[i + 1];
                if (i + 2 < args.length) {
                    HorseGame.screenshotDelay = Float.parseFloat(args[i + 2]);
                }
            }
        }
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Horse Ranch");
        config.setWindowedMode(1280, 800);
        config.useVsync(true);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        new Lwjgl3Application(new HorseGame(), config);
    }
}
