package com.ranchgame.horse;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

/**
 * Player-chosen colors for the horse and rider. Colors are applied by
 * recoloring the named materials of a horse ModelInstance, so changes show
 * up immediately without rebuilding the model.
 */
public class HorseAppearance {

    public static final Color[] COAT = {
            new Color(0.55f, 0.36f, 0.20f, 1f),   // bay
            new Color(0.42f, 0.22f, 0.12f, 1f),   // chestnut
            new Color(0.22f, 0.18f, 0.16f, 1f),   // black
            new Color(0.85f, 0.68f, 0.36f, 1f),   // palomino
            new Color(0.90f, 0.86f, 0.78f, 1f),   // cream
            new Color(0.55f, 0.54f, 0.52f, 1f),   // grey
            new Color(0.78f, 0.78f, 0.80f, 1f),   // dapple grey
    };
    /** How strongly each coat's legs darken toward the points color (0 = none). */
    private static final float[] COAT_POINTS = {0.55f, 0f, 0f, 0f, 0f, 0f, 0.6f};
    private static final Color POINTS_DARK = new Color(0.16f, 0.13f, 0.12f, 1f);
    public static final Color[] MANE = {
            new Color(0.25f, 0.16f, 0.10f, 1f),   // dark brown
            new Color(0.12f, 0.10f, 0.10f, 1f),   // black
            new Color(0.92f, 0.90f, 0.84f, 1f),   // white
            new Color(0.80f, 0.66f, 0.42f, 1f),   // flaxen
            new Color(0.58f, 0.28f, 0.16f, 1f),   // auburn
            new Color(0.48f, 0.47f, 0.46f, 1f),   // silver
    };
    public static final Color[] TACK = {
            new Color(0.45f, 0.20f, 0.12f, 1f),   // saddle brown
            new Color(0.28f, 0.16f, 0.10f, 1f),   // dark leather
            new Color(0.15f, 0.13f, 0.12f, 1f),   // black
            new Color(0.72f, 0.53f, 0.32f, 1f),   // tan
            new Color(0.40f, 0.12f, 0.14f, 1f),   // oxblood
            new Color(0.52f, 0.50f, 0.48f, 1f),   // grey
    };
    public static final Color[] PAD = {
            new Color(0.85f, 0.10f, 0.15f, 1f),   // red
            new Color(0.15f, 0.25f, 0.60f, 1f),   // navy
            new Color(0.12f, 0.45f, 0.25f, 1f),   // forest
            new Color(0.45f, 0.20f, 0.60f, 1f),   // purple
            new Color(0.95f, 0.95f, 0.92f, 1f),   // white
            new Color(0.95f, 0.75f, 0.15f, 1f),   // gold
    };
    public static final Color[] SHIRT = {
            new Color(0.25f, 0.55f, 0.85f, 1f),   // blue
            new Color(0.80f, 0.22f, 0.22f, 1f),   // red
            new Color(0.20f, 0.55f, 0.32f, 1f),   // green
            new Color(0.55f, 0.30f, 0.70f, 1f),   // purple
            new Color(0.95f, 0.95f, 0.93f, 1f),   // white
            new Color(0.95f, 0.60f, 0.15f, 1f),   // orange
    };
    public static final Color[] PANTS = {
            new Color(0.20f, 0.25f, 0.45f, 1f),   // denim
            new Color(0.12f, 0.14f, 0.24f, 1f),   // navy
            new Color(0.68f, 0.58f, 0.42f, 1f),   // tan
            new Color(0.36f, 0.26f, 0.18f, 1f),   // brown
            new Color(0.45f, 0.45f, 0.47f, 1f),   // grey
            new Color(0.16f, 0.15f, 0.15f, 1f),   // black
    };
    public static final Color[] HAIR = {
            new Color(0.28f, 0.18f, 0.10f, 1f),   // brown
            new Color(0.10f, 0.09f, 0.09f, 1f),   // black
            new Color(0.85f, 0.72f, 0.42f, 1f),   // blonde
            new Color(0.62f, 0.28f, 0.14f, 1f),   // ginger
            new Color(0.72f, 0.70f, 0.68f, 1f),   // grey
            new Color(0.55f, 0.35f, 0.25f, 1f),   // auburn
    };

    /** Leg-wrap colors; index 0 means no wraps. */
    public static final Color[] WRAPS = {
            new Color(0.6f, 0.6f, 0.6f, 1f),      // (none)
            new Color(0.93f, 0.72f, 0.72f, 1f),   // rose
            new Color(0.95f, 0.95f, 0.92f, 1f),   // white
            new Color(0.2f, 0.3f, 0.6f, 1f),      // blue
            new Color(0.75f, 0.2f, 0.22f, 1f),    // red
            new Color(0.16f, 0.15f, 0.15f, 1f),   // black
    };
    /** Face markings; swatch colors are just row icons. 0 none, 1 star, 2 blaze. */
    public static final Color[] MARKING = {
            new Color(0.6f, 0.6f, 0.6f, 1f),      // (none)
            new Color(0.97f, 0.96f, 0.93f, 1f),   // star
            new Color(0.9f, 0.88f, 0.84f, 1f),    // blaze
    };
    public static final Color[] HELMET = {
            new Color(0.9f, 0.85f, 0.8f, 1f),     // cream (reference look)
            new Color(0.16f, 0.17f, 0.22f, 1f),   // navy
            new Color(0.15f, 0.14f, 0.14f, 1f),   // black
            new Color(0.93f, 0.72f, 0.72f, 1f),   // rose
            new Color(0.35f, 0.25f, 0.18f, 1f),   // brown
            new Color(0.45f, 0.55f, 0.72f, 1f),   // slate blue
    };

    public int coat, mane, tack, pad, shirt, pants, hair;
    public int wraps = 1, marking = 1, helmet;

    public void load(Preferences prefs) {
        coat = clamp(prefs.getInteger("look.coat", 0), COAT.length);
        mane = clamp(prefs.getInteger("look.mane", 0), MANE.length);
        tack = clamp(prefs.getInteger("look.tack", 0), TACK.length);
        pad = clamp(prefs.getInteger("look.pad", 0), PAD.length);
        shirt = clamp(prefs.getInteger("look.shirt", 0), SHIRT.length);
        pants = clamp(prefs.getInteger("look.pants", 0), PANTS.length);
        hair = clamp(prefs.getInteger("look.hair", 0), HAIR.length);
        wraps = clamp(prefs.getInteger("look.wraps", 1), WRAPS.length);
        marking = clamp(prefs.getInteger("look.marking", 1), MARKING.length);
        helmet = clamp(prefs.getInteger("look.helmet", 0), HELMET.length);
    }

    public void save(Preferences prefs) {
        prefs.putInteger("look.coat", coat);
        prefs.putInteger("look.mane", mane);
        prefs.putInteger("look.tack", tack);
        prefs.putInteger("look.pad", pad);
        prefs.putInteger("look.shirt", shirt);
        prefs.putInteger("look.pants", pants);
        prefs.putInteger("look.hair", hair);
        prefs.putInteger("look.wraps", wraps);
        prefs.putInteger("look.marking", marking);
        prefs.putInteger("look.helmet", helmet);
        prefs.flush();
    }

    private static int clamp(int v, int len) {
        return v < 0 || v >= len ? 0 : v;
    }

    /** Recolor every named material and toggle optional parts of an instance. */
    public void apply(ModelInstance instance) {
        Color coatColor = COAT[coat];
        Color muzzleColor = new Color(coatColor).lerp(Color.WHITE, 0.35f);
        Color legColor = new Color(coatColor).lerp(POINTS_DARK, COAT_POINTS[coat]);
        for (Material m : instance.materials) {
            String id = m.id;
            if (id == null) continue;
            if (id.startsWith("coat_leg")) setDiffuse(m, legColor);
            else if (id.startsWith("coat")) setDiffuse(m, coatColor);
            else if (id.startsWith("mane")) setDiffuse(m, MANE[mane]);
            else if (id.equals("muzzle")) setDiffuse(m, muzzleColor);
            else if (id.startsWith("tack_leather")) setDiffuse(m, TACK[tack]);
            else if (id.equals("tack_cloth")) setDiffuse(m, PAD[pad]);
            else if (id.equals("rider_shirt")) setDiffuse(m, SHIRT[shirt]);
            else if (id.equals("rider_pants")) setDiffuse(m, PANTS[pants]);
            else if (id.equals("rider_hair")) setDiffuse(m, HAIR[hair]);
            else if (id.equals("wraps") && wraps > 0) setDiffuse(m, WRAPS[wraps]);
            else if (id.equals("helmet")) setDiffuse(m, HELMET[helmet]);
        }
        // optional geometry: leg wraps and face markings
        setPartsEnabled(instance, "wrap_", wraps > 0);
        setPartsEnabled(instance, "markingStar", marking == 1);
        setPartsEnabled(instance, "markingBlaze", marking == 2);
    }

    /** Strip wraps and markings from pasture horses that keep their baked colors. */
    public static void disableExtras(ModelInstance instance) {
        setPartsEnabled(instance, "wrap_", false);
        setPartsEnabled(instance, "markingStar", false);
        setPartsEnabled(instance, "markingBlaze", false);
    }

    private static void setPartsEnabled(ModelInstance instance, String meshPartIdPrefix, boolean on) {
        for (Node node : instance.nodes) {
            setPartsEnabled(node, meshPartIdPrefix, on);
        }
    }

    private static void setPartsEnabled(Node node, String prefix, boolean on) {
        for (NodePart part : node.parts) {
            if (part.meshPart.id != null && part.meshPart.id.startsWith(prefix)) {
                part.enabled = on;
            }
        }
        for (Node child : node.getChildren()) {
            setPartsEnabled(child, prefix, on);
        }
    }

    private static void setDiffuse(Material m, Color c) {
        m.set(ColorAttribute.createDiffuse(c));
    }
}
