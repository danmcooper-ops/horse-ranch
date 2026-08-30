package com.ranchgame.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.RandomXS128;

/** Builds the whole static ranch (ground, fences, barn, trees, props) procedurally. */
public final class WorldBuilder {

    public static final float HALF = 110f;          // fenced area is (-HALF..HALF)^2
    private static final long ATTRS = Usage.Position | Usage.Normal;
    private static final long ATTRS_TEX = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

    private WorldBuilder() {
    }

    public static RanchWorld build(ProceduralTextures tex) {
        RanchWorld world = new RanchWorld();
        ModelBuilder mb = new ModelBuilder();
        RandomXS128 rng = new RandomXS128(20260722L);
        Matrix4 tr = new Matrix4();
        mb.begin();

        // --- Ground + decorative patches ---------------------------------
        MeshPartBuilder grass = mb.part("grass", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.grass, Color.WHITE, 44f, 44f));
        BoxShapeBuilder.build(grass, 0f, -0.05f, 0f, 2f * HALF + 30f, 0.1f, 2f * HALF + 30f);

        MeshPartBuilder patches = mb.part("patches", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.grass, new Color(0.88f, 0.94f, 0.82f, 1f), 3f, 3f));
        for (int i = 0; i < 40; i++) {
            float x = rng.nextFloat() * 200f - 100f;
            float z = rng.nextFloat() * 200f - 100f;
            float w = 3f + rng.nextFloat() * 7f;
            float d = 3f + rng.nextFloat() * 7f;
            BoxShapeBuilder.build(patches, x, 0.005f, z, w, 0.01f, d);
        }

        // Cobblestone paths from spawn toward the barn and the course area
        // (top-face UV: U runs along the box's Z axis)
        MeshPartBuilder path = mb.part("trail", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.cobble, Color.WHITE, 26f, 1.5f));
        BoxShapeBuilder.build(path, 0f, 0.008f, -20f, 3.4f, 0.012f, 60f);
        MeshPartBuilder path2 = mb.part("trail2", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.cobble, Color.WHITE, 1.5f, 14f));
        BoxShapeBuilder.build(path2, -16f, 0.008f, -20f, 32f, 0.012f, 3.4f);

        // --- Perimeter fence ----------------------------------------------
        MeshPartBuilder posts = mb.part("fencePosts", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, Color.WHITE, 1f, 1f));
        MeshPartBuilder rails = mb.part("fenceRails", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, new Color(1f, 0.96f, 0.9f, 1f), 26f, 1f));
        for (int side = 0; side < 4; side++) {
            boolean horizontal = side < 2;              // along X at z = +/-HALF
            float fixed = side % 2 == 0 ? HALF : -HALF;
            for (float p = -HALF; p <= HALF; p += 5.5f) {
                // chunky posts with a slight random hand-set tilt
                float tilt = (rng.nextFloat() - 0.5f) * 4f;
                if (horizontal) {
                    posts.setVertexTransform(tr.setToTranslation(p, 0.7f, fixed).rotate(0f, 0f, 1f, tilt));
                    BoxShapeBuilder.build(posts, 0f, 0f, 0f, 0.26f, 1.5f, 0.26f);
                } else {
                    posts.setVertexTransform(tr.setToTranslation(fixed, 0.7f, p).rotate(1f, 0f, 0f, tilt));
                    BoxShapeBuilder.build(posts, 0f, 0f, 0f, 0.26f, 1.5f, 0.26f);
                }
            }
            posts.setVertexTransform(null);
            for (int rail = 0; rail < 3; rail++) {
                float y = 0.38f + rail * 0.42f;
                if (horizontal) BoxShapeBuilder.build(rails, 0f, y, fixed, 2f * HALF, 0.14f, 0.11f);
                else BoxShapeBuilder.build(rails, fixed, y, 0f, 0.11f, 0.14f, 2f * HALF);
            }
        }
        world.addObstacle(0f, HALF, 2f * HALF + 1f, 0.8f);
        world.addObstacle(0f, -HALF, 2f * HALF + 1f, 0.8f);
        world.addObstacle(HALF, 0f, 0.8f, 2f * HALF + 1f);
        world.addObstacle(-HALF, 0f, 0.8f, 2f * HALF + 1f);

        // --- Barn ----------------------------------------------------------
        float bx = -32f, bz = -20f;
        MeshPartBuilder barn = mb.part("barn", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.planks, Color.WHITE, 3f, 1.4f));
        BoxShapeBuilder.build(barn, bx, 2.5f, bz, 10f, 5f, 14f);
        MeshPartBuilder roof = mb.part("roof", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.shingles, Color.WHITE, 4f, 2f));
        roof.setVertexTransform(tr.setToTranslation(bx - 2.7f, 5.9f, bz).rotate(0f, 0f, 1f, 35f));
        BoxShapeBuilder.build(roof, 0f, 0f, 0f, 6.6f, 0.25f, 14.8f);
        roof.setVertexTransform(tr.setToTranslation(bx + 2.7f, 5.9f, bz).rotate(0f, 0f, 1f, -35f));
        BoxShapeBuilder.build(roof, 0f, 0f, 0f, 6.6f, 0.25f, 14.8f);
        roof.setVertexTransform(null);
        MeshPartBuilder door = mb.part("barnDoor", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, new Color(0.55f, 0.38f, 0.28f, 1f), 4f, 3f));
        BoxShapeBuilder.build(door, bx, 1.6f, bz + 7.02f, 3.6f, 3.2f, 0.1f);
        MeshPartBuilder trim = mb.part("barnTrim", GL20.GL_TRIANGLES, ATTRS, mat(Color.WHITE));
        BoxShapeBuilder.build(trim, bx, 3.6f, bz + 7.02f, 4.2f, 0.3f, 0.1f);
        world.addObstacle(bx, bz, 10.4f, 14.4f);

        // Hay bales + water trough near the barn
        MeshPartBuilder hay = mb.part("hay", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.coat, new Color(0.87f, 0.74f, 0.36f, 1f), 2f, 2f));
        BoxShapeBuilder.build(hay, -24f, 0.5f, -10f, 1.8f, 1f, 1.2f);
        BoxShapeBuilder.build(hay, -22.2f, 0.5f, -10.6f, 1.8f, 1f, 1.2f);
        BoxShapeBuilder.build(hay, -23.1f, 1.4f, -10.3f, 1.7f, 0.9f, 1.1f);
        world.addObstacle(-23.1f, -10.3f, 4f, 2f);
        MeshPartBuilder trough = mb.part("trough", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.5f, 0.52f, 0.55f, 1f)));
        BoxShapeBuilder.build(trough, -24f, 0.35f, -26f, 2.6f, 0.7f, 1.1f);
        MeshPartBuilder water = mb.part("water", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.35f, 0.6f, 0.85f, 1f)));
        BoxShapeBuilder.build(water, -24f, 0.62f, -26f, 2.3f, 0.1f, 0.8f);
        world.addObstacle(-24f, -26f, 3f, 1.5f);

        // --- Trees: lush two-tone canopies on tapered trunks ---------------
        MeshPartBuilder trunks = mb.part("trunks", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, new Color(0.62f, 0.5f, 0.42f, 1f), 1f, 2f));
        MeshPartBuilder leaves = mb.part("leaves", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.leaves, new Color(0.78f, 0.82f, 0.62f, 1f), 2f, 1.5f));
        MeshPartBuilder leavesHi = mb.part("leavesHi", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.leaves, new Color(1.08f, 1.2f, 0.82f, 1f), 2.4f, 1.8f));
        int planted = 0;
        while (planted < 26) {
            float x = rng.nextFloat() * 210f - 105f;
            float z = rng.nextFloat() * 210f - 105f;
            // keep the course paddock (east), spawn trail and barn area clear
            if (x > 2f && x < 62f && z > -32f && z < 40f) continue;
            if (Math.abs(x) < 4f && z > -55f && z < 12f) continue;
            if (x > -42f && x < -14f && z > -32f && z < -4f) continue;
            boolean tall = planted % 5 == 0;
            float h = (tall ? 3.6f : 2.2f) + rng.nextFloat() * 1.4f;
            // tapered trunk: wide base, slimmer upper half
            trunks.setVertexTransform(tr.setToTranslation(x, h * 0.3f, z));
            CylinderShapeBuilder.build(trunks, 0.62f, h * 0.62f, 0.62f, 10);
            trunks.setVertexTransform(tr.setToTranslation(x, h * 0.75f, z));
            CylinderShapeBuilder.build(trunks, 0.42f, h * 0.55f, 0.42f, 10);
            // canopy: dark leafy mass with sunlit highlight blobs on top
            float cs = tall ? 1.25f : 1f;
            leaves.setVertexTransform(tr.setToTranslation(x, h + 1.4f * cs, z));
            SphereShapeBuilder.build(leaves, 3.8f * cs, 2.9f * cs, 3.8f * cs, 14, 10);
            leaves.setVertexTransform(tr.setToTranslation(x + 1.2f * cs, h + 1.9f * cs, z + 0.6f * cs));
            SphereShapeBuilder.build(leaves, 2.5f * cs, 2f * cs, 2.5f * cs, 12, 8);
            leaves.setVertexTransform(tr.setToTranslation(x - 1.1f * cs, h + 2f * cs, z - 0.5f * cs));
            SphereShapeBuilder.build(leaves, 2.3f * cs, 1.9f * cs, 2.3f * cs, 12, 8);
            leaves.setVertexTransform(tr.setToTranslation(x - 0.2f * cs, h + 1.2f * cs, z + 1.3f * cs));
            SphereShapeBuilder.build(leaves, 2.2f * cs, 1.7f * cs, 2.2f * cs, 12, 8);
            // sunlit tops (key light comes from +x,+z-ish)
            leavesHi.setVertexTransform(tr.setToTranslation(x + 0.8f * cs, h + 2.7f * cs, z + 0.7f * cs));
            SphereShapeBuilder.build(leavesHi, 2f * cs, 1.4f * cs, 2f * cs, 12, 8);
            leavesHi.setVertexTransform(tr.setToTranslation(x - 0.7f * cs, h + 2.9f * cs, z - 0.3f * cs));
            SphereShapeBuilder.build(leavesHi, 1.6f * cs, 1.1f * cs, 1.6f * cs, 10, 7);
            world.addObstacle(x, z, 1.2f, 1.2f);
            planted++;
        }
        trunks.setVertexTransform(null);
        leaves.setVertexTransform(null);
        leavesHi.setVertexTransform(null);

        // --- Flowers and rocks ---------------------------------------------
        MeshPartBuilder petals = mb.part("petals", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.97f, 0.95f, 0.9f, 1f)));
        MeshPartBuilder centers = mb.part("flowerCenters", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.98f, 0.8f, 0.25f, 1f)));
        for (int i = 0; i < 60; i++) {
            float x = rng.nextFloat() * 205f - 102f;
            float z = rng.nextFloat() * 205f - 102f;
            if (Math.abs(x) < 4f && z > -55f && z < 12f) continue;
            boolean yellow = rng.nextFloat() < 0.4f;
            float s = 0.05f + rng.nextFloat() * 0.03f;
            MeshPartBuilder p = yellow ? centers : petals;
            for (int k = 0; k < 4; k++) {
                float a = k * MathUtils.PI2 / 4f;
                p.setVertexTransform(tr.setToTranslation(
                        x + MathUtils.cos(a) * s * 1.2f, 0.16f, z + MathUtils.sin(a) * s * 1.2f));
                SphereShapeBuilder.build(p, s * 1.6f, s * 0.8f, s * 1.6f, 6, 5);
            }
            MeshPartBuilder c = yellow ? petals : centers;
            c.setVertexTransform(tr.setToTranslation(x, 0.18f, z));
            SphereShapeBuilder.build(c, s * 1.1f, s * 0.9f, s * 1.1f, 6, 5);
        }
        petals.setVertexTransform(null);
        centers.setVertexTransform(null);

        MeshPartBuilder rocks = mb.part("rocks", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.58f, 0.58f, 0.6f, 1f)));
        for (int i = 0; i < 14; i++) {
            float x = rng.nextFloat() * 200f - 100f;
            float z = rng.nextFloat() * 200f - 100f;
            if (x > 2f && x < 62f && z > -32f && z < 40f) continue;
            if (Math.abs(x) < 5f && z > -55f && z < 12f) continue;
            if (x > -42f && x < -14f && z > -32f && z < -4f) continue;
            float s = 0.35f + rng.nextFloat() * 0.6f;
            rocks.setVertexTransform(tr.setToTranslation(x, s * 0.4f, z)
                    .rotate(0f, 1f, 0f, rng.nextFloat() * 90f));
            SphereShapeBuilder.build(rocks, s * 2.2f, s * 1.1f, s * 1.6f, 10, 7);
            rocks.setVertexTransform(tr.setToTranslation(x + s * 0.8f, s * 0.28f, z + s * 0.3f));
            SphereShapeBuilder.build(rocks, s * 1.3f, s * 0.8f, s * 1.1f, 8, 6);
            world.addObstacle(x, z, s * 2f, s * 1.6f);
        }
        rocks.setVertexTransform(null);

        // --- Horizon: soft hills and far mountains beyond the fence --------
        MeshPartBuilder hills = mb.part("hills", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.34f, 0.5f, 0.28f, 1f)));
        for (int i = 0; i < 11; i++) {
            float a = i * MathUtils.PI2 / 11f + 0.2f;
            float r = 165f + rng.nextFloat() * 40f;
            float w = 50f + rng.nextFloat() * 45f;
            float hh = 10f + rng.nextFloat() * 12f;
            hills.setVertexTransform(tr.setToTranslation(MathUtils.cos(a) * r, -2f, MathUtils.sin(a) * r));
            SphereShapeBuilder.build(hills, w, hh * 2f, w * 0.75f, 14, 8);
        }
        hills.setVertexTransform(null);
        MeshPartBuilder mountains = mb.part("mountains", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.52f, 0.55f, 0.62f, 1f)));
        for (int i = 0; i < 5; i++) {
            float a = i * MathUtils.PI2 / 5f + 0.9f;
            float r = 255f + rng.nextFloat() * 30f;
            float w = 80f + rng.nextFloat() * 50f;
            float hh = 38f + rng.nextFloat() * 26f;
            mountains.setVertexTransform(tr.setToTranslation(MathUtils.cos(a) * r, 0f, MathUtils.sin(a) * r));
            ConeShapeBuilder.build(mountains, w, hh, w * 0.8f, 9);
        }
        mountains.setVertexTransform(null);

        // --- Grass tufts (visual only, no collision) -----------------------
        MeshPartBuilder tufts = mb.part("tufts", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.grass, new Color(1.45f, 1.55f, 1.2f, 1f), 0.6f, 0.6f));
        for (int i = 0; i < 150; i++) {
            float x = rng.nextFloat() * 212f - 106f;
            float z = rng.nextFloat() * 212f - 106f;
            if (Math.abs(x) < 4f && z > -55f && z < 12f) continue;   // not on the trail
            float s = 0.12f + rng.nextFloat() * 0.14f;
            // three small crossed blades rather than one cone: reads as a grass clump
            for (int b = 0; b < 3; b++) {
                float ox = (rng.nextFloat() - 0.5f) * 0.5f;
                float oz = (rng.nextFloat() - 0.5f) * 0.5f;
                tufts.setVertexTransform(tr.setToTranslation(x + ox, s * 1.6f, z + oz)
                        .rotate(0f, 1f, 0f, rng.nextFloat() * 180f));
                BoxShapeBuilder.build(tufts, 0f, 0f, 0f, s * 1.1f, s * 2.4f, s * 0.16f);
            }
        }
        tufts.setVertexTransform(null);

        Model model = mb.end();
        world.models.add(model);
        world.instances.add(new ModelInstance(model));
        return world;
    }

    /** Inside-out gradient sphere that follows the camera; render WITHOUT environment. */
    public static Model buildSkyDome(Texture sky) {
        ModelBuilder mb = new ModelBuilder();
        Material m = new Material(
                TextureAttribute.createDiffuse(sky),
                ColorAttribute.createDiffuse(Color.WHITE),
                IntAttribute.createCullFace(GL20.GL_NONE));
        return mb.createSphere(700f, 700f, 700f, 24, 12, m,
                Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    }

    /** A few puffy clouds; render WITHOUT environment so they stay bright. */
    public static Model buildClouds() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder c = mb.part("clouds", GL20.GL_TRIANGLES, ATTRS,
                new Material(ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f)),
                        new BlendingAttribute(0.92f)));
        Matrix4 tr = new Matrix4();
        RandomXS128 rng = new RandomXS128(99L);
        for (int i = 0; i < 7; i++) {
            float x = rng.nextFloat() * 400f - 200f;
            float z = rng.nextFloat() * 400f - 200f;
            float y = 46f + rng.nextFloat() * 18f;
            float s = 7f + rng.nextFloat() * 8f;
            tr.setToTranslation(x, y, z);
            c.setVertexTransform(tr);
            SphereShapeBuilder.build(c, s * 2.2f, s * 0.75f, s * 1.5f, 10, 7);
            tr.setToTranslation(x + s * 0.9f, y + s * 0.14f, z + s * 0.3f);
            c.setVertexTransform(tr);
            SphereShapeBuilder.build(c, s * 1.4f, s * 0.6f, s * 1.1f, 9, 6);
            tr.setToTranslation(x - s * 0.8f, y + s * 0.1f, z - s * 0.25f);
            c.setVertexTransform(tr);
            SphereShapeBuilder.build(c, s * 1.2f, s * 0.55f, s, 9, 6);
        }
        c.setVertexTransform(null);
        return mb.end();
    }

    /** Small translucent disc used as a fake blob shadow. */
    public static Model buildShadowDisc() {
        ModelBuilder mb = new ModelBuilder();
        Material m = new Material(
                ColorAttribute.createDiffuse(new Color(0f, 0f, 0f, 1f)),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.28f));
        return mb.createCylinder(1.5f, 0.02f, 1.5f, 14, m, ATTRS);
    }

    private static Material mat(Color c) {
        return new Material(ColorAttribute.createDiffuse(c));
    }

    static Material mat(Texture t, Color tint, float tileU, float tileV) {
        TextureAttribute ta = TextureAttribute.createDiffuse(t);
        ta.scaleU = tileU;
        ta.scaleV = tileV;
        return new Material(ta, ColorAttribute.createDiffuse(tint));
    }
}
