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

/**
 * Shared face and helmet geometry for the girl, used by both the mounted
 * rider (inside the horse model) and the walking rider model so the two
 * always look identical. Parts attach to whichever node the ModelBuilder
 * currently has open; coordinates are relative to that node.
 */
final class RiderBits {

    private RiderBits() {
    }

    /** Eyes with irises, brows and a mouth, placed on a head centered at (cx,cy,cz). */
    static void buildFace(ModelBuilder mb, float cx, float cy, float cz) {
        MeshPartBuilder sclera = mb.part("faceSclera", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("sclera", new Color(0.97f, 0.97f, 0.96f, 1f)));
        ballLow(sclera, cx + 0.042f, cy + 0.015f, cz + 0.085f, 0.02f, 0.023f, 0.012f);
        ballLow(sclera, cx - 0.042f, cy + 0.015f, cz + 0.085f, 0.02f, 0.023f, 0.012f);
        MeshPartBuilder iris = mb.part("faceIris", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("iris", new Color(0.3f, 0.48f, 0.78f, 1f)));
        ballLow(iris, cx + 0.042f, cy + 0.013f, cz + 0.098f, 0.011f, 0.013f, 0.006f);
        ballLow(iris, cx - 0.042f, cy + 0.013f, cz + 0.098f, 0.011f, 0.013f, 0.006f);
        MeshPartBuilder brows = mb.part("faceBrows", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("face_detail", new Color(0.32f, 0.22f, 0.14f, 1f)));
        ballLow(brows, cx + 0.045f, cy + 0.048f, cz + 0.083f, 0.027f, 0.006f, 0.012f);
        ballLow(brows, cx - 0.045f, cy + 0.048f, cz + 0.083f, 0.027f, 0.006f, 0.012f);
        MeshPartBuilder mouth = mb.part("faceMouth", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("mouth", new Color(0.8f, 0.48f, 0.45f, 1f)));
        ballLow(mouth, cx, cy - 0.048f, cz + 0.088f, 0.022f, 0.008f, 0.012f);
    }

    /** Helmet dome + brim (customizable color) with a cream center stripe and badge. */
    static void buildHelmet(ModelBuilder mb, float cx, float cy, float cz) {
        MeshPartBuilder helm = mb.part("helmetDome", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("helmet", new Color(0.9f, 0.85f, 0.8f, 1f)));
        ball(helm, cx, cy, cz, 0.117f, 0.10f, 0.122f);
        ballLowX(helm, cx, cy - 0.04f, cz + 0.115f, 0.088f, 0.02f, 0.07f, -8f);
        MeshPartBuilder trimP = mb.part("helmetTrim", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal, mat("helmet_trim", new Color(0.95f, 0.9f, 0.82f, 1f)));
        ball(trimP, cx, cy + 0.004f, cz, 0.028f, 0.103f, 0.125f);   // center stripe
        ballLow(trimP, cx, cy - 0.012f, cz + 0.12f, 0.016f, 0.016f, 0.008f); // badge
    }

    private static Material mat(String id, Color c) {
        return new Material(id, ColorAttribute.createDiffuse(c));
    }
}
