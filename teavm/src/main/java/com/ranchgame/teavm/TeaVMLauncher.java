package com.ranchgame.teavm;

import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.ranchgame.HorseGame;
import org.teavm.jso.JSBody;

/** Entry point of the compiled web (JavaScript/WebGL) build. */
public class TeaVMLauncher {

    public static void main(String[] args) {
        // the gdx-teavm MultitouchScreen peripheral query always answers no,
        // so ask the browser directly (covers iPhone, iPad and Android)
        HorseGame.touchDevice = isTouchDevice();
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        // 0 = fill the browser window
        config.width = 0;
        config.height = 0;
        new TeaApplication(new HorseGame(), config);
    }

    @JSBody(script = "return ('ontouchstart' in window) || (navigator.maxTouchPoints > 0);")
    private static native boolean isTouchDevice();
}
