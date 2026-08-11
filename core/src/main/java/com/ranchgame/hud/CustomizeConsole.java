package com.ranchgame.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.ranchgame.horse.HorseAppearance;

/**
 * The customization console: a "..." button in the top-right corner opens a
 * panel of color swatches for the horse (coat, mane, tack) and the rider
 * (shirt, trousers, hair). Selections apply immediately and are saved.
 */
public class CustomizeConsole {

    /** Notified whenever the player picks a new color. */
    public interface Listener {
        void appearanceChanged();
    }

    private interface Selector {
        int get();
        void set(int index);
    }

    private static final float SWATCH_W = 38f;
    private static final float SWATCH_H = 26f;

    private final HorseAppearance appearance;
    private final Listener listener;
    private final TextureRegionDrawable white;
    private final Drawable panelBackground;
    private final Label.LabelStyle captionStyle;
    private final Image button;
    private final Table panel;
    private final Table rows = new Table();
    private final Array<Runnable> refreshers = new Array<>();
    private Cell<ScrollPane> scrollCell;
    private boolean open;

    public CustomizeConsole(Stage stage, HorseAppearance appearance, Texture whiteTex,
                            Texture dotsTex, Drawable panelBackground,
                            Label.LabelStyle captionStyle,
                            Label.LabelStyle titleStyle, Listener listener) {
        this.appearance = appearance;
        this.listener = listener;
        this.captionStyle = captionStyle;
        this.panelBackground = panelBackground;
        this.white = new TextureRegionDrawable(new TextureRegion(whiteTex));

        // --- "..." button -------------------------------------------------
        button = new Image(new TextureRegionDrawable(new TextureRegion(dotsTex)));
        button.setSize(42f, 42f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setOpen(!open);
            }
        });
        stage.addActor(button);

        // --- swatch rows ---------------------------------------------------
        addRow("Horse coat", HorseAppearance.COAT, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.coat; }
            public void set(int i) { CustomizeConsole.this.appearance.coat = i; }
        });
        addRow("Mane & tail", HorseAppearance.MANE, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.mane; }
            public void set(int i) { CustomizeConsole.this.appearance.mane = i; }
        });
        addRow("Saddle", HorseAppearance.TACK, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.tack; }
            public void set(int i) { CustomizeConsole.this.appearance.tack = i; }
        });
        addRow("Saddle pad", HorseAppearance.PAD, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.pad; }
            public void set(int i) { CustomizeConsole.this.appearance.pad = i; }
        });
        addRow("Rider shirt", HorseAppearance.SHIRT, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.shirt; }
            public void set(int i) { CustomizeConsole.this.appearance.shirt = i; }
        });
        addRow("Rider trousers", HorseAppearance.PANTS, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.pants; }
            public void set(int i) { CustomizeConsole.this.appearance.pants = i; }
        });
        addRow("Rider hair", HorseAppearance.HAIR, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.hair; }
            public void set(int i) { CustomizeConsole.this.appearance.hair = i; }
        });
        addRow("Helmet", HorseAppearance.HELMET, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.helmet; }
            public void set(int i) { CustomizeConsole.this.appearance.helmet = i; }
        });
        addRow("Face marking (1st = none)", HorseAppearance.MARKING, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.marking; }
            public void set(int i) { CustomizeConsole.this.appearance.marking = i; }
        });
        addRow("Leg wraps (1st = none)", HorseAppearance.WRAPS, new Selector() {
            public int get() { return CustomizeConsole.this.appearance.wraps; }
            public void set(int i) { CustomizeConsole.this.appearance.wraps = i; }
        });

        // --- panel ---------------------------------------------------------
        ScrollPane scroll = new ScrollPane(rows);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);

        Table doneButton = new Table();
        doneButton.setBackground(white.tint(new Color(0.45f, 0.33f, 0.22f, 0.35f)));
        doneButton.add(new Label("Done", captionStyle)).pad(6f, 24f, 6f, 24f);
        doneButton.setTouchable(Touchable.enabled);
        doneButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setOpen(false);
            }
        });

        panel = new Table();
        panel.setBackground(panelBackground);
        panel.pad(14f);
        panel.add(new Label("Customize", titleStyle)).left().padBottom(6f).row();
        scrollCell = panel.add(scroll).growX();
        panel.row();
        panel.add(doneButton).padTop(12f);
        panel.setTouchable(Touchable.enabled);   // swallow clicks behind the panel
        panel.setVisible(false);
        stage.addActor(panel);
    }

    private void addRow(String label, Color[] palette, final Selector selector) {
        rows.add(new Label(label, captionStyle)).left().padTop(8f).row();
        Table swatches = new Table();
        for (int i = 0; i < palette.length; i++) {
            final int index = i;
            final Table cell = new Table();
            Image chip = new Image(white);
            chip.setColor(palette[i]);
            cell.add(chip).size(SWATCH_W, SWATCH_H).pad(3f);
            cell.setTouchable(Touchable.enabled);
            cell.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selector.set(index);
                    refreshSelection();
                    listener.appearanceChanged();
                }
            });
            refreshers.add(new Runnable() {
                @Override
                public void run() {
                    cell.setBackground(selector.get() == index
                            ? white.tint(new Color(0.45f, 0.33f, 0.22f, 0.9f)) : null);
                }
            });
            swatches.add(cell).padRight(2f);
        }
        rows.add(swatches).left().row();
    }

    private void refreshSelection() {
        for (Runnable r : refreshers) r.run();
    }

    public void setOpen(boolean open) {
        this.open = open;
        panel.setVisible(open);
        if (open) refreshSelection();
    }

    public boolean isOpen() {
        return open;
    }

    public void layout(float worldWidth, float worldHeight) {
        button.setPosition(worldWidth - button.getWidth() - 10f,
                worldHeight - button.getHeight() - 10f);

        // let the swatch list use whatever vertical room the screen allows
        scrollCell.maxHeight(Math.max(140f, worldHeight - 150f));
        panel.invalidateHierarchy();
        panel.pack();

        float w = Math.min(panel.getWidth(), worldWidth - 20f);
        float h = Math.min(panel.getHeight(), worldHeight - 20f);
        panel.setSize(w, h);
        // Anchor right when there's room, so the horse stays visible while
        // you pick colors; centre it only on screens too narrow for that.
        float x = worldWidth - w > 300f ? worldWidth - w - 12f : (worldWidth - w) / 2f;
        panel.setPosition(x, (worldHeight - h) / 2f);
    }
}
