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
import com.badlogic.gdx.math.MathUtils;

import static com.ranchgame.horse.Organic.ball;
import static com.ranchgame.horse.Organic.ballLow;
import static com.ranchgame.horse.Organic.ballLowX;
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
        // proportions matched to a Quarter Horse conformation photo:
        // flat topline, belly line at ~half the withers height, sloped croup
        ball(b, 0f, 0.18f, -0.05f, 0.30f, 0.37f, 0.62f);   // barrel
        ball(b, 0f, 0.14f, 0.42f, 0.27f, 0.34f, 0.34f);    // chest/shoulders
        ball(b, 0f, 0.16f, -0.52f, 0.29f, 0.33f, 0.38f);   // hindquarters
        ball(b, 0f, 0.30f, -0.60f, 0.22f, 0.20f, 0.28f);   // rounded croup
        ball(b, 0f, 0.02f, 0f, 0.28f, 0.30f, 0.55f);       // underline (belly at ~half height)
        ball(b, 0f, 0.42f, 0.30f, 0.14f, 0.13f, 0.24f);    // withers
        ball(b, 0f, 0.30f, 0.36f, 0.20f, 0.24f, 0.26f);    // shoulder blend into the neck

        if (withRider) {
            MeshPartBuilder pad = mb.part("saddlePad", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_cloth", new Color(0.85f, 0.1f, 0.15f, 1f)));
            ball(pad, 0f, 0.46f, -0.04f, 0.33f, 0.07f, 0.42f);
            MeshPartBuilder sd = mb.part("saddle", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_leather", new Color(0.45f, 0.2f, 0.12f, 1f)));
            ball(sd, 0f, 0.53f, -0.06f, 0.19f, 0.09f, 0.30f);   // seat
            ball(sd, 0f, 0.60f, -0.24f, 0.10f, 0.08f, 0.07f);   // cantle
            ball(sd, 0f, 0.60f, 0.13f, 0.09f, 0.08f, 0.06f);    // pommel
            // girth strap around the barrel
            ballZ(sd, 0.30f, 0.18f, 0.06f, 0.03f, 0.30f, 0.09f, 8f);
            ballZ(sd, -0.30f, 0.18f, 0.06f, 0.03f, 0.30f, 0.09f, -8f);
            // stirrup leathers + metal irons
            ballZ(sd, 0.33f, 0.36f, 0.02f, 0.022f, 0.16f, 0.05f, 6f);
            ballZ(sd, -0.33f, 0.36f, 0.02f, 0.022f, 0.16f, 0.05f, -6f);
            MeshPartBuilder iron = mb.part("stirrups", GL20.GL_TRIANGLES, ATTRS,
                    mat("stirrup_metal", new Color(0.72f, 0.74f, 0.78f, 1f)));
            ballLow(iron, 0.34f, 0.20f, 0.02f, 0.045f, 0.055f, 0.02f);
            ballLow(iron, -0.34f, 0.20f, 0.02f, 0.045f, 0.055f, 0.02f);
        }

        // --- Neck + head (pivot at the neck base) --------------------------
        Node neck = mb.node();
        neck.id = "neck";
        neck.translation.set(0f, 1.25f, 0.55f);
        MeshPartBuilder n = mb.part("neck", GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_neck", coat, bodyColor));
        ballX(n, 0f, 0.05f, 0.10f, 0.145f, 0.28f, 0.22f, 42f);  // neck base
        ballX(n, 0f, 0.26f, 0.30f, 0.115f, 0.25f, 0.16f, 40f);  // mid neck
        ballX(n, 0f, 0.42f, 0.48f, 0.095f, 0.19f, 0.13f, 34f);  // upper neck
        ballX(n, 0f, 0.58f, 0.63f, 0.108f, 0.14f, 0.24f, 33f);  // skull, nose angled down
        ballX(n, 0f, 0.52f, 0.54f, 0.095f, 0.115f, 0.15f, 26f); // cheek/jaw
        MeshPartBuilder mz = mb.part("muzzle", GL20.GL_TRIANGLES, ATTRS, mat("muzzle", muzzle));
        ballX(mz, 0f, 0.475f, 0.795f, 0.062f, 0.08f, 0.14f, 40f);
        MeshPartBuilder det = mb.part("detail", GL20.GL_TRIANGLES, ATTRS, mat("detail", DETAIL));
        ball(det, 0.105f, 0.60f, 0.66f, 0.033f, 0.04f, 0.033f);    // eye R
        ball(det, -0.105f, 0.60f, 0.66f, 0.033f, 0.04f, 0.033f);   // eye L
        ball(det, 0.04f, 0.465f, 0.895f, 0.016f, 0.02f, 0.016f);   // nostril R
        ball(det, -0.04f, 0.465f, 0.895f, 0.016f, 0.02f, 0.016f);  // nostril L
        MeshPartBuilder glint = mb.part("eyeGlint", GL20.GL_TRIANGLES, ATTRS,
                mat("eye_glint", new Color(0.95f, 0.95f, 0.98f, 1f)));
        ballLow(glint, 0.118f, 0.615f, 0.675f, 0.01f, 0.012f, 0.01f);   // catchlight R
        ballLow(glint, -0.118f, 0.615f, 0.675f, 0.01f, 0.012f, 0.01f);  // catchlight L

        // face markings, toggled by the customize console via NodePart.enabled
        MeshPartBuilder star = mb.part("markingStar", GL20.GL_TRIANGLES, ATTRS,
                mat("marking", new Color(0.96f, 0.95f, 0.92f, 1f)));
        ballLowX(star, 0f, 0.675f, 0.73f, 0.045f, 0.05f, 0.02f, -57f);
        MeshPartBuilder blaze = mb.part("markingBlaze", GL20.GL_TRIANGLES, ATTRS,
                mat("marking", new Color(0.96f, 0.95f, 0.92f, 1f)));
        ballLowX(blaze, 0f, 0.67f, 0.735f, 0.035f, 0.085f, 0.025f, -57f);
        ballLowX(blaze, 0f, 0.55f, 0.82f, 0.03f, 0.09f, 0.028f, -50f);

        if (withRider) {
            // bridle: flat straps that hug the head, plus thin rein ropes
            MeshPartBuilder br = mb.part("bridle", GL20.GL_TRIANGLES, ATTRS,
                    mat("tack_leather_bridle", new Color(0.45f, 0.2f, 0.12f, 1f)));
            ballLowX(br, 0f, 0.495f, 0.81f, 0.082f, 0.022f, 0.152f, -50f);     // noseband
            ballLowX(br, 0.093f, 0.575f, 0.70f, 0.011f, 0.065f, 0.013f, -53f); // cheek strap R
            ballLowX(br, -0.093f, 0.575f, 0.70f, 0.011f, 0.065f, 0.013f, -53f);// cheek strap L
            ballLow(br, 0f, 0.67f, 0.575f, 0.10f, 0.012f, 0.035f);             // browband
            // reins: thin rope segments hugging the neck sides down to the withers
            for (int i = 0; i < 8; i++) {
                float ft = i / 7f;
                float sag = MathUtils.sin(ft * MathUtils.PI) * 0.05f;
                float y = 0.47f * (1f - ft) + 0.16f * ft - sag;
                float z = 0.80f * (1f - ft) + 0.06f * ft;
                float x = 0.09f + 0.035f * MathUtils.sin(ft * MathUtils.PI);
                ballLowX(br, x, y, z, 0.009f, 0.014f, 0.055f, -28f);
                ballLowX(br, -x, y, z, 0.009f, 0.014f, 0.055f, -28f);
            }
        }
        MeshPartBuilder mn = mb.part("mane", GL20.GL_TRIANGLES, ATTRS, mat("mane", maneColor));
        ballZ(mn, 0.05f, 0.76f, 0.52f, 0.028f, 0.09f, 0.05f, -16f);   // ear R
        ballZ(mn, -0.05f, 0.76f, 0.52f, 0.028f, 0.09f, 0.05f, 16f);   // ear L
        ball(mn, 0f, 0.70f, 0.62f, 0.042f, 0.045f, 0.05f);            // braided forelock knob
        // button braids: a row of tight knobs along the crest
        for (int i = 0; i < 8; i++) {
            float ft = i / 7f;
            float y = 0.64f - 0.60f * ft;
            float z = 0.44f - 0.51f * ft;
            ball(mn, 0f, y, z, 0.05f, 0.058f, 0.052f);
        }

        // --- Tail ----------------------------------------------------------
        Node tail = mb.node();
        tail.id = "tail";
        tail.translation.set(0f, 1.44f, -0.86f);
        MeshPartBuilder t = mb.part("tail", GL20.GL_TRIANGLES, ATTRS, mat("mane_tail", maneColor));
        // braided tail: a tapering column of knots with a tassel at the tip
        ball(t, 0f, -0.06f, -0.03f, 0.075f, 0.09f, 0.08f);
        ball(t, 0f, -0.22f, -0.07f, 0.068f, 0.09f, 0.072f);
        ball(t, 0f, -0.38f, -0.10f, 0.06f, 0.085f, 0.064f);
        ball(t, 0f, -0.53f, -0.12f, 0.052f, 0.08f, 0.056f);
        ball(t, 0f, -0.67f, -0.135f, 0.045f, 0.075f, 0.048f);
        ballX(t, 0f, -0.82f, -0.145f, 0.036f, 0.1f, 0.038f, 2f);   // tassel

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
            ballX(boots, 0.285f, -0.30f, 0.165f, 0.052f, 0.115f, 0.06f, 10f);  // shaft R
            ballX(boots, -0.285f, -0.30f, 0.165f, 0.052f, 0.115f, 0.06f, 10f); // shaft L
            MeshPartBuilder bootTop = mb.part("bootTops", GL20.GL_TRIANGLES, ATTRS,
                    mat("boot_top", new Color(0.9f, 0.85f, 0.78f, 1f)));
            ballLow(bootTop, 0.285f, -0.185f, 0.15f, 0.054f, 0.024f, 0.062f);
            ballLow(bootTop, -0.285f, -0.185f, 0.15f, 0.054f, 0.024f, 0.062f);

            MeshPartBuilder shirt = mb.part("riderTorso", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_shirt", new Color(0.45f, 0.55f, 0.72f, 1f)));
            ball(shirt, 0f, 0.30f, 0f, 0.145f, 0.26f, 0.115f);            // jacket torso
            ball(shirt, 0f, 0.06f, -0.02f, 0.165f, 0.09f, 0.135f);        // jacket skirt flare
            ballX(shirt, 0.185f, 0.36f, 0.03f, 0.05f, 0.16f, 0.055f, -20f); // upper arm R
            ballX(shirt, -0.185f, 0.36f, 0.03f, 0.05f, 0.16f, 0.055f, -20f);// upper arm L
            MeshPartBuilder trim = mb.part("jacketTrim", GL20.GL_TRIANGLES, ATTRS,
                    mat("jacket_trim", new Color(0.93f, 0.85f, 0.78f, 1f)));
            ball(trim, 0f, 0.545f, 0.01f, 0.066f, 0.015f, 0.062f);          // shirt collar
            ballLow(trim, 0.19f, 0.225f, 0.10f, 0.052f, 0.028f, 0.056f);    // cuff R
            ballLow(trim, -0.19f, 0.225f, 0.10f, 0.052f, 0.028f, 0.056f);   // cuff L
            MeshPartBuilder buttons = mb.part("jacketButtons", GL20.GL_TRIANGLES, ATTRS,
                    mat("buttons", new Color(0.82f, 0.66f, 0.32f, 1f)));
            ballLow(buttons, 0f, 0.42f, 0.118f, 0.013f, 0.013f, 0.008f);
            ballLow(buttons, 0f, 0.34f, 0.121f, 0.013f, 0.013f, 0.008f);
            ballLow(buttons, 0f, 0.26f, 0.118f, 0.013f, 0.013f, 0.008f);
            MeshPartBuilder skin = mb.part("riderSkin", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_skin", new Color(0.94f, 0.78f, 0.62f, 1f)));
            ballX(skin, 0.19f, 0.17f, 0.16f, 0.042f, 0.14f, 0.05f, -42f);  // forearm R
            ballX(skin, -0.19f, 0.17f, 0.16f, 0.042f, 0.14f, 0.05f, -42f); // forearm L
            ball(skin, 0f, 0.545f, 0.01f, 0.042f, 0.06f, 0.042f);          // neck
            ball(skin, 0f, 0.66f, 0.02f, 0.095f, 0.115f, 0.10f);           // head
            MeshPartBuilder glove = mb.part("gloves", GL20.GL_TRIANGLES, ATTRS,
                    mat("gloves", new Color(0.92f, 0.9f, 0.86f, 1f)));
            ballLow(glove, 0.17f, 0.07f, 0.27f, 0.045f, 0.045f, 0.05f);    // hand R
            ballLow(glove, -0.17f, 0.07f, 0.27f, 0.045f, 0.045f, 0.05f);   // hand L
            RiderBits.buildFace(mb, 0f, 0.66f, 0.02f);
            MeshPartBuilder hair = mb.part("riderHair", GL20.GL_TRIANGLES, ATTRS,
                    mat("rider_hair", new Color(0.28f, 0.18f, 0.1f, 1f)));
            ball(hair, 0f, 0.675f, -0.01f, 0.097f, 0.11f, 0.098f);         // hair cap under the helmet
            ballX(hair, 0f, 0.55f, -0.14f, 0.055f, 0.15f, 0.06f, 18f);     // ponytail
            ballX(hair, 0f, 0.38f, -0.19f, 0.04f, 0.11f, 0.045f, 10f);     // ponytail tip
            RiderBits.buildHelmet(mb, 0f, 0.735f, 0f);
        }

        return mb.end();
    }

    private static void buildLeg(ModelBuilder mb, String id, float x, float z, boolean hind,
                                 Color bodyColor, Color hoofColor, Texture coat) {
        Node leg = mb.node();
        leg.id = id;
        leg.translation.set(x, 0.95f, z);
        float bulk = hind ? 1.2f : 1f;
        // upper leg: pivots at the shoulder/hip
        MeshPartBuilder l = mb.part(id, GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_" + id, coat, bodyColor));
        ball(l, 0f, -0.12f, hind ? -0.03f : 0f, 0.09f * bulk, 0.28f, 0.11f * bulk); // thigh/forearm
        ball(l, 0f, -0.42f, 0f, 0.058f, 0.09f, 0.065f);                             // knee/hock

        // lower leg: its own node pivoted AT the knee so the animator can fold it.
        // The animator recomputes this node's translation each frame from the hip
        // swing; mesh coordinates here are knee-local (knee sits 0.42 below the hip).
        Node low = mb.node();
        low.id = "low" + id.substring(3);   // legFL -> lowFL
        low.translation.set(leg.translation.x, leg.translation.y - 0.42f, leg.translation.z);
        MeshPartBuilder c = mb.part("low" + id, GL20.GL_TRIANGLES, ATTRS_TEX,
                coatMat("coat_" + id + "_low", coat, bodyColor));
        ball(c, 0f, -0.20f, 0f, 0.048f, 0.20f, 0.052f);                             // cannon
        ball(c, 0f, -0.40f, 0.01f, 0.055f, 0.06f, 0.06f);                           // fetlock
        MeshPartBuilder w = mb.part("wrap_" + id, GL20.GL_TRIANGLES, ATTRS,
                mat("wraps", new Color(0.93f, 0.72f, 0.72f, 1f)));
        ballLow(w, 0f, -0.14f, 0f, 0.057f, 0.05f, 0.061f);
        ballLow(w, 0f, -0.22f, 0f, 0.058f, 0.05f, 0.062f);
        ballLow(w, 0f, -0.30f, 0f, 0.06f, 0.05f, 0.064f);
        MeshPartBuilder h = mb.part(id + "Hoof", GL20.GL_TRIANGLES, ATTRS,
                mat("hoof_" + id, hoofColor));
        puck(h, 0f, -0.485f, 0.015f, 0.068f, 0.09f);
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
        ta.scaleU = 1f;
        ta.scaleV = 1f;
        return new Material(id, ta, ColorAttribute.createDiffuse(tint));
    }
}
