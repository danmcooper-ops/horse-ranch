package com.ranchgame.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Static ranch scenery: render instances plus XZ collision rectangles. */
public class RanchWorld implements Disposable {

    public final Array<ModelInstance> instances = new Array<>();
    /** Axis-aligned obstacles on the XZ plane (Rectangle.x/y = min x/z). */
    public final Array<Rectangle> obstacles = new Array<>();
    final Array<Model> models = new Array<>();

    public void addObstacle(float centerX, float centerZ, float width, float depth) {
        obstacles.add(new Rectangle(centerX - width / 2f, centerZ - depth / 2f, width, depth));
    }

    /** Push a circle at (x,z) with the given radius out of every obstacle. Returns corrected x/z in out[0..1]. */
    public void resolveCollision(float[] pos, float radius) {
        for (Rectangle r : obstacles) {
            float cx = clamp(pos[0], r.x, r.x + r.width);
            float cz = clamp(pos[1], r.y, r.y + r.height);
            float dx = pos[0] - cx;
            float dz = pos[1] - cz;
            float d2 = dx * dx + dz * dz;
            if (d2 > radius * radius) continue;
            if (d2 > 0.000001f) {
                float d = (float) Math.sqrt(d2);
                pos[0] = cx + dx / d * radius;
                pos[1] = cz + dz / d * radius;
            } else {
                // center is inside the rectangle: push out along the shallowest axis
                float left = pos[0] - r.x, right = r.x + r.width - pos[0];
                float bottom = pos[1] - r.y, top = r.y + r.height - pos[1];
                float min = Math.min(Math.min(left, right), Math.min(bottom, top));
                if (min == left) pos[0] = r.x - radius;
                else if (min == right) pos[0] = r.x + r.width + radius;
                else if (min == bottom) pos[1] = r.y - radius;
                else pos[1] = r.y + r.height + radius;
            }
        }
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : Math.min(v, max);
    }

    @Override
    public void dispose() {
        for (Model m : models) m.dispose();
    }
}
