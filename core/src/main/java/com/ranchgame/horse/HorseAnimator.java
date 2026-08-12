package com.ranchgame.horse;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Drives the named nodes of a horse ModelInstance from Horse state.
 * Upper legs swing from the hip; the lower legs live in their own nodes
 * pivoted at the knee, whose position and rotation are recomputed each
 * frame so the knee folds during the forward swing — timing and fold
 * amounts follow Muybridge's motion plates.
 */
public class HorseAnimator {

    private static final float KNEE_DROP = 0.42f;   // knee sits this far below the hip

    private final ModelInstance instance;
    private final Node body, neck, tail, rider;
    private final Node[] hips = new Node[4];
    private final Node[] knees = new Node[4];
    private final float[] hipX = new float[4], hipY = new float[4], hipZ = new float[4];
    private float time;

    public HorseAnimator(ModelInstance instance) {
        this.instance = instance;
        body = instance.getNode("body", true);
        neck = instance.getNode("neck", true);
        tail = instance.getNode("tail", true);
        rider = instance.getNode("rider", true);
        String[] ids = {"FL", "FR", "BL", "BR"};
        for (int i = 0; i < 4; i++) {
            hips[i] = instance.getNode("leg" + ids[i], true);
            knees[i] = instance.getNode("low" + ids[i], true);
            hipX[i] = hips[i].translation.x;
            hipY[i] = hips[i].translation.y;
            hipZ[i] = hips[i].translation.z;
        }
    }

    public ModelInstance instance() {
        return instance;
    }

    /** Hide or show the mounted rider (used while the player walks on foot). */
    public void setRiderVisible(boolean visible) {
        if (rider != null) {
            float s = visible ? 1f : 0f;
            rider.scale.set(s, s, s);
        }
    }

    /** Position a lower leg under its hip and apply hip swing + knee fold. */
    private void poseLeg(int i, float hipDeg, float kneeFoldDeg) {
        hips[i].rotation.setFromAxis(Vector3.X, hipDeg);
        // knee position = hip pivot + the knee offset rotated by the hip swing
        float rad = hipDeg * MathUtils.degreesToRadians;
        knees[i].translation.set(hipX[i],
                hipY[i] - KNEE_DROP * MathUtils.cos(rad),
                hipZ[i] - KNEE_DROP * MathUtils.sin(rad));
        knees[i].rotation.setFromAxis(Vector3.X, hipDeg + kneeFoldDeg);
    }

    public void update(Horse horse, float delta) {
        time += delta;
        Gait g = horse.gait;
        float amp = g.legAmplitude * horse.strideScale;

        if (horse.grounded) {
            for (int i = 0; i < 4; i++) {
                boolean front = i < 2;
                float legAmp = amp * (front ? g.frontAmp : 1f);
                float ph = horse.phase + g.legPhase[i];
                float hip = MathUtils.sin(ph) * legAmp;
                // the knee folds while the leg is protracting (swinging forward);
                // cos > 0 marks that half of the cycle, per the Muybridge plates
                float protract = Math.max(0f, MathUtils.cos(ph));
                float fold = protract * g.kneeFold * horse.strideScale * (front ? 1f : 0.55f);
                poseLeg(i, hip, fold);
            }
            body.rotation.setFromAxis(Vector3.X,
                    MathUtils.sin(horse.phase + 1.0f) * g.rock * horse.strideScale);
            // canter/gallop stretch the neck low and forward (per the plates)
            float extend = g.singleBounce ? -(6f + g.rock * 1.5f) * horse.strideScale : 0f;
            float nod = MathUtils.sin(horse.phase * g.headBobFreq)
                    * (2f + g.rock * 0.8f) * horse.strideScale;
            neck.rotation.setFromAxis(Vector3.X, extend + nod);
        } else {
            // gathered suspension, straight from the Sallie Gardner plate:
            // fronts folded tight under the chest, hinds curled forward
            poseLeg(0, -22f, 88f);
            poseLeg(1, -16f, 82f);
            poseLeg(2, 42f, 34f);
            poseLeg(3, 36f, 30f);
            body.rotation.setFromAxis(Vector3.X, -MathUtils.clamp(horse.verticalVelocity * 3f, -14f, 14f));
            neck.rotation.setFromAxis(Vector3.X, -18f);
        }

        tail.rotation.setFromAxis(0f, 0f, 1f, MathUtils.sin(time * 1.6f) * 9f);
        if (rider != null) {
            float lean = horse.grounded ? horse.speed * 1.1f : 16f;
            rider.rotation.setFromAxis(Vector3.X, lean);
        }
        instance.calculateTransforms();

        float bounce = 0f;
        if (horse.grounded) {
            // trot bumps twice per stride; canter/gallop rise once, over the suspension
            float bPhase = g.singleBounce ? horse.phase * 0.5f + 1.2f : horse.phase;
            bounce = Math.abs(MathUtils.sin(bPhase)) * g.bounce * horse.strideScale;
        }
        instance.transform.setToRotation(Vector3.Y, horse.yaw)
                .setTranslation(horse.position.x, horse.position.y + bounce, horse.position.z);
    }

    /** Idle grazing pose for pasture horses: head down, slow bob and tail swish. */
    public void graze(float x, float z, float yawDeg, float delta) {
        time += delta;
        neck.rotation.setFromAxis(Vector3.X, 64f + MathUtils.sin(time * 0.7f) * 7f);
        tail.rotation.setFromAxis(0f, 0f, 1f, MathUtils.sin(time * 1.1f) * 10f);
        for (int i = 0; i < 4; i++) {
            poseLeg(i, 0f, 0f);
        }
        instance.calculateTransforms();
        instance.transform.setToRotation(Vector3.Y, yawDeg).setTranslation(x, 0f, z);
    }
}
