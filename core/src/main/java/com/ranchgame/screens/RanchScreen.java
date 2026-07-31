package com.ranchgame.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.ranchgame.course.CourseManager;
import com.ranchgame.course.Gate;
import com.ranchgame.horse.Horse;
import com.ranchgame.horse.HorseAnimator;
import com.ranchgame.horse.HorseModelFactory;
import com.ranchgame.hud.Hud;
import com.ranchgame.world.ProceduralTextures;
import com.ranchgame.world.RanchWorld;
import com.ranchgame.world.WorldBuilder;

/** The whole game: free-roam ranch riding plus the show-jumping course. */
public class RanchScreen extends ScreenAdapter {

    private static final Color SKY = new Color(0.55f, 0.78f, 0.95f, 1f);

    private final ModelBatch batch = new ModelBatch();
    private final Environment environment = new Environment();
    private final PerspectiveCamera camera;
    private final ProceduralTextures textures;
    private final RanchWorld world;
    private final CourseManager course = new CourseManager();
    private final Hud hud = new Hud();

    private final Horse horse = new Horse();
    private final Model horseModel;
    private final ModelInstance horseInstance;
    private final HorseAnimator horseAnimator;

    private final Model pastureModel1, pastureModel2;
    private final HorseAnimator pasture1, pasture2;

    private final Model shadowModel;
    private final ModelInstance horseShadow, pastureShadow1, pastureShadow2;

    private final float[] collisionPos = new float[2];
    private final Vector3 tmp = new Vector3();
    private final Vector3 camTarget = new Vector3();
    private CourseManager.State lastState = CourseManager.State.READY;

    public RanchScreen() {
        // warm key light + cool fill, Sims-style soft daylight
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.58f, 0.54f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, SKY.r, SKY.g, SKY.b, 1f));
        environment.add(new DirectionalLight().set(0.85f, 0.8f, 0.7f, -0.3f, -0.85f, -0.35f));
        environment.add(new DirectionalLight().set(0.18f, 0.2f, 0.26f, 0.5f, -0.25f, 0.45f));

        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.3f;
        camera.far = 420f;

        textures = new ProceduralTextures();
        world = WorldBuilder.build(textures);

        horseModel = HorseModelFactory.create(
                new Color(0.55f, 0.36f, 0.2f, 1f), new Color(0.25f, 0.16f, 0.1f, 1f), true,
                textures.coat);
        horseInstance = new ModelInstance(horseModel);
        horseAnimator = new HorseAnimator(horseInstance);
        horse.position.set(0f, 0f, -42f);
        horse.yaw = 0f;

        pastureModel1 = HorseModelFactory.create(
                new Color(0.35f, 0.3f, 0.28f, 1f), new Color(0.12f, 0.1f, 0.1f, 1f), false,
                textures.coat);
        pastureModel2 = HorseModelFactory.create(
                new Color(0.85f, 0.78f, 0.68f, 1f), new Color(0.9f, 0.88f, 0.82f, 1f), false,
                textures.coat);
        pasture1 = new HorseAnimator(new ModelInstance(pastureModel1));
        pasture2 = new HorseAnimator(new ModelInstance(pastureModel2));

        shadowModel = WorldBuilder.buildShadowDisc();
        horseShadow = new ModelInstance(shadowModel);
        pastureShadow1 = new ModelInstance(shadowModel);
        pastureShadow2 = new ModelInstance(shadowModel);
        pastureShadow1.transform.setToTranslation(-18f, 0.015f, -33f);
        pastureShadow2.transform.setToTranslation(-13f, 0.015f, -26f);
        world.addObstacle(-18f, -33f, 1.4f, 2.2f);
        world.addObstacle(-13f, -26f, 1.4f, 2.2f);

        Gdx.input.setInputProcessor(hud.stage);
        updateTouchMode(Gdx.graphics.getWidth());

        // start the camera behind the horse
        horse.forward(tmp);
        camera.position.set(horse.position).mulAdd(tmp, -8f).add(0f, 3.6f, 0f);
        camera.lookAt(horse.position.x, 1.5f, horse.position.z);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void updateTouchMode(int width) {
        boolean touch = Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen);
        boolean smallWeb = Gdx.app.getType() == Application.ApplicationType.WebGL
                && width / Math.max(1f, Gdx.graphics.getDensity()) < 800f;
        hud.setTouchVisible(touch || smallWeb);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 20f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            // debug: teleport just south of the course start line, facing it
            horse.position.set(14f, 0f, -26f);
            horse.yaw = 0f;
        }
        float prevX = horse.position.x;
        float prevZ = horse.position.z;

        // --- input ---
        float turn = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) turn += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) turn -= 1f;
        turn += hud.touchTurnAxis();
        turn = MathUtils.clamp(turn, -1f, 1f);
        int shift = hud.touchGaitShift();
        boolean gaitUp = shift > 0
                || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W);
        boolean gaitDown = shift < 0
                || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S);
        boolean jump = hud.touchJumpPressed() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        // --- simulation ---
        horse.update(delta, turn, gaitUp, gaitDown, jump);
        collisionPos[0] = horse.position.x;
        collisionPos[1] = horse.position.z;
        world.resolveCollision(collisionPos, Horse.RADIUS);
        horse.position.x = MathUtils.clamp(collisionPos[0], -WorldBuilder.HALF + 0.8f, WorldBuilder.HALF - 0.8f);
        horse.position.z = MathUtils.clamp(collisionPos[1], -WorldBuilder.HALF + 0.8f, WorldBuilder.HALF - 0.8f);

        course.update(delta, prevX, prevZ, horse.position.x, horse.position.z,
                horse.position.y, horse.grounded);
        if (course.event != null) hud.showMessage(course.event);
        syncHudWithCourse();

        horseAnimator.update(horse, delta);
        pasture1.graze(-18f, -33f, 70f, delta);
        pasture2.graze(-13f, -26f, 205f, delta);
        float shrink = Math.max(0.45f, 1f - horse.position.y * 0.22f);
        horseShadow.transform.setToTranslation(horse.position.x, 0.015f, horse.position.z)
                .scale(shrink, 1f, shrink);

        // --- camera ---
        horse.forward(tmp);
        float dist = 7f + horse.speed * 0.35f;
        float height = 3.2f + horse.speed * 0.12f;
        camTarget.set(horse.position).mulAdd(tmp, -dist).add(0f, height, 0f);
        float alpha = 1f - (float) Math.exp(-4.5f * delta);
        camera.position.lerp(camTarget, alpha);
        tmp.set(horse.position).add(0f, 1.5f, 0f).mulAdd(horse.forward(new Vector3()), 2.5f);
        camera.direction.set(tmp).sub(camera.position).nor();
        camera.up.set(Vector3.Y);
        camera.update();

        // --- render ---
        Gdx.gl.glClearColor(SKY.r, SKY.g, SKY.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.begin(camera);
        batch.render(world.instances, environment);
        batch.render(course.startLine.instance, environment);
        for (Gate gate : course.gates) batch.render(gate.instance, environment);
        batch.render(pasture1Instance(), environment);
        batch.render(pasture2Instance(), environment);
        batch.render(horseInstance, environment);
        batch.render(horseShadow, environment);
        batch.render(pastureShadow1, environment);
        batch.render(pastureShadow2, environment);
        batch.end();

        hud.setGait(horse.gait.label, horse.speed);
        hud.update(delta);
        hud.draw();
    }

    private ModelInstance pasture1Instance() {
        return pasture1.instance();
    }

    private ModelInstance pasture2Instance() {
        return pasture2.instance();
    }

    private void syncHudWithCourse() {
        CourseManager.State state = course.state;
        if (state == CourseManager.State.RUNNING) {
            int gateNo = Math.min(course.nextGate + 1, course.gates.size);
            hud.setCourseStatus(course.nextGate >= course.gates.size
                    ? "Race back through the flags!"
                    : "Gate " + gateNo + " / " + course.gates.size);
            hud.setTimer(formatTime(course.time)
                    + (course.faults > 0 ? "  +" + (int) (course.faults * CourseManager.FAULT_PENALTY) + "s" : ""));
        } else if (state == CourseManager.State.READY) {
            hud.setCourseStatus("Jumping course: ride through the green flags to start");
            hud.setTimer(course.bestTotal > 0f ? "Best " + formatTime(course.bestTotal) : "");
        } else {
            hud.setCourseStatus("Course complete!");
        }

        if (state != lastState) {
            if (state == CourseManager.State.FINISHED) {
                hud.showResults("Course complete!\nTime " + formatTime(course.time)
                        + "   Faults +" + (int) (course.faults * CourseManager.FAULT_PENALTY) + "s"
                        + "\nTotal " + formatTime(course.total())
                        + "\nBest " + formatTime(course.bestTotal)
                        + "\n\nCross the green flags to ride again");
            } else {
                hud.hideResults();
            }
            lastState = state;
        }
    }

    private static String formatTime(float seconds) {
        int tenths = (int) (seconds * 10f);
        int m = tenths / 600;
        int s = (tenths / 10) % 60;
        int t = tenths % 10;
        return m + ":" + (s < 10 ? "0" : "") + s + "." + t;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        hud.resize(width, height);
        updateTouchMode(width);
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        textures.dispose();
        course.dispose();
        hud.dispose();
        horseModel.dispose();
        pastureModel1.dispose();
        pastureModel2.dispose();
        shadowModel.dispose();
    }
}
