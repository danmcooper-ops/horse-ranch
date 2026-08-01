package com.ranchgame.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private final Texture dots;
    private final Label.LabelStyle style;
    private final Label.LabelStyle bigStyle;
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

        style = new Label.LabelStyle(font, Color.WHITE);
        bigStyle = new Label.LabelStyle(bigFont, Color.WHITE);

        gaitLabel = new Label("Halt", bigStyle);
        courseLabel = new Label("", style);
        courseLabel.setAlignment(Align.center);
        timerLabel = new Label("", style);
        timerLabel.setAlignment(Align.right);
        messageLabel = new Label("", bigStyle);
        messageLabel.setAlignment(Align.center);
        messageLabel.setColor(1f, 0.9f, 0.3f, 1f);
        hintLabel = new Label("Arrows: steer & change gait   Space: jump", style);
        hintLabel.setColor(1f, 1f, 1f, 0.75f);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(10f);
        root.top();
        root.add(gaitLabel).left().expandX();
        root.add(timerLabel).right().padRight(52f);   // clear of the "..." button
        root.row();
        courseLabel.setWrap(true);
        root.add(courseLabel).colspan(2).growX().center().padTop(2f);
        root.row();
        root.add(messageLabel).colspan(2).padTop(14f);
        stage.addActor(root);

        Table bottom = new Table();
        bottom.setFillParent(true);
        bottom.bottom().pad(10f);
        bottom.add(hintLabel);
        stage.addActor(bottom);

        TextureRegionDrawable panelBg = new TextureRegionDrawable(new TextureRegion(white));
        resultsPanel = new Table();
        resultsPanel.setFillParent(true);
        resultsPanel.center();
        resultsLabel = new Label("", bigStyle);
        resultsLabel.setAlignment(Align.center);
        Table inner = new Table();
        inner.setBackground(panelBg.tint(new Color(0f, 0f, 0f, 0.65f)));
        inner.pad(24f);
        inner.add(resultsLabel);
        resultsPanel.add(inner);
        resultsPanel.setVisible(false);
        stage.addActor(resultsPanel);

        Touchpad.TouchpadStyle padStyle = new Touchpad.TouchpadStyle();
        padStyle.background = new TextureRegionDrawable(new TextureRegion(padBase));
        padStyle.knob = new TextureRegionDrawable(new TextureRegion(padKnob));
        touchpad = new Touchpad(6f, padStyle);
        touchpad.setSize(128f, 128f);
        stage.addActor(touchpad);

        jumpButton = new Image(new TextureRegionDrawable(new TextureRegion(padBase)));
        jumpButton.setSize(86f, 86f);
        jumpButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float sx, float sy, int pointer, int button) {
                jumpQueued = true;
                return true;
            }
        });
        stage.addActor(jumpButton);
        jumpLabel = new Label("JUMP", style);
        jumpLabel.setTouchable(Touchable.disabled);
        stage.addActor(jumpLabel);

        layoutTouchControls();
        setTouchVisible(false);
    }

    /** Rounded translucent square with three dots: the customize button. */
    private static Texture dotsTexture(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        int r = size / 5;
        pm.setColor(1f, 1f, 1f, 0.3f);
        pm.fillRectangle(r, 0, size - 2 * r, size);
        pm.fillRectangle(0, r, size, size - 2 * r);
        pm.fillCircle(r, r, r);
        pm.fillCircle(size - r - 1, r, r);
        pm.fillCircle(r, size - r - 1, r);
        pm.fillCircle(size - r - 1, size - r - 1, r);
        pm.setColor(1f, 1f, 1f, 0.95f);
        int dot = Math.max(2, size / 14);
        for (int i = -1; i <= 1; i++) {
            pm.fillCircle(size / 2 + i * (size / 4), size / 2, dot);
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
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
        hintLabel.setVisible(!touchControlsWanted && (console == null || !console.isOpen()));
    }

    /** Builds the customize console; call once after construction. */
    public void createConsole(com.ranchgame.horse.HorseAppearance appearance,
                              final CustomizeConsole.Listener listener) {
        console = new CustomizeConsole(stage, appearance, white, dots, style, bigStyle,
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
    }
}
