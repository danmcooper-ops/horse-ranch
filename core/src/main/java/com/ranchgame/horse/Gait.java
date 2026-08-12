package com.ranchgame.horse;

/**
 * The horse's gaits. Footfall phase offsets follow Muybridge's motion
 * plates: the walk is an even 4-beat lateral sequence (LH, LF, RH, RF),
 * the trot moves diagonal pairs together, the canter is 3-beat with a
 * diagonal pair, and the gallop clusters all four footfalls in half the
 * stride, leaving a gathered suspension for the rest.
 */
public enum Gait {
    HALT(0f, 0f, 0f, 0f, 0f, new float[]{0f, 0f, 0f, 0f}, 0f, 1f, 1, false, "Halt"),
    // lateral 4-beat: BL 0, FL 1/4, BR 1/2, FR 3/4; head nods twice per stride
    WALK(1.7f, 5.5f, 22f, 0.008f, 0f,
            new float[]{1.5708f, 4.7124f, 0f, 3.1416f}, 18f, 1f, 2, false, "Walk"),
    // 2-beat diagonal pairs (FL+BR, FR+BL)
    TROT(4.0f, 9f, 30f, 0.035f, 2f,
            new float[]{0f, 3.1416f, 3.1416f, 0f}, 35f, 1f, 1, false, "Trot"),
    // 3-beat, right lead: BL, then BR+FL together, then FR, then suspension
    CANTER(6.8f, 10.5f, 38f, 0.07f, 5f,
            new float[]{2.0f, 4.0f, 0f, 2.0f}, 48f, 1.15f, 1, true, "Canter"),
    // transverse 4-beat: BL, BR, FL, FR clustered, then gathered suspension
    GALLOP(10.5f, 12.5f, 42f, 0.11f, 8f,
            new float[]{1.8f, 2.4f, 0f, 0.6f}, 65f, 1.3f, 1, true, "Gallop");

    /** Top speed in meters/second. */
    public final float maxSpeed;
    /** Stride cycle frequency in radians/second at full speed. */
    public final float strideFreq;
    /** Peak upper-leg swing in degrees (hind legs; fronts scaled by frontAmp). */
    public final float legAmplitude;
    /** Vertical body bounce in meters. */
    public final float bounce;
    /** Body pitch rocking in degrees (canter/gallop). */
    public final float rock;
    /** Stride phase offsets in radians for legs FL, FR, BL, BR. */
    public final float[] legPhase;
    /** How far the knee folds during the forward swing, in degrees. */
    public final float kneeFold;
    /** Front-leg amplitude multiplier (gallop reaches further with the fronts). */
    public final float frontAmp;
    /** Head nods per stride (the walk nods twice). */
    public final int headBobFreq;
    /** True for gaits with one suspension per stride (canter/gallop bounce). */
    public final boolean singleBounce;
    public final String label;

    Gait(float maxSpeed, float strideFreq, float legAmplitude, float bounce, float rock,
         float[] legPhase, float kneeFold, float frontAmp, int headBobFreq,
         boolean singleBounce, String label) {
        this.maxSpeed = maxSpeed;
        this.strideFreq = strideFreq;
        this.legAmplitude = legAmplitude;
        this.bounce = bounce;
        this.rock = rock;
        this.legPhase = legPhase;
        this.kneeFold = kneeFold;
        this.frontAmp = frontAmp;
        this.headBobFreq = headBobFreq;
        this.singleBounce = singleBounce;
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
