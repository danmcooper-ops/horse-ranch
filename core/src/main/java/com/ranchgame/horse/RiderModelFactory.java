package com.ranchgame.horse;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;

/**
 * The rider standing on their own two feet. Limbs are separate nodes pivoted
 * at shoulder/hip so they swing while walking. Material ids match the mounted
 * rider's (rider_shirt, rider_pants, rider_hair, ...) so HorseAppearance
 * recolors both models identically.
 */
public final class RiderModelFactory {

    private static final long ATTRS = Usage.Position | Usage.Normal;

    private RiderModelFactory() {
    }

    public static Model create() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        // --- Torso + head (pivot at the hip line) --------------------------
        Node torso = mb.node();
        torso.id = "torso";
        torso.translation.set(0f, 0.82f, 0f);
        MeshPartBuilder shirt = mb.part("torso", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_shirt", new Color(0.25f, 0.55f, 0.85f, 1f)));
        BoxShapeBuilder.build(shirt, 0f, 0.29f, 0f, 0.36f, 0.58f, 0.2f);
        MeshPartBuilder skin = mb.part("head", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
        BoxShapeBuilder.build(skin, 0f, 0.71f, 0f, 0.22f, 0.24f, 0.22f);
        MeshPartBuilder hair = mb.part("hair", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_hair", new Color(0.28f, 0.18f, 0.1f, 1f)));
        BoxShapeBuilder.build(hair, 0f, 0.84f, 0f, 0.24f, 0.09f, 0.24f);    // crown
        BoxShapeBuilder.build(hair, 0f, 0.74f, -0.11f, 0.23f, 0.2f, 0.05f); // back
        BoxShapeBuilder.build(hair, 0f, 0.79f, 0.11f, 0.23f, 0.1f, 0.04f);  // fringe

        // --- Arms (pivot at the shoulder) ----------------------------------
        buildArm(mb, "armL", -0.25f);
        buildArm(mb, "armR", 0.25f);

        // --- Legs (pivot at the hip) ---------------------------------------
        buildWalkerLeg(mb, "legL", -0.11f);
        buildWalkerLeg(mb, "legR", 0.11f);

        return mb.end();
    }

    private static void buildArm(ModelBuilder mb, String id, float x) {
        Node arm = mb.node();
        arm.id = id;
        arm.translation.set(x, 1.36f, 0f);
        MeshPartBuilder sleeve = mb.part(id, GL20.GL_TRIANGLES, ATTRS,
                mat("rider_shirt", new Color(0.25f, 0.55f, 0.85f, 1f)));
        BoxShapeBuilder.build(sleeve, 0f, -0.17f, 0f, 0.11f, 0.34f, 0.13f);
        MeshPartBuilder hand = mb.part(id + "Hand", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
        BoxShapeBuilder.build(hand, 0f, -0.42f, 0f, 0.1f, 0.16f, 0.12f);
    }

    private static void buildWalkerLeg(ModelBuilder mb, String id, float x) {
        Node leg = mb.node();
        leg.id = id;
        leg.translation.set(x, 0.82f, 0f);
        MeshPartBuilder pants = mb.part(id, GL20.GL_TRIANGLES, ATTRS,
                mat("rider_pants", new Color(0.2f, 0.25f, 0.45f, 1f)));
        BoxShapeBuilder.build(pants, 0f, -0.34f, 0f, 0.16f, 0.68f, 0.18f);
        MeshPartBuilder boot = mb.part(id + "Boot", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_boots", new Color(0.3f, 0.18f, 0.1f, 1f)));
        BoxShapeBuilder.build(boot, 0f, -0.76f, 0.04f, 0.17f, 0.16f, 0.28f);
    }

    private static Material mat(String id, Color c) {
        return new Material(id, ColorAttribute.createDiffuse(c));
    }
}
