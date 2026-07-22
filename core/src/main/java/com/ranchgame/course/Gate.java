package com.ranchgame.course;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * One jump gate (two posts + a colored rail) or, with railHeight == 0,
 * a start/finish line marked by two flag posts. The crossing line runs
 * between the posts; heading is the intended direction of travel.
 */
public class Gate {

    public static final float WIDTH = 4.5f;
    public static final float RAIL_HEIGHT = 0.85f;
    private static final long ATTRS = Usage.Position | Usage.Normal;

    public final float x, z, headingDeg, railHeight;
    public final Vector2 endA = new Vector2();
    public final Vector2 endB = new Vector2();
    public final Vector2 dir = new Vector2();
    public final ModelInstance instance;
    public final Model model;
    private final Material railMaterial;

    public Gate(float x, float z, float headingDeg, boolean startLine) {
        this.x = x;
        this.z = z;
        this.headingDeg = headingDeg;
        this.railHeight = startLine ? 0f : RAIL_HEIGHT;
        dir.set(MathUtils.sinDeg(headingDeg), MathUtils.cosDeg(headingDeg));
        // perpendicular in XZ
        float px = MathUtils.cosDeg(headingDeg);
        float pz = -MathUtils.sinDeg(headingDeg);
        endA.set(x + px * WIDTH / 2f, z + pz * WIDTH / 2f);
        endB.set(x - px * WIDTH / 2f, z - pz * WIDTH / 2f);

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder posts = mb.part("posts", GL20.GL_TRIANGLES, ATTRS,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        float hw = WIDTH / 2f;
        float postH = startLine ? 2.2f : 1.15f;
        BoxShapeBuilder.build(posts, -hw, postH / 2f, 0f, 0.24f, postH, 0.24f);
        BoxShapeBuilder.build(posts, hw, postH / 2f, 0f, 0.24f, postH, 0.24f);
        Color accent = startLine ? new Color(0.15f, 0.75f, 0.3f, 1f) : new Color(0.85f, 0.25f, 0.2f, 1f);
        MeshPartBuilder rail = mb.part("rail", GL20.GL_TRIANGLES, ATTRS,
                new Material(ColorAttribute.createDiffuse(accent)));
        if (startLine) {
            BoxShapeBuilder.build(rail, -hw, 2.0f, 0.2f, 0.08f, 0.5f, 0.8f);   // flags
            BoxShapeBuilder.build(rail, hw, 2.0f, 0.2f, 0.08f, 0.5f, 0.8f);
        } else {
            BoxShapeBuilder.build(rail, 0f, RAIL_HEIGHT, 0f, WIDTH, 0.16f, 0.14f);
            BoxShapeBuilder.build(rail, 0f, RAIL_HEIGHT * 0.55f, 0f, WIDTH, 0.12f, 0.1f);
        }
        model = mb.end();
        instance = new ModelInstance(model);
        instance.transform.setToRotation(0f, 1f, 0f, headingDeg).setTranslation(x, 0f, z);
        railMaterial = instance.materials.get(1);
    }

    public void setRailColor(Color c) {
        railMaterial.set(ColorAttribute.createDiffuse(c));
    }

    /** True if the segment p0->p1 crosses the gate line between the posts. */
    public boolean crossed(Vector2 p0, Vector2 p1, boolean requireForward) {
        if (requireForward && (p1.x - p0.x) * dir.x + (p1.y - p0.y) * dir.y <= 0f) return false;
        return segmentsIntersect(p0.x, p0.y, p1.x, p1.y, endA.x, endA.y, endB.x, endB.y);
    }

    private static boolean segmentsIntersect(float ax, float ay, float bx, float by,
                                             float cx, float cy, float dx, float dy) {
        float d1 = cross(dx - cx, dy - cy, ax - cx, ay - cy);
        float d2 = cross(dx - cx, dy - cy, bx - cx, by - cy);
        float d3 = cross(bx - ax, by - ay, cx - ax, cy - ay);
        float d4 = cross(bx - ax, by - ay, dx - ax, dy - ay);
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0))
                && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    private static float cross(float x1, float y1, float x2, float y2) {
        return x1 * y2 - y1 * x2;
    }
}
