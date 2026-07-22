package com.ranchgame.horse;

/** The horse's movement gaits, each with speed, stride and animation parameters. */
public enum Gait {
    HALT(0f, 0f, 0f, 0f, 0f, new float[]{0f, 0f, 0f, 0f}, "Halt"),
    WALK(1.7f, 5.5f, 24f, 0.012f, 0f, new float[]{1.5708f, 4.7124f, 0f, 3.1416f}, "Walk"),
    TROT(4.0f, 9f, 32f, 0.035f, 2f, new float[]{0f, 3.1416f, 3.1416f, 0f}, "Trot"),
    CANTER(6.8f, 10.5f, 42f, 0.07f, 5f, new float[]{0.6f, 0f, 3.7416f, 3.1416f}, "Canter"),
    GALLOP(10.5f, 12.5f, 50f, 0.1f, 8f, new float[]{0f, 0.4f, 3.1416f, 3.5416f}, "Gallop");

    /** Top speed in meters/second. */
    public final float maxSpeed;
    /** Stride cycle frequency in radians/second at full speed. */
    public final float strideFreq;
    /** Peak leg swing in degrees. */
    public final float legAmplitude;
    /** Vertical body bounce in meters. */
    public final float bounce;
    /** Body pitch rocking in degrees (canter/gallop). */
    public final float rock;
    /** Stride phase offsets in radians for legs FL, FR, BL, BR. */
    public final float[] legPhase;
    public final String label;

    Gait(float maxSpeed, float strideFreq, float legAmplitude, float bounce, float rock,
         float[] legPhase, String label) {
        this.maxSpeed = maxSpeed;
        this.strideFreq = strideFreq;
        this.legAmplitude = legAmplitude;
        this.bounce = bounce;
        this.rock = rock;
        this.legPhase = legPhase;
        this.label = label;
    }

    public Gait faster() {
        int i = ordinal();
        return i < values().length - 1 ? values()[i + 1] : this;
    }

    public Gait slower() {
        int i = ordinal();
        return i > 0 ? values()[i - 1] : this;
    }
}
