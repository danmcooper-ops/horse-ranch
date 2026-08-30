package com.ranchgame.horse;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import static com.ranchgame.horse.Organic.ball;
import static com.ranchgame.horse.Organic.ballLow;
import static com.ranchgame.horse.Organic.ballLowX;
import static com.ranchgame.horse.Organic.ballZ;

/**
 * Shared face and helmet geometry for the girl, used by both the mounted
 * rider (inside the horse model) and the walking rider model so the two
 * always look identical. Parts attach to whichever node the ModelBuilder
 * currently has open; coordinates are relative to that node.
 */
final class RiderBits {

    private static final Color SKIN = new Color(0.94f, 0.78f, 0.62f, 1f);

    private RiderBits() {
    }

    /**
     * Eyes with irises and pupils, brows, nose, ears and a mouth, placed on
     * a head centered at (cx,cy,cz).
     */
    static void buildFace(ModelBuilder mb, float cx, float cy, float cz) {
        MeshPartBuilder nose = mb.part("faceNose", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("rider_skin", SKIN));
        ballLow(nose, cx, cy - 0.012f, cz + 0.10f, 0.015f, 0.025f, 0.018f);
        MeshPartBuilder ears = mb.part("faceEars", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("rider_skin", SKIN));
        ballLow(ears, cx + 0.102f, cy - 0.005f, cz + 0.005f, 0.012f, 0.028f, 0.022f);
        ballLow(ears, cx - 0.102f, cy - 0.005f, cz + 0.005f, 0.012f, 0.028f, 0.022f);

        MeshPartBuilder sclera = mb.part("faceSclera", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("sclera", new Color(0.97f, 0.97f, 0.96f, 1f)));
        ballLow(sclera, cx + 0.042f, cy + 0.015f, cz + 0.083f, 0.016f, 0.019f, 0.011f);
        ballLow(sclera, cx - 0.042f, cy + 0.015f, cz + 0.083f, 0.016f, 0.019f, 0.011f);
        MeshPartBuilder iris = mb.part("faceIris", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("iris", new Color(0.3f, 0.48f, 0.78f, 1f)));
        ballLow(iris, cx + 0.042f, cy + 0.013f, cz + 0.092f, 0.010f, 0.012f, 0.006f);
        ballLow(iris, cx - 0.042f, cy + 0.013f, cz + 0.092f, 0.010f, 0.012f, 0.006f);
        MeshPartBuilder pupil = mb.part("facePupil", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("pupil", new Color(0.08f, 0.07f, 0.07f, 1f)));
        ballLow(pupil, cx + 0.042f, cy + 0.012f, cz + 0.097f, 0.005f, 0.006f, 0.004f);
        ballLow(pupil, cx - 0.042f, cy + 0.012f, cz + 0.097f, 0.005f, 0.006f, 0.004f);
        MeshPartBuilder brows = mb.part("faceBrows", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("face_detail", new Color(0.32f, 0.22f, 0.14f, 1f)));
        ballLow(brows, cx + 0.044f, cy + 0.044f, cz + 0.081f, 0.023f, 0.005f, 0.010f);
        ballLow(brows, cx - 0.044f, cy + 0.044f, cz + 0.081f, 0.023f, 0.005f, 0.010f);
        MeshPartBuilder mouth = mb.part("faceMouth", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("mouth", new Color(0.8f, 0.48f, 0.45f, 1f)));
        ballLow(mouth, cx, cy - 0.052f, cz + 0.088f, 0.019f, 0.007f, 0.010f);
    }

    /**
     * Riding helmet: velvet dome + small peak (customizable color) with a
     * cream center stripe, badge and a harness strapped under the jaw.
     */
    static void buildHelmet(ModelBuilder mb, float cx, float cy, float cz) {
        MeshPartBuilder helm = mb.part("helmetDome", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("helmet", new Color(0.15f, 0.14f, 0.14f, 1f)));
        ball(helm, cx, cy, cz, 0.112f, 0.095f, 0.118f);
        ballLowX(helm, cx, cy - 0.035f, cz + 0.11f, 0.075f, 0.016f, 0.055f, -10f);  // peak
        MeshPartBuilder trimP = mb.part("helmetTrim", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("helmet_trim", new Color(0.95f, 0.9f, 0.82f, 1f)));
        ball(trimP, cx, cy + 0.004f, cz, 0.026f, 0.098f, 0.121f);   // center stripe
        ballLow(trimP, cx, cy - 0.008f, cz + 0.115f, 0.015f, 0.015f, 0.008f); // badge
        MeshPartBuilder strap = mb.part("helmetStrap", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("helmet_strap", new Color(0.2f, 0.19f, 0.18f, 1f)));
        ballZ(strap, cx + 0.094f, cy - 0.115f, cz + 0.012f, 0.005f, 0.058f, 0.01f, -6f); // strap R
        ballZ(strap, cx - 0.094f, cy - 0.115f, cz + 0.012f, 0.005f, 0.058f, 0.01f, 6f);  // strap L
    }

    private static Material mat(String id, Color c) {
        return new Material(id, ColorAttribute.createDiffuse(c));
    }
}
