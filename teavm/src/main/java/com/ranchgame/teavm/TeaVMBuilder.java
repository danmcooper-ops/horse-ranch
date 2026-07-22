package com.ranchgame.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.teavm.tooling.TeaVMTool;

/** Compiles the game to JavaScript/WebGL and hardens index.html for mobile. */
public class TeaVMBuilder {

    public static void main(String[] args) throws Exception {
        TeaBuildConfiguration config = new TeaBuildConfiguration();
        config.assetsPath.add(new AssetFileHandle("../assets"));
        config.webappPath = new File("build/dist").getCanonicalPath();

        TeaVMTool tool = TeaBuilder.config(config);
        tool.setMainClass(TeaVMLauncher.class.getName());
        tool.setObfuscated(false);
        TeaBuilder.build(tool);

        hardenIndexHtml(new File("build/dist/webapp/index.html").toPath());
    }

    /**
     * Injects the viewport meta tag and touch CSS that stop mobile browsers
     * from zooming, scrolling or rubber-banding while playing.
     */
    private static void hardenIndexHtml(Path index) throws Exception {
        if (!Files.exists(index)) {
            System.out.println("index.html not found, skipping mobile hardening");
            return;
        }
        String html = new String(Files.readAllBytes(index), StandardCharsets.UTF_8);
        String inject = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, "
                + "maximum-scale=1, user-scalable=no, viewport-fit=cover\">\n"
                + "<style>\n"
                + "html,body{position:fixed;inset:0;overflow:hidden;overscroll-behavior:none;"
                + "margin:0;padding:0;width:100%;height:100%;background:#8cc6ef;}\n"
                + "body>div{width:100%;height:100%;}\n"
                + "#canvas{width:100%;height:100%;display:block;touch-action:none;outline:none;"
                + "-webkit-tap-highlight-color:transparent;}\n"
                + "</style>\n"
                // Some embedded/hidden webviews never fire requestAnimationFrame while the
                // page is hidden at load; fall back to a timer so the game still starts.
                + "<script>if(document.visibilityState==='hidden'){"
                + "window.requestAnimationFrame=function(cb){return setTimeout(function(){"
                + "cb(performance.now());},16);};}</script>\n";
        html = html.replace("<title>gdx-teavm</title>", "<title>Horse Ranch</title>");
        if (html.contains("</head>")) {
            html = html.replace("</head>", inject + "</head>");
        } else {
            html = inject + html;
        }
        Files.write(index, html.getBytes(StandardCharsets.UTF_8));
        System.out.println("index.html hardened for mobile");
    }
}
