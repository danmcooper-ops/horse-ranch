package com.ranchgame.horse;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/** Small helpers for building smooth, organic shapes out of ellipsoids. */
final class Organic {

    private static final Matrix4 M = new Matrix4();

    private Organic() {
    }

    /** Axis-aligned ellipsoid centered at (cx,cy,cz) with radii (rx,ry,rz). */
    static void ball(MeshPartBuilder p, float cx, float cy, float cz,
                     float rx, float ry, float rz) {
        p.setVertexTransform(M.setToTranslation(cx, cy, cz));
        SphereShapeBuilder.build(p, rx * 2f, ry * 2f, rz * 2f, 28, 18);
        p.setVertexTransform(null);
    }

    /** Ellipsoid pitched around the X axis (positive leans its top toward +Z). */
    static void ballX(MeshPartBuilder p, float cx, float cy, float cz,
                      float rx, float ry, float rz, float pitchDeg) {
        p.setVertexTransform(M.setToTranslation(cx, cy, cz).rotate(Vector3.X, pitchDeg));
        SphereShapeBuilder.build(p, rx * 2f, ry * 2f, rz * 2f, 28, 18);
        p.setVertexTransform(null);
    }

    /** Ellipsoid rolled around the Z axis (for splayed ears, tilted limbs). */
    static void ballZ(MeshPartBuilder p, float cx, float cy, float cz,
                      float rx, float ry, float rz, float rollDeg) {
        p.setVertexTransform(M.setToTranslation(cx, cy, cz).rotate(Vector3.Z, rollDeg));
        SphereShapeBuilder.build(p, rx * 2f, ry * 2f, rz * 2f, 28, 18);
        p.setVertexTransform(null);
    }

    /** Low-poly ellipsoid for small detail parts (straps, buttons, eyes, petals). */
    static void ballLow(MeshPartBuilder p, float cx, float cy, float cz,
                        float rx, float ry, float rz) {
        p.setVertexTransform(M.setToTranslation(cx, cy, cz));
        SphereShapeBuilder.build(p, rx * 2f, ry * 2f, rz * 2f, 12, 8);
        p.setVertexTransform(null);
    }

    /** Low-poly ellipsoid pitched around the X axis. */
    static void ballLowX(MeshPartBuilder p, float cx, float cy, float cz,
                         float rx, float ry, float rz, float pitchDeg) {
        p.setVertexTransform(M.setToTranslation(cx, cy, cz).rotate(Vector3.X, pitchDeg));
        SphereShapeBuilder.build(p, rx * 2f, ry * 2f, rz * 2f, 12, 8);
        p.setVertexTransform(null);
    }

    /** Squat cylinder (hooves, hat brims). */
    static void puck(MeshPartBuilder p, float cx, float cy, float cz,
                     float radius, float height) {
        p.setVertexTransform(M.setToTranslation(cx, cy, cz));
        CylinderShapeBuilder.build(p, radius * 2f, height, radius * 2f, 20);
        p.setVertexTransform(null);
    }
}
