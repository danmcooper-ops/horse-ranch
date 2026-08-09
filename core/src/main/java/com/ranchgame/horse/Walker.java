package com.ranchgame.horse;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * The rider on foot: halt / walk / jog movement with swinging limbs.
 * Same control scheme as the horse (turn axis + pace up/down).
 */
public class Walker {

    public static final float RADIUS = 0.35f;
    private static final float[] PACE_SPEED = {0f, 1.6f, 3.4f};
    private static final float[] PACE_FREQ = {0f, 7f, 10.5f};
    private static final String[] PACE_LABEL = {"Standing", "Walking", "Jogging"};

    public final Vector3 position = new Vector3();
    public float yaw;
    public float speed;
    public int pace;
    private float phase;

    private final ModelInstance instance;
    private final Node torso, armL, armR, legL, legR;

    public Walker(ModelInstance instance) {
        this.instance = instance;
        torso = instance.getNode("torso", true);
        armL = instance.getNode("armL", true);
        armR = instance.getNode("armR", true);
        legL = instance.getNode("legL", true);
        legR = instance.getNode("legR", true);
    }

    public ModelInstance instance() {
        return instance;
    }

    public String paceLabel() {
        return PACE_LABEL[pace];
    }

    public void update(float delta, float turnAxis, boolean paceUp, boolean paceDown) {
        if (paceUp && pace < PACE_SPEED.length - 1) pace++;
        if (paceDown && pace > 0) pace--;

        float target = PACE_SPEED[pace];
        if (speed < target) speed = Math.min(target, speed + 5f * delta);
        else speed = Math.max(target, speed - 7f * delta);

        yaw += turnAxis * 170f * delta;
        position.x += MathUtils.sinDeg(yaw) * speed * delta;
        position.z += MathUtils.cosDeg(yaw) * speed * delta;

        float freq = PACE_FREQ[Math.max(1, pace)];
        phase += freq * (0.4f + 0.6f * speed / 3.4f) * delta;
        float stride = MathUtils.clamp(speed / 1.2f, 0f, 1f);
        float swing = MathUtils.sin(phase) * (18f + 14f * (speed / 3.4f)) * stride;

        legL.rotation.setFromAxis(Vector3.X, swing);
        legR.rotation.setFromAxis(Vector3.X, -swing);
        armL.rotation.setFromAxis(Vector3.X, -swing * 0.8f);
        armR.rotation.setFromAxis(Vector3.X, swing * 0.8f);
        torso.rotation.setFromAxis(Vector3.X, speed * 1.2f);
        instance.calculateTransforms();

        float bob = Math.abs(MathUtils.sin(phase)) * 0.035f * stride;
        instance.transform.setToRotation(Vector3.Y, yaw)
                .setTranslation(position.x, position.y + bob, position.z);
    }

    public Vector3 forward(Vector3 out) {
        return out.set(MathUtils.sinDeg(yaw), 0f, MathUtils.cosDeg(yaw));
    }
}
