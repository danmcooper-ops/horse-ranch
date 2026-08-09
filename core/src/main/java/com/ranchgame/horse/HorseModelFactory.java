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

import static com.ranchgame.horse.Organic.ball;
import static com.ranchgame.horse.Organic.ballX;
import static com.ranchgame.horse.Organic.ballZ;
import static com.ranchgame.horse.Organic.puck;

/**
 * A lifelike horse sculpted from smooth overlapping ellipsoids: barrel chest,
 * arched neck, real head with eyes and nostrils, jointed tapering legs and a
 * flowing mane and tail. Node ids and pivots match the old blocky model
 * (body, neck, tail, legFL/FR/BL/BR, rider) so all animation keeps working,
 * and material ids match so the customization console recolors everything.
 */
public final class HorseModelFactory {

    private static final long ATTRS = Usage.Position | Usage.Normal;
    private static final long ATTRS_TEX = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
    private static final Color DETAIL = new Color(0.09f, 0.07f, 0.06f, 1f);

    private HorseModelFactory() {
    }

    public static Model create(Color bodyColor, Color maneColor, boolean withRider, Texture coat) {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        Color hoof = new Color(0.2f, 0.15f, 0.12f, 1f);
        Color muzzle = new Color(bodyColor).lerp(Color.WHITE, 0.35f);

        // --- Body: chest, barrel and hindquarters --------------------------
        Node body = mb.node();
        body.id = "body";
        body.translation.set(0f, 1.02f, 0f);
        MeshPartBuilder b = mb.part("body", GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_body", coat, bodyColor));
        ball(b, 0f, 0.16f, -0.05f, 0.34f, 0.40f, 0.62f);   // barrel
        ball(b, 0f, 0.12f, 0.42f, 0.29f, 0.36f, 0.34f);    // chest/shoulders
        ball(b, 0f, 0.20f, -0.52f, 0.32f, 0.38f, 0.38f);   // hindquarters
        ball(b, 0f, 0.40f, 0.30f, 0.16f, 0.16f, 0.24f);    // withers

        if (withRider) {
            MeshPartBuilder pad = mb.part("saddlePad", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_cloth", new Color(0.85f, 0.1f, 0.15f, 1f)));
            ball(pad, 0f, 0.48f, -0.04f, 0.33f, 0.07f, 0.42f);
            MeshPartBuilder sd = mb.part("saddle", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_leather", new Color(0.45f, 0.2f, 0.12f, 1f)));
            ball(sd, 0f, 0.55f, -0.06f, 0.19f, 0.09f, 0.30f);   // seat
            ball(sd, 0f, 0.62f, -0.24f, 0.10f, 0.08f, 0.07f);   // cantle
            ball(sd, 0f, 0.62f, 0.13f, 0.09f, 0.08f, 0.06f);    // pommel
            // girth strap around the barrel
            ballZ(sd, 0.30f, 0.18f, 0.06f, 0.03f, 0.30f, 0.09f, 8f);
            ballZ(sd, -0.30f, 0.18f, 0.06f, 0.03f, 0.30f, 0.09f, -8f);
        }

        // --- Neck + head (pivot at the neck base) --------------------------
        Node neck = mb.node();
        neck.id = "neck";
        neck.translation.set(0f, 1.25f, 0.55f);
        MeshPartBuilder n = mb.part("neck", GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_neck", coat, bodyColor));
        ballX(n, 0f, 0.05f, 0.10f, 0.16f, 0.28f, 0.22f, 42f);   // neck base
        ballX(n, 0f, 0.30f, 0.30f, 0.13f, 0.26f, 0.17f, 40f);   // mid neck
        ballX(n, 0f, 0.50f, 0.47f, 0.11f, 0.20f, 0.14f, 34f);   // upper neck
        ballX(n, 0f, 0.66f, 0.60f, 0.12f, 0.15f, 0.22f, -25f);  // skull
        ballX(n, 0f, 0.60f, 0.52f, 0.10f, 0.12f, 0.15f, -20f);  // cheek/jaw
        MeshPartBuilder mz = mb.part("muzzle", GL20.GL_TRIANGLES, ATTRS, mat("muzzle", muzzle));
        ballX(mz, 0f, 0.565f, 0.78f, 0.07f, 0.085f, 0.14f, -30f);
        MeshPartBuilder det = mb.part("detail", GL20.GL_TRIANGLES, ATTRS, mat("detail", DETAIL));
        ball(det, 0.105f, 0.68f, 0.64f, 0.03f, 0.036f, 0.03f);     // eye R
        ball(det, -0.105f, 0.68f, 0.64f, 0.03f, 0.036f, 0.03f);    // eye L
        ball(det, 0.04f, 0.545f, 0.875f, 0.016f, 0.02f, 0.016f);   // nostril R
        ball(det, -0.04f, 0.545f, 0.875f, 0.016f, 0.02f, 0.016f);  // nostril L
        MeshPartBuilder mn = mb.part("mane", GL20.GL_TRIANGLES, ATTRS, mat("mane", maneColor));
        ballZ(mn, 0.05f, 0.84f, 0.50f, 0.028f, 0.09f, 0.05f, -16f);   // ear R
        ballZ(mn, -0.05f, 0.84f, 0.50f, 0.028f, 0.09f, 0.05f, 16f);   // ear L
        ballX(mn, 0f, 0.76f, 0.62f, 0.055f, 0.05f, 0.10f, -25f);      // forelock
        // mane lumps flowing down the crest
        ballX(mn, 0f, 0.70f, 0.44f, 0.055f, 0.13f, 0.09f, 35f);
        ballX(mn, 0f, 0.50f, 0.28f, 0.06f, 0.15f, 0.09f, 38f);
        ballX(mn, 0f, 0.28f, 0.12f, 0.06f, 0.15f, 0.09f, 40f);
        ballX(mn, 0f, 0.06f, -0.05f, 0.055f, 0.14f, 0.09f, 42f);

        // --- Tail ----------------------------------------------------------
        Node tail = mb.node();
        tail.id = "tail";
        tail.translation.set(0f, 1.5f, -0.88f);
        MeshPartBuilder t = mb.part("tail", GL20.GL_TRIANGLES, ATTRS, mat("mane_tail", maneColor));
        ballX(t, 0f, -0.06f, -0.02f, 0.075f, 0.16f, 0.09f, 18f);
        ballX(t, 0f, -0.3f, -0.09f, 0.075f, 0.24f, 0.09f, 8f);
        ballX(t, 0f, -0.62f, -0.13f, 0.06f, 0.22f, 0.07f, 4f);
        ballX(t, 0f, -0.88f, -0.15f, 0.045f, 0.16f, 0.05f, 0f);

        // --- Legs (pivot at shoulder / hip) --------------------------------
        buildLeg(mb, "legFL", -0.20f, 0.45f, false, bodyColor, hoof, coat);
        buildLeg(mb, "legFR", 0.20f, 0.45f, false, bodyColor, hoof, coat);
        buildLeg(mb, "legBL", -0.21f, -0.52f, true, bodyColor, hoof, coat);
        buildLeg(mb, "legBR", 0.21f, -0.52f, true, bodyColor, hoof, coat);

        // --- Rider: a girl in the saddle -----------------------------------
        if (withRider) {
            Node rider = mb.node();
            rider.id = "rider";
            rider.translation.set(0f, 1.56f, -0.05f);

            MeshPartBuilder pants = mb.part("riderLegs", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_pants", new Color(0.2f, 0.25f, 0.45f, 1f)));
            ball(pants, 0f, 0.08f, -0.02f, 0.15f, 0.12f, 0.13f);          // hips
            ballX(pants, 0.20f, 0.02f, 0.14f, 0.085f, 0.10f, 0.24f, -18f); // thigh R
            ballX(pants, -0.20f, 0.02f, 0.14f, 0.085f, 0.10f, 0.24f, -18f);// thigh L
            ballX(pants, 0.28f, -0.22f, 0.16f, 0.055f, 0.19f, 0.065f, 12f);// calf R
            ballX(pants, -0.28f, -0.22f, 0.16f, 0.055f, 0.19f, 0.065f, 12f);// calf L
            MeshPartBuilder boots = mb.part("riderBoots", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_boots", new Color(0.3f, 0.18f, 0.1f, 1f)));
            ball(boots, 0.29f, -0.42f, 0.20f, 0.05f, 0.07f, 0.12f);
            ball(boots, -0.29f, -0.42f, 0.20f, 0.05f, 0.07f, 0.12f);

            MeshPartBuilder shirt = mb.part("riderTorso", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_shirt", new Color(0.25f, 0.55f, 0.85f, 1f)));
            ball(shirt, 0f, 0.30f, 0f, 0.145f, 0.26f, 0.115f);            // torso
            ballX(shirt, 0.185f, 0.36f, 0.03f, 0.05f, 0.16f, 0.055f, -20f); // upper arm R
            ballX(shirt, -0.185f, 0.36f, 0.03f, 0.05f, 0.16f, 0.055f, -20f);// upper arm L
            MeshPartBuilder skin = mb.part("riderSkin", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
            ballX(skin, 0.19f, 0.17f, 0.16f, 0.042f, 0.14f, 0.05f, -42f);  // forearm R
            ballX(skin, -0.19f, 0.17f, 0.16f, 0.042f, 0.14f, 0.05f, -42f); // forearm L
            ball(skin, 0.17f, 0.07f, 0.27f, 0.045f, 0.045f, 0.05f);        // hand R
            ball(skin, -0.17f, 0.07f, 0.27f, 0.045f, 0.045f, 0.05f);       // hand L
            ball(skin, 0f, 0.66f, 0.02f, 0.105f, 0.115f, 0.105f);          // head
            MeshPartBuilder rdet = mb.part("riderDetail", GL20.GL_TRIANGLES, ATTRS,
                    mat("detail", DETAIL));
            ball(rdet, 0.045f, 0.67f, 0.115f, 0.016f, 0.018f, 0.012f);     // eye R
            ball(rdet, -0.045f, 0.67f, 0.115f, 0.016f, 0.018f, 0.012f);    // eye L
            MeshPartBuilder hair = mb.part("riderHair", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_hair", new Color(0.28f, 0.18f, 0.1f, 1f)));
            ball(hair, 0f, 0.70f, -0.02f, 0.115f, 0.11f, 0.115f);          // hair cap
            ballX(hair, 0f, 0.55f, -0.14f, 0.055f, 0.15f, 0.06f, 18f);     // ponytail
            ballX(hair, 0f, 0.38f, -0.19f, 0.04f, 0.11f, 0.045f, 10f);     // ponytail tip
        }

        return mb.end();
    }

    private static void buildLeg(ModelBuilder mb, String id, float x, float z, boolean hind,
                                 Color bodyColor, Color hoofColor, Texture coat) {
        Node leg = mb.node();
        leg.id = id;
        leg.translation.set(x, 0.95f, z);
        float bulk = hind ? 1.25f : 1f;
        MeshPartBuilder l = mb.part(id, GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_" + id, coat, bodyColor));
        ball(l, 0f, -0.12f, hind ? -0.03f : 0f, 0.10f * bulk, 0.28f, 0.12f * bulk); // thigh/forearm
        ball(l, 0f, -0.42f, 0f, 0.062f, 0.09f, 0.07f);                              // knee/hock
        ball(l, 0f, -0.62f, 0f, 0.048f, 0.20f, 0.052f);                             // cannon
        ball(l, 0f, -0.82f, 0.01f, 0.055f, 0.06f, 0.06f);                           // fetlock
        MeshPartBuilder h = mb.part(id + "Hoof", GL20.GL_TRIANGLES, ATTRS,
                mat("hoof_" + id, hoofColor));
        puck(h, 0f, -0.905f, 0.015f, 0.068f, 0.09f);
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
