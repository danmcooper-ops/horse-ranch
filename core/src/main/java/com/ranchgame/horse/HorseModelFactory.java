package com.ranchgame.horse;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Builds a stylized low-poly horse (optionally with rider) entirely in code.
 * Every articulated part is its own named node whose mesh is authored with the
 * pivot at the joint, so rotating the node swings the limb naturally.
 * Node ids: body, neck, tail, legFL, legFR, legBL, legBR, rider.
 */
public final class HorseModelFactory {

    private static final long ATTRS = Usage.Position | Usage.Normal;
    private static final long ATTRS_TEX = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

    private HorseModelFactory() {
    }

    public static Model create(Color bodyColor, Color maneColor, boolean withRider, Texture coat) {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        Color hoof = new Color(0.16f, 0.12f, 0.1f, 1f);
        Color muzzle = new Color(bodyColor).lerp(Color.WHITE, 0.35f);

        // --- Body ---------------------------------------------------------
        Node body = mb.node();
        body.id = "body";
        body.translation.set(0f, 1.02f, 0f);
        MeshPartBuilder b = mb.part("body", GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_body", coat, bodyColor));
        // stacked, offset boxes approximate a rounded barrel-chested silhouette
        BoxShapeBuilder.build(b, 0f, 0.02f, 0.05f, 0.68f, 0.6f, 1.6f);   // core
        BoxShapeBuilder.build(b, 0f, -0.15f, 0f, 0.58f, 0.5f, 1.3f);     // belly
        BoxShapeBuilder.build(b, 0f, 0.33f, 0.02f, 0.5f, 0.2f, 1.4f);    // back/withers
        BoxShapeBuilder.build(b, 0f, -0.02f, 0.8f, 0.54f, 0.54f, 0.3f);  // chest
        BoxShapeBuilder.build(b, 0f, 0.04f, -0.78f, 0.58f, 0.6f, 0.32f); // rump
        if (withRider) {
            MeshPartBuilder sd = mb.part("saddle", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_leather", new Color(0.45f, 0.2f, 0.12f, 1f)));
            BoxShapeBuilder.build(sd, 0f, 0.4f, -0.05f, 0.52f, 0.14f, 0.6f);
            MeshPartBuilder pad = mb.part("saddlePad", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_cloth", new Color(0.85f, 0.1f, 0.15f, 1f)));
            BoxShapeBuilder.build(pad, 0f, 0.36f, -0.05f, 0.78f, 0.06f, 0.75f);
        }

        // --- Neck + head (one node, pivot at neck base) -------------------
        Node neck = mb.node();
        neck.id = "neck";
        neck.translation.set(0f, 1.3f, 0.62f);
        MeshPartBuilder n = mb.part("neck", GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_neck", coat, bodyColor));
        Matrix4 lean = new Matrix4().rotate(Vector3.X, 32f);
        n.setVertexTransform(lean);
        BoxShapeBuilder.build(n, 0f, 0.18f, 0f, 0.34f, 0.55f, 0.4f);    // neck base, wider
        BoxShapeBuilder.build(n, 0f, 0.52f, 0.02f, 0.26f, 0.45f, 0.3f); // upper neck, tapered
        n.setVertexTransform(null);
        BoxShapeBuilder.build(n, 0f, 0.8f, 0.55f, 0.28f, 0.28f, 0.55f);   // head
        MeshPartBuilder mz = mb.part("muzzle", GL20.GL_TRIANGLES, ATTRS, mat("muzzle", muzzle));
        BoxShapeBuilder.build(mz, 0f, 0.74f, 0.9f, 0.2f, 0.2f, 0.24f);
        MeshPartBuilder mn = mb.part("mane", GL20.GL_TRIANGLES, ATTRS, mat("mane", maneColor));
        mn.setVertexTransform(lean);
        BoxShapeBuilder.build(mn, 0f, 0.36f, -0.21f, 0.12f, 0.85f, 0.14f);
        mn.setVertexTransform(null);
        BoxShapeBuilder.build(mn, 0f, 0.98f, 0.39f, 0.06f, 0.16f, 0.06f); // forelock tuft
        BoxShapeBuilder.build(mn, -0.08f, 0.99f, 0.37f, 0.06f, 0.2f, 0.06f); // ear L
        BoxShapeBuilder.build(mn, 0.08f, 0.99f, 0.37f, 0.06f, 0.2f, 0.06f);  // ear R

        // --- Tail ---------------------------------------------------------
        Node tail = mb.node();
        tail.id = "tail";
        tail.translation.set(0f, 1.32f, -0.75f);
        MeshPartBuilder t = mb.part("tail", GL20.GL_TRIANGLES, ATTRS, mat("mane_tail", maneColor));
        t.setVertexTransform(new Matrix4().rotate(Vector3.X, 20f));
        BoxShapeBuilder.build(t, 0f, -0.4f, 0f, 0.16f, 0.8f, 0.16f);
        t.setVertexTransform(null);

        // --- Legs (pivot at hip/shoulder) ---------------------------------
        buildLeg(mb, "legFL", -0.24f, 0.62f, bodyColor, hoof, coat);
        buildLeg(mb, "legFR", 0.24f, 0.62f, bodyColor, hoof, coat);
        buildLeg(mb, "legBL", -0.24f, -0.62f, bodyColor, hoof, coat);
        buildLeg(mb, "legBR", 0.24f, -0.62f, bodyColor, hoof, coat);

        // --- Rider --------------------------------------------------------
        if (withRider) {
            Node rider = mb.node();
            rider.id = "rider";
            rider.translation.set(0f, 1.46f, -0.02f);
            MeshPartBuilder jeans = mb.part("riderLegs", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_pants", new Color(0.2f, 0.25f, 0.45f, 1f)));
            BoxShapeBuilder.build(jeans, -0.42f, -0.1f, 0.02f, 0.13f, 0.55f, 0.16f);
            BoxShapeBuilder.build(jeans, 0.42f, -0.1f, 0.02f, 0.13f, 0.55f, 0.16f);
            MeshPartBuilder boots = mb.part("riderBoots", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_boots", new Color(0.3f, 0.18f, 0.1f, 1f)));
            BoxShapeBuilder.build(boots, -0.42f, -0.41f, 0.07f, 0.14f, 0.14f, 0.26f);
            BoxShapeBuilder.build(boots, 0.42f, -0.41f, 0.07f, 0.14f, 0.14f, 0.26f);
            MeshPartBuilder shirt = mb.part("riderTorso", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_shirt", new Color(0.25f, 0.55f, 0.85f, 1f)));
            BoxShapeBuilder.build(shirt, 0f, 0.3f, 0f, 0.36f, 0.55f, 0.24f);
            BoxShapeBuilder.build(shirt, -0.23f, 0.38f, 0.18f, 0.09f, 0.09f, 0.4f); // arm L
            BoxShapeBuilder.build(shirt, 0.23f, 0.38f, 0.18f, 0.09f, 0.09f, 0.4f);  // arm R
            MeshPartBuilder skin = mb.part("riderHead", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
            BoxShapeBuilder.build(skin, 0f, 0.7f, 0f, 0.22f, 0.24f, 0.22f);
            MeshPartBuilder hair = mb.part("riderHair", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_hair", new Color(0.28f, 0.18f, 0.1f, 1f)));
            BoxShapeBuilder.build(hair, 0f, 0.83f, 0f, 0.24f, 0.09f, 0.24f);    // crown
            BoxShapeBuilder.build(hair, 0f, 0.73f, -0.11f, 0.23f, 0.2f, 0.05f); // back
            BoxShapeBuilder.build(hair, 0f, 0.78f, 0.11f, 0.23f, 0.1f, 0.04f);  // fringe
        }

        return mb.end();
    }

    private static void buildLeg(ModelBuilder mb, String id, float x, float z,
                                 Color bodyColor, Color hoofColor, Texture coat) {
        Node leg = mb.node();
        leg.id = id;
        leg.translation.set(x, 0.95f, z);
        MeshPartBuilder l = mb.part(id, GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_" + id, coat, bodyColor));
        BoxShapeBuilder.build(l, 0f, -0.26f, 0f, 0.25f, 0.55f, 0.27f);  // thigh/forearm
        BoxShapeBuilder.build(l, 0f, -0.7f, 0f, 0.15f, 0.45f, 0.16f);   // cannon, slimmer
        MeshPartBuilder h = mb.part(id + "Hoof", GL20.GL_TRIANGLES, ATTRS, mat("hoof_" + id, hoofColor));
        BoxShapeBuilder.build(h, 0f, -0.9f, 0f, 0.18f, 0.13f, 0.19f);
    }

    private static Material mat(String id, Color c) {
        return new Material(id, ColorAttribute.createDiffuse(c));
    }

    /**
     * Dappled coat: the near-white noise texture tinted with the horse's color.
     * The material id lets {@link HorseAppearance} recolor it later.
     */
    private static Material coatMat(String id, Texture coat, Color tint) {
        TextureAttribute ta = TextureAttribute.createDiffuse(coat);
        ta.scaleU = 1.5f;
        ta.scaleV = 1.5f;
        return new Material(id, ta, ColorAttribute.createDiffuse(tint));
    }
}
