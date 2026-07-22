package com.ranchgame.course;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * The show-jumping course: an ordered loop of gates entered through a
 * start/finish flag line. Tracks timing, faults and the best total.
 */
public class CourseManager implements Disposable {

    public enum State { READY, RUNNING, FINISHED }

    public static final float FAULT_PENALTY = 4f;
    private static final Color NEXT_COLOR = new Color(1f, 0.85f, 0.1f, 1f);
    private static final Color RAIL_COLOR = new Color(0.85f, 0.25f, 0.2f, 1f);
    private static final Color DONE_COLOR = new Color(0.45f, 0.65f, 0.9f, 1f);

    public final Gate startLine;
    public final Array<Gate> gates = new Array<>();
    public State state = State.READY;
    public int nextGate;
    public int faults;
    public float time;
    public float bestTotal;
    /** Set for one frame when something noteworthy happens. */
    public String event;

    private final Preferences prefs;
    private final Vector2 p0 = new Vector2();
    private final Vector2 p1 = new Vector2();

    public CourseManager() {
        startLine = new Gate(14f, -18f, 0f, true);
        addGate(14f, 0f, 0f);
        addGate(16f, 18f, 20f);
        addGate(30f, 30f, 90f);
        addGate(46f, 24f, 140f);
        addGate(52f, 6f, 180f);
        addGate(48f, -12f, 215f);
        addGate(32f, -22f, 270f);
        prefs = Gdx.app.getPreferences("horse-ranch");
        bestTotal = prefs.getFloat("bestTotal", 0f);
        highlight();
    }

    private void addGate(float x, float z, float heading) {
        gates.add(new Gate(x, z, heading, false));
    }

    public void update(float delta, float prevX, float prevZ, float curX, float curZ,
                       float horseY, boolean grounded) {
        event = null;
        p0.set(prevX, prevZ);
        p1.set(curX, curZ);

        if (state == State.RUNNING) {
            time += delta;
            Gate gate = nextGate < gates.size ? gates.get(nextGate) : null;
            if (gate != null && gate.crossed(p0, p1, true)) {
                boolean clean = !grounded && horseY > gate.railHeight - 0.15f;
                if (clean) {
                    event = "Clear!";
                } else {
                    faults++;
                    event = "Rail down! +" + (int) FAULT_PENALTY + "s";
                }
                nextGate++;
                highlight();
                if (nextGate >= gates.size) event = event + "  -  now race to the flags!";
            } else if (nextGate < gates.size && crossedAnyLaterGate()) {
                faults++;
                event = "Wrong gate! +" + (int) FAULT_PENALTY + "s";
            }
            if (nextGate >= gates.size && startLine.crossed(p0, p1, false)) {
                state = State.FINISHED;
                float total = total();
                if (bestTotal <= 0f || total < bestTotal) {
                    bestTotal = total;
                    prefs.putFloat("bestTotal", bestTotal);
                    prefs.flush();
                    event = "NEW BEST!";
                }
            }
        } else if (startLine.crossed(p0, p1, true)) {
            state = State.RUNNING;
            time = 0f;
            faults = 0;
            nextGate = 0;
            event = "GO!";
            highlight();
        }
    }

    private boolean crossedAnyLaterGate() {
        for (int i = nextGate + 1; i < gates.size; i++) {
            if (gates.get(i).crossed(p0, p1, true)) return true;
        }
        return false;
    }

    public float total() {
        return time + faults * FAULT_PENALTY;
    }

    private void highlight() {
        for (int i = 0; i < gates.size; i++) {
            if (i == nextGate && state != State.FINISHED) gates.get(i).setRailColor(NEXT_COLOR);
            else if (i < nextGate) gates.get(i).setRailColor(DONE_COLOR);
            else gates.get(i).setRailColor(RAIL_COLOR);
        }
    }

    @Override
    public void dispose() {
        startLine.model.dispose();
        for (Gate g : gates) g.model.dispose();
    }
}
