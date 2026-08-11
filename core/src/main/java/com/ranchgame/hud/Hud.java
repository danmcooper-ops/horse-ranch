package com.ranchgame.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * 2D overlay: gait/speed readout, course status, flash messages, results
 * panel, and (on touch devices) a virtual joystick + jump button.
 * Stage units are density-independent pixels so controls are finger-sized
 * on every phone.
 */
public class Hud implements Disposable {

    public final Stage stage;
    private final BitmapFont font;
    private final BitmapFont bigFont;
    private final Texture white;
    private final Texture padBase;
    private final Texture padKnob;

    private Table timerPill;
    private Table coursePill;
    private final Label gaitLabel;
    private final Label courseLabel;
    private final Label timerLabel;
    private final Label messageLabel;
    private final Label hintLabel;
    private final Table resultsPanel;
    private final Label resultsLabel;

    private final Touchpad touchpad;
    private final Image jumpButton;
    private final Label jumpLabel;
    private final Image actionButton;
    private final Label actionLabel;
    private boolean actionQueued;
    private boolean actionAvailable;
    private final Texture dots;
    private final Texture panelTex;
    private NinePatchDrawable panelBg;
    private final Label.LabelStyle style;
    private final Label.LabelStyle bigStyle;
    private Label.LabelStyle darkStyle;
    private Label.LabelStyle darkBigStyle;
    private CustomizeConsole console;
    private boolean touchControlsWanted;
    private boolean jumpQueued;
    private boolean knobArmed = true;
    private float messageTimer;

    public Hud() {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Math.max(1f, Gdx.graphics.getDensity()));
        stage = new Stage(viewport);

        font = new BitmapFont(Gdx.files.internal("ui/lsans-15.fnt"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(1.15f);
        font.setUseIntegerPositions(false);
        bigFont = new BitmapFont(Gdx.files.internal("ui/lsans-15.fnt"));
        bigFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bigFont.getData().setScale(1.9f);
        bigFont.setUseIntegerPositions(false);

        Pixmap p = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        white = new Texture(p);
        p.dispose();
        padBase = circleTexture(120, new Color(1f, 1f, 1f, 0.25f), new Color(1f, 1f, 1f, 0.5f));
        padKnob = circleTexture(56, new Color(1f, 1f, 1f, 0.75f), new Color(1f, 1f, 1f, 0.9f));
        dots = dotsTexture(84);
        panelTex = panelTexture(64);
        panelBg = new NinePatchDrawable(new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20));

        style = new Label.LabelStyle(font, Color.WHITE);
        bigStyle = new Label.LabelStyle(bigFont, Color.WHITE);
        Color ink = new Color(0.32f, 0.24f, 0.16f, 1f);   // warm brown text on cream
        darkStyle = new Label.LabelStyle(font, ink);
        darkBigStyle = new Label.LabelStyle(bigFont, ink);

        gaitLabel = new Label("Halt", darkBigStyle);
        courseLabel = new Label("", darkStyle);
        courseLabel.setAlignment(Align.center);
        timerLabel = new Label("", darkStyle);
        timerLabel.setAlignment(Align.right);
        messageLabel = new Label("", bigStyle);
        messageLabel.setAlignment(Align.center);
        messageLabel.setColor(1f, 0.85f, 0.35f, 1f);
        hintLabel = new Label("Arrows: steer & change gait   Space: jump   E: dismount / ride", style);
        hintLabel.setColor(1f, 1f, 1f, 0.75f);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(10f);
        root.top();
        Table gaitPill = new Table();
        gaitPill.setBackground(panelBg);
        gaitPill.add(gaitLabel).pad(0f, 14f, 2f, 14f);
        timerPill = new Table();
        timerPill.setBackground(panelBg);
        timerPill.add(timerLabel).pad(0f, 14f, 2f, 14f);
        root.add(gaitPill).left().expandX();
        root.add(timerPill).right().padRight(52f);    // clear of the "..." button
        root.row();
        courseLabel.setWrap(true);
        coursePill = new Table();
        coursePill.setBackground(panelBg);
        coursePill.add(courseLabel).growX().pad(0f, 16f, 2f, 16f);
        root.add(coursePill).colspan(2).center().padTop(6f).minWidth(260f);
        root.row();
        root.add(messageLabel).colspan(2).padTop(14f);
        stage.addActor(root);

        Table bottom = new Table();
        bottom.setFillParent(true);
        bottom.bottom().pad(10f);
        bottom.add(hintLabel);
        stage.addActor(bottom);

        resultsPanel = new Table();
        resultsPanel.setFillParent(true);
        resultsPanel.center();
        resultsLabel = new Label("", darkBigStyle);
        resultsLabel.setAlignment(Align.center);
        Table inner = new Table();
        inner.setBackground(panelBg);
        inner.pad(24f);
        inner.add(resultsLabel);
        resultsPanel.add(inner);
        resultsPanel.setVisible(false);
        stage.addActor(resultsPanel);

        Touchpad.TouchpadStyle padStyle = new Touchpad.TouchpadStyle();
        padStyle.background = new TextureRegionDrawable(new TextureRegion(padBase));
        padStyle.knob = new TextureRegionDrawable(new TextureRegion(padKnob));
        touchpad = new Touchpad(6f, padStyle);
        touchpad.setColor(1f, 0.94f, 0.82f, 1f);
        touchpad.setSize(128f, 128f);
        stage.addActor(touchpad);

        jumpButton = new Image(new TextureRegionDrawable(new TextureRegion(padBase)));
        jumpButton.setColor(1f, 0.94f, 0.82f, 1f);
        jumpButton.setSize(86f, 86f);
        jumpButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float sx, float sy, int pointer, int button) {
                jumpQueued = true;
                return true;
            }
        });
        stage.addActor(jumpButton);
        jumpLabel = new Label("JUMP", darkStyle);
        jumpLabel.setTouchable(Touchable.disabled);
        stage.addActor(jumpLabel);

        actionButton = new Image(new TextureRegionDrawable(new TextureRegion(padBase)));
        actionButton.setSize(74f, 74f);
        actionButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float sx, float sy, int pointer, int button) {
                actionQueued = true;
                return true;
            }
        });
        stage.addActor(actionButton);
        actionLabel = new Label("OFF", style);
        actionLabel.setTouchable(Touchable.disabled);
        stage.addActor(actionLabel);

        layoutTouchControls();
        setTouchVisible(false);
    }

    /** Rounded cream square with three brown dots: the customize button. */
    private static Texture dotsTexture(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        int r = size / 5;
        pm.setColor(0.95f, 0.9f, 0.8f, 0.92f);
        pm.fillRectangle(r, 0, size - 2 * r, size);
        pm.fillRectangle(0, r, size, size - 2 * r);
        pm.fillCircle(r, r, r);
        pm.fillCircle(size - r - 1, r, r);
        pm.fillCircle(r, size - r - 1, r);
        pm.fillCircle(size - r - 1, size - r - 1, r);
        pm.setColor(0.42f, 0.31f, 0.2f, 1f);
        int dot = Math.max(2, size / 14);
        for (int i = -1; i <= 1; i++) {
            pm.fillCircle(size / 2 + i * (size / 4), size / 2, dot);
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    /** Rounded parchment panel with a wood-brown border, stretched as a 9-patch. */
    private static Texture panelTexture(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        int r = size / 4;
        // border layer (wood brown), then inset fill (warm cream)
        fillRounded(pm, 0, size, r, new Color(0.45f, 0.33f, 0.22f, 0.96f));
        fillRounded(pm, 3, size, r - 3, new Color(0.96f, 0.92f, 0.82f, 0.96f));
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    private static void fillRounded(Pixmap pm, int inset, int size, int radius, Color c) {
        pm.setColor(c);
        int a = inset, b = size - inset - 1;
        pm.fillRectangle(a + radius, a, b - a - 2 * radius + 1, b - a + 1);
        pm.fillRectangle(a, a + radius, b - a + 1, b - a - 2 * radius + 1);
        pm.fillCircle(a + radius, a + radius, radius);
        pm.fillCircle(b - radius, a + radius, radius);
        pm.fillCircle(a + radius, b - radius, radius);
        pm.fillCircle(b - radius, b - radius, radius);
    }

    private static Texture circleTexture(int size, Color fill, Color ring) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        int r = size / 2 - 1;
        pm.setColor(ring);
        pm.fillCircle(size / 2, size / 2, r);
        pm.setColor(fill);
        pm.fillCircle(size / 2, size / 2, r - 3);
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    private void layoutTouchControls() {
        touchpad.setPosition(24f, 24f);
        float bx = stage.getViewport().getWorldWidth() - jumpButton.getWidth() - 28f;
        jumpButton.setPosition(bx, 40f);
        jumpLabel.pack();
        jumpLabel.setPosition(bx + (jumpButton.getWidth() - jumpLabel.getWidth()) / 2f,
                40f + (jumpButton.getHeight() - jumpLabel.getHeight()) / 2f);
        float ax = stage.getViewport().getWorldWidth() - actionButton.getWidth() - 34f;
        float ay = 40f + jumpButton.getHeight() + 18f;
        actionButton.setPosition(ax, ay);
        actionLabel.pack();
        actionLabel.setPosition(ax + (actionButton.getWidth() - actionLabel.getWidth()) / 2f,
                ay + (actionButton.getHeight() - actionLabel.getHeight()) / 2f);
        if (console != null) {
            console.layout(stage.getViewport().getWorldWidth(),
                    stage.getViewport().getWorldHeight());
        }
    }

    public void setTouchVisible(boolean visible) {
        touchControlsWanted = visible;
        applyTouchVisibility();
    }

    private void applyTouchVisibility() {
        boolean visible = touchControlsWanted && (console == null || !console.isOpen());
        touchpad.setVisible(visible);
        jumpButton.setVisible(visible);
        jumpLabel.setVisible(visible);
        jumpButton.setTouchable(visible ? Touchable.enabled : Touchable.disabled);
        boolean action = visible && actionAvailable;
        actionButton.setVisible(action);
        actionLabel.setVisible(action);
        actionButton.setTouchable(action ? Touchable.enabled : Touchable.disabled);
        hintLabel.setVisible(!touchControlsWanted && (console == null || !console.isOpen()));
    }

    /** Show/hide the mount button and set its label ("OFF" to dismount, "RIDE" to mount). */
    public void setAction(boolean available, String label) {
        actionAvailable = available;
        if (label != null && !label.contentEquals(actionLabel.getText())) {
            actionLabel.setText(label);
            actionLabel.pack();
            layoutTouchControls();
        }
    }

    public boolean touchActionPressed() {
        boolean a = actionQueued;
        actionQueued = false;
        return a;
    }

    /** Builds the customize console; call once after construction. */
    public void createConsole(com.ranchgame.horse.HorseAppearance appearance,
                              final CustomizeConsole.Listener listener) {
        console = new CustomizeConsole(stage, appearance, white, dots, panelBg, darkStyle, darkBigStyle,
                new CustomizeConsole.Listener() {
                    @Override
                    public void appearanceChanged() {
                        listener.appearanceChanged();
                    }
                });
        layoutTouchControls();
        if (com.ranchgame.HorseGame.openConsoleOnStart) console.setOpen(true);
    }

    public boolean isConsoleOpen() {
        return console != null && console.isOpen();
    }

    // --- Touch input polling ---------------------------------------------

    public float touchTurnAxis() {
        return touchpad.isVisible() ? -touchpad.getKnobPercentX() : 0f;
    }

    /** +1 = shift gait up, -1 = shift down, 0 = nothing; uses hysteresis on knob Y. */
    public int touchGaitShift() {
        if (!touchpad.isVisible()) return 0;
        float y = touchpad.getKnobPercentY();
        if (knobArmed && y > 0.62f) {
            knobArmed = false;
            return 1;
        }
        if (knobArmed && y < -0.62f) {
            knobArmed = false;
            return -1;
        }
        if (!knobArmed && Math.abs(y) < 0.3f) knobArmed = true;
        return 0;
    }

    public boolean touchJumpPressed() {
        boolean j = jumpQueued;
        jumpQueued = false;
        return j;
    }

    // --- Display updates --------------------------------------------------

    public void setGait(String label, float speed) {
        gaitLabel.setText(label + "  " + (int) (speed * 3.6f) + " km/h");
    }

    public void setCourseStatus(String text) {
        courseLabel.setText(text);
    }

    public void setTimer(String text) {
        timerLabel.setText(text);
    }

    public void showMessage(String text) {
        messageLabel.setText(text);
        messageTimer = 2.2f;
        messageLabel.setVisible(true);
    }

    public void showResults(String text) {
        resultsLabel.setText(text);
        resultsPanel.setVisible(true);
    }

    public void hideResults() {
        resultsPanel.setVisible(false);
    }

    public void update(float delta) {
        applyTouchVisibility();
        if (messageTimer > 0f) {
            messageTimer -= delta;
            if (messageTimer <= 0f) messageLabel.setVisible(false);
        }
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layoutTouchControls();
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        bigFont.dispose();
        white.dispose();
        padBase.dispose();
        padKnob.dispose();
        dots.dispose();
        panelTex.dispose();
    }
}
