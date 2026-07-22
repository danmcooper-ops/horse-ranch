package com.ranchgame.horse;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Player horse state: position, gait, stride phase and jump physics. */
public class Horse {

    public static final float GRAVITY = 13f;
    public static final float RADIUS = 0.55f;

    public final Vector3 position = new Vector3();
    /** Heading in degrees; facing = (sinDeg(yaw), 0, cosDeg(yaw)). */
    public float yaw;
    public float speed;
    public float verticalVelocity;
    public boolean grounded = true;
    public Gait gait = Gait.HALT;
    /** Stride cycle phase in radians. */
    public float phase;
    /** 0..1 scale applied to leg swing so legs ease to rest as speed drops. */
    public float strideScale;

    public void update(float delta, float turnAxis, boolean gaitUp, boolean gaitDown, boolean jump) {
        if (gaitUp) gait = gait.faster();
        if (gaitDown) gait = gait.slower();

        float target = gait.maxSpeed;
        if (speed < target) {
            speed = Math.min(target, speed + (3.5f + speed * 0.25f) * delta);
        } else {
            speed = Math.max(target, speed - 6f * delta);
        }

        float turnRate = gait == Gait.HALT ? 60f : Math.max(70f, 160f - 8f * speed);
        yaw += turnAxis * turnRate * delta;

        position.x += MathUtils.sinDeg(yaw) * speed * delta;
        position.z += MathUtils.cosDeg(yaw) * speed * delta;

        if (jump && grounded && speed > 0.5f) {
            verticalVelocity = 3.8f + 0.28f * speed;
            grounded = false;
        }
        if (!grounded) {
            verticalVelocity -= GRAVITY * delta;
            position.y += verticalVelocity * delta;
            if (position.y <= 0f) {
                position.y = 0f;
                verticalVelocity = 0f;
                grounded = true;
            }
        }

        float max = Math.max(0.1f, gait.maxSpeed);
        float omega = gait.strideFreq * (0.35f + 0.65f * speed / max);
        if (gait == Gait.HALT) omega = speed > 0.1f ? 4f : 0f;
        phase += omega * delta;
        if (phase > MathUtils.PI2 * 1000f) phase -= MathUtils.PI2 * 1000f;
        strideScale = MathUtils.clamp(speed / 1.2f, 0f, 1f);
    }

    public Vector3 forward(Vector3 out) {
        return out.set(MathUtils.sinDeg(yaw), 0f, MathUtils.cosDeg(yaw));
    }
}
