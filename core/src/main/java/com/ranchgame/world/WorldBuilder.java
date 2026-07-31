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
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
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

        // Dirt trail from spawn toward the barn and the course area
        MeshPartBuilder dirt = mb.part("trail", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.dirt, Color.WHITE, 21f, 1.2f));
        BoxShapeBuilder.build(dirt, 0f, 0.008f, -20f, 3.4f, 0.012f, 60f);
        MeshPartBuilder dirt2 = mb.part("trail2", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.dirt, Color.WHITE, 1.2f, 11f));
        BoxShapeBuilder.build(dirt2, -16f, 0.008f, -20f, 32f, 0.012f, 3.4f);

        // --- Perimeter fence ----------------------------------------------
        MeshPartBuilder posts = mb.part("fencePosts", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, Color.WHITE, 1f, 1f));
        MeshPartBuilder rails = mb.part("fenceRails", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, new Color(1f, 0.96f, 0.9f, 1f), 26f, 1f));
        for (int side = 0; side < 4; side++) {
            boolean horizontal = side < 2;              // along X at z = +/-HALF
            float fixed = side % 2 == 0 ? HALF : -HALF;
            for (float p = -HALF; p <= HALF; p += 5.5f) {
                if (horizontal) BoxShapeBuilder.build(posts, p, 0.65f, fixed, 0.22f, 1.3f, 0.22f);
                else BoxShapeBuilder.build(posts, fixed, 0.65f, p, 0.22f, 1.3f, 0.22f);
            }
            for (int rail = 0; rail < 2; rail++) {
                float y = 0.55f + rail * 0.5f;
                if (horizontal) BoxShapeBuilder.build(rails, 0f, y, fixed, 2f * HALF, 0.12f, 0.1f);
                else BoxShapeBuilder.build(rails, fixed, y, 0f, 0.1f, 0.12f, 2f * HALF);
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
        Matrix4 tr = new Matrix4();
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

        // --- Trees ---------------------------------------------------------
        MeshPartBuilder trunks = mb.part("trunks", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.wood, new Color(0.85f, 0.75f, 0.68f, 1f), 1f, 2f));
        MeshPartBuilder leaves = mb.part("leaves", GL20.GL_TRIANGLES, ATTRS_TEX,
                mat(tex.leaves, Color.WHITE, 2f, 1.5f));
        int planted = 0;
        while (planted < 26) {
            float x = rng.nextFloat() * 210f - 105f;
            float z = rng.nextFloat() * 210f - 105f;
            // keep the course paddock (east), spawn trail and barn area clear
            if (x > 2f && x < 62f && z > -32f && z < 40f) continue;
            if (Math.abs(x) < 4f && z > -55f && z < 12f) continue;
            if (x > -42f && x < -14f && z > -32f && z < -4f) continue;
            float h = 2f + rng.nextFloat() * 1.5f;
            trunks.setVertexTransform(tr.setToTranslation(x, h / 2f, z));
            CylinderShapeBuilder.build(trunks, 0.5f, h, 0.5f, 10);
            leaves.setVertexTransform(tr.setToTranslation(x, h + 1.2f, z));
            ConeShapeBuilder.build(leaves, 2.8f, 2.6f, 2.8f, 12);
            leaves.setVertexTransform(tr.setToTranslation(x, h + 2.9f, z));
            ConeShapeBuilder.build(leaves, 2f, 2f, 2f, 12);
            world.addObstacle(x, z, 1.1f, 1.1f);
            planted++;
        }
        trunks.setVertexTransform(null);
        leaves.setVertexTransform(null);

        Model model = mb.end();
        world.models.add(model);
        world.instances.add(new ModelInstance(model));
        return world;
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
