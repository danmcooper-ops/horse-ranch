package com.ranchgame.teavm;

import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.ranchgame.HorseGame;

/** Entry point of the compiled web (JavaScript/WebGL) build. */
public class TeaVMLauncher {

    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        // 0 = fill the browser window
        config.width = 0;
        config.height = 0;
        new TeaApplication(new HorseGame(), config);
    }
}
