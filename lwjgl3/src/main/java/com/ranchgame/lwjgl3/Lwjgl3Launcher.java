package com.ranchgame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ranchgame.HorseGame;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired(args)) return;
        // debug: --screenshot <path> [seconds] renders briefly, saves a PNG and exits
        for (int i = 0; i < args.length; i++) {
            if ("--console".equals(args[i])) HorseGame.openConsoleOnStart = true;
            if ("--walk".equals(args[i])) HorseGame.startDismounted = true;
        }
        for (int i = 0; i < args.length - 1; i++) {
            if ("--look".equals(args[i])) {
                String[] parts = args[i + 1].split(",");
                int[] look = new int[Math.max(10, parts.length)];
                for (int k = 0; k < parts.length; k++) {
                    look[k] = Integer.parseInt(parts[k].trim());
                }
                HorseGame.presetLook = look;
            }
            if ("--pose".equals(args[i])) {
                // --pose <gaitIndex>,<phase 0..1 of the stride cycle>
                String[] pp = args[i + 1].split(",");
                HorseGame.poseGait = Integer.parseInt(pp[0].trim());
                HorseGame.posePhase = Float.parseFloat(pp[1].trim());
            }
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
