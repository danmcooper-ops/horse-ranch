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

import static com.ranchgame.horse.Organic.ball;
import static com.ranchgame.horse.Organic.ballX;

/**
 * The girl on her own two feet, sculpted from smooth ellipsoids with a
 * ponytail. Limbs are separate nodes pivoted at shoulder/hip so they swing
 * while walking. Material ids match the mounted rider's so HorseAppearance
 * recolors both models identically.
 */
public final class RiderModelFactory {

    private static final long ATTRS = Usage.Position | Usage.Normal;
    private static final Color DETAIL = new Color(0.09f, 0.07f, 0.06f, 1f);

    private RiderModelFactory() {
    }

    public static Model create() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        // --- Torso + head (pivot at the hip line) --------------------------
        Node torso = mb.node();
        torso.id = "torso";
        torso.translation.set(0f, 0.82f, 0f);
        MeshPartBuilder pants = mb.part("hips", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_pants", new Color(0.2f, 0.25f, 0.45f, 1f)));
        ball(pants, 0f, 0.05f, 0f, 0.15f, 0.13f, 0.11f);               // hips
        MeshPartBuilder shirt = mb.part("torso", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_shirt", new Color(0.25f, 0.55f, 0.85f, 1f)));
        ball(shirt, 0f, 0.30f, 0f, 0.145f, 0.27f, 0.11f);              // torso
        ball(shirt, 0f, 0.50f, 0f, 0.155f, 0.09f, 0.115f);             // shoulders
        MeshPartBuilder skin = mb.part("head", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
        ball(skin, 0f, 0.585f, 0f, 0.045f, 0.055f, 0.045f);            // neck
        ball(skin, 0f, 0.70f, 0.01f, 0.105f, 0.115f, 0.105f);          // head
        MeshPartBuilder det = mb.part("detail", GL20.GL_TRIANGLES, ATTRS,
                mat("detail", DETAIL));
        ball(det, 0.045f, 0.71f, 0.105f, 0.016f, 0.018f, 0.012f);      // eye R
        ball(det, -0.045f, 0.71f, 0.105f, 0.016f, 0.018f, 0.012f);     // eye L
        MeshPartBuilder hair = mb.part("hair", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_hair", new Color(0.28f, 0.18f, 0.1f, 1f)));
        ball(hair, 0f, 0.74f, -0.03f, 0.115f, 0.11f, 0.115f);          // hair cap
        ballX(hair, 0f, 0.58f, -0.15f, 0.055f, 0.16f, 0.06f, 14f);     // ponytail
        ballX(hair, 0f, 0.40f, -0.19f, 0.04f, 0.12f, 0.045f, 8f);      // ponytail tip

        // --- Arms (pivot at the shoulder) ----------------------------------
        buildArm(mb, "armL", -0.20f);
        buildArm(mb, "armR", 0.20f);

        // --- Legs (pivot at the hip) ---------------------------------------
        buildLeg(mb, "legL", -0.085f);
        buildLeg(mb, "legR", 0.085f);

        return mb.end();
    }

    private static void buildArm(ModelBuilder mb, String id, float x) {
        Node arm = mb.node();
        arm.id = id;
        arm.translation.set(x * 0.9f, 1.33f, 0f);
        MeshPartBuilder sleeve = mb.part(id, GL20.GL_TRIANGLES, ATTRS,
                mat("rider_shirt", new Color(0.25f, 0.55f, 0.85f, 1f)));
        ball(sleeve, 0f, -0.02f, 0f, 0.055f, 0.07f, 0.055f);           // shoulder
        ball(sleeve, 0f, -0.13f, 0f, 0.046f, 0.15f, 0.05f);            // upper arm
        MeshPartBuilder skin = mb.part(id + "Skin", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
        ball(skin, 0f, -0.32f, 0f, 0.038f, 0.13f, 0.042f);             // forearm
        ball(skin, 0f, -0.46f, 0f, 0.042f, 0.052f, 0.042f);            // hand
    }

    private static void buildLeg(ModelBuilder mb, String id, float x) {
        Node leg = mb.node();
        leg.id = id;
        leg.translation.set(x, 0.82f, 0f);
        MeshPartBuilder pants = mb.part(id, GL20.GL_TRIANGLES, ATTRS,
                mat("rider_pants", new Color(0.2f, 0.25f, 0.45f, 1f)));
        ball(pants, 0f, -0.2f, 0f, 0.075f, 0.24f, 0.085f);             // thigh
        ball(pants, 0f, -0.52f, 0f, 0.055f, 0.20f, 0.06f);             // calf
        MeshPartBuilder boot = mb.part(id + "Boot", GL20.GL_TRIANGLES, ATTRS,
                mat("rider_boots", new Color(0.3f, 0.18f, 0.1f, 1f)));
        ball(boot, 0f, -0.76f, 0.05f, 0.055f, 0.065f, 0.12f);          // boot
    }

    private static Material mat(String id, Color c) {
        return new Material(id, ColorAttribute.createDiffuse(c));
    }
}
