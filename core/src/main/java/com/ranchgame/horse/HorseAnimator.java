package com.ranchgame.horse;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Drives the named nodes of a horse ModelInstance from Horse state:
 * leg swing per gait, head bob, tail swish, body rock, airborne tuck,
 * plus the world transform (position/yaw/bounce).
 */
public class HorseAnimator {

    private final ModelInstance instance;
    private final Node body, neck, tail, legFL, legFR, legBL, legBR, rider;
    private float time;

    public HorseAnimator(ModelInstance instance) {
        this.instance = instance;
        body = instance.getNode("body", true);
        neck = instance.getNode("neck", true);
        tail = instance.getNode("tail", true);
        legFL = instance.getNode("legFL", true);
        legFR = instance.getNode("legFR", true);
        legBL = instance.getNode("legBL", true);
        legBR = instance.getNode("legBR", true);
        rider = instance.getNode("rider", true);
    }

    public ModelInstance instance() {
        return instance;
    }

    public void update(Horse horse, float delta) {
        time += delta;
        Gait g = horse.gait;
        float amp = g.legAmplitude * horse.strideScale;

        if (horse.grounded) {
            legFL.rotation.setFromAxis(Vector3.X, MathUtils.sin(horse.phase + g.legPhase[0]) * amp);
            legFR.rotation.setFromAxis(Vector3.X, MathUtils.sin(horse.phase + g.legPhase[1]) * amp);
            legBL.rotation.setFromAxis(Vector3.X, MathUtils.sin(horse.phase + g.legPhase[2]) * amp);
            legBR.rotation.setFromAxis(Vector3.X, MathUtils.sin(horse.phase + g.legPhase[3]) * amp);
            body.rotation.setFromAxis(Vector3.X, MathUtils.sin(horse.phase) * g.rock * horse.strideScale);
            neck.rotation.setFromAxis(Vector3.X,
                    MathUtils.sin(horse.phase) * (2f + g.rock * 0.8f) * horse.strideScale);
        } else {
            // airborne: front legs tucked, hind legs extended, neck stretched forward
            legFL.rotation.setFromAxis(Vector3.X, 48f);
            legFR.rotation.setFromAxis(Vector3.X, 42f);
            legBL.rotation.setFromAxis(Vector3.X, -38f);
            legBR.rotation.setFromAxis(Vector3.X, -32f);
            body.rotation.setFromAxis(Vector3.X, -MathUtils.clamp(horse.verticalVelocity * 3f, -14f, 14f));
            neck.rotation.setFromAxis(Vector3.X, -18f);
        }

        tail.rotation.setFromAxis(0f, 0f, 1f, MathUtils.sin(time * 1.6f) * 9f);
        if (rider != null) {
            float lean = horse.grounded ? horse.speed * 1.1f : 16f;
            rider.rotation.setFromAxis(Vector3.X, lean);
        }
        instance.calculateTransforms();

        float bounce = horse.grounded
                ? Math.abs(MathUtils.sin(horse.phase)) * g.bounce * horse.strideScale : 0f;
        instance.transform.setToRotation(Vector3.Y, horse.yaw)
                .setTranslation(horse.position.x, horse.position.y + bounce, horse.position.z);
    }

    /** Idle grazing pose for pasture horses: head down, slow bob and tail swish. */
    public void graze(float x, float z, float yawDeg, float delta) {
        time += delta;
        neck.rotation.setFromAxis(Vector3.X, 52f + MathUtils.sin(time * 0.7f) * 7f);
        tail.rotation.setFromAxis(0f, 0f, 1f, MathUtils.sin(time * 1.1f) * 10f);
        instance.calculateTransforms();
        instance.transform.setToRotation(Vector3.Y, yawDeg).setTranslation(x, 0f, z);
    }
}
