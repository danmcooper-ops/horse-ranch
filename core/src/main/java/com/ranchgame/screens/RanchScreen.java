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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.ranchgame.course.CourseManager;
import com.ranchgame.course.Gate;
import com.ranchgame.horse.Horse;
import com.ranchgame.horse.HorseAnimator;
import com.ranchgame.horse.HorseAppearance;
import com.ranchgame.horse.HorseModelFactory;
import com.ranchgame.horse.RiderModelFactory;
import com.ranchgame.horse.Walker;
import com.ranchgame.hud.CustomizeConsole;
import com.ranchgame.hud.Hud;
import com.ranchgame.world.ProceduralTextures;
import com.ranchgame.world.RanchWorld;
import com.ranchgame.world.WorldBuilder;

/** The whole game: free-roam ranch riding plus the show-jumping course. */
public class RanchScreen extends ScreenAdapter {

    private static final Color SKY = new Color(0.8f, 0.89f, 0.97f, 1f);

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
    private final HorseAppearance appearance = new HorseAppearance();

    private final Model riderModel;
    private final Walker walker;
    private boolean mounted = true;
    private static final float MOUNT_RANGE = 4f;

    private final Model pastureModel1, pastureModel2;
    private final HorseAnimator pasture1, pasture2;

    private final DirectionalShadowLight shadowLight;
    private final ModelBatch shadowBatch;
    private final Model skyModel, cloudModel;
    private final ModelInstance skyDome, clouds;
    private float cloudDrift;

    private final float[] collisionPos = new float[2];
    private final Vector3 tmp = new Vector3();
    private final Vector3 camTarget = new Vector3();
    private CourseManager.State lastState = CourseManager.State.READY;

    public RanchScreen() {
        // warm shadow-casting key light + cool fill, soft daylight
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.58f, 0.56f, 0.52f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, SKY.r, SKY.g, SKY.b, 1f));
        shadowLight = new DirectionalShadowLight(2048, 2048, 70f, 70f, 1f, 120f);
        shadowLight.set(0.88f, 0.82f, 0.72f, -0.3f, -0.85f, -0.35f);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;
        environment.add(new DirectionalLight().set(0.18f, 0.2f, 0.26f, 0.5f, -0.25f, 0.45f));
        // render back faces into the depth map: kills self-shadowing acne on lit faces
        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.defaultCullFace = GL20.GL_FRONT;
        shadowBatch = new ModelBatch(new DepthShaderProvider(depthConfig));

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

        skyModel = WorldBuilder.buildSkyDome(textures.sky);
        skyDome = new ModelInstance(skyModel);
        cloudModel = WorldBuilder.buildClouds();
        clouds = new ModelInstance(cloudModel);
        world.addObstacle(-18f, -33f, 1.4f, 2.2f);
        world.addObstacle(-13f, -26f, 1.4f, 2.2f);

        riderModel = RiderModelFactory.create();
        walker = new Walker(new ModelInstance(riderModel));

        final com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("horse-ranch");
        appearance.load(prefs);
        if (com.ranchgame.HorseGame.presetLook != null) {
            int[] p = com.ranchgame.HorseGame.presetLook;
            appearance.coat = p[0];
            appearance.mane = p[1];
            appearance.tack = p[2];
            appearance.pad = p[3];
            appearance.shirt = p[4];
            appearance.pants = p[5];
            appearance.hair = p[6];
        }
        appearance.apply(horseInstance);
        appearance.apply(walker.instance());
        hud.createConsole(appearance, new CustomizeConsole.Listener() {
            @Override
            public void appearanceChanged() {
                appearance.apply(horseInstance);
                appearance.apply(walker.instance());
                appearance.save(prefs);
            }
        });

        if (com.ranchgame.HorseGame.startDismounted) {
            mounted = false;
            horse.forward(tmp);
            walker.position.set(horse.position).add(tmp.z * 1.2f, 0f, -tmp.x * 1.2f);
            walker.yaw = horse.yaw;
            horseAnimator.setRiderVisible(false);
        }

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
        boolean touch = com.ranchgame.HorseGame.touchDevice
                || Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen);
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

        // --- input (suspended while the customize console is open) ---
        boolean consoleOpen = hud.isConsoleOpen();
        float turn = 0f;
        boolean gaitUp = false, gaitDown = false, jump = false;
        if (!consoleOpen) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) turn += 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) turn -= 1f;
            turn += hud.touchTurnAxis();
            turn = MathUtils.clamp(turn, -1f, 1f);
            int shift = hud.touchGaitShift();
            gaitUp = shift > 0
                    || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W);
            gaitDown = shift < 0
                    || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S);
            jump = hud.touchJumpPressed() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        }
        boolean mountToggle = !consoleOpen
                && (hud.touchActionPressed() || Gdx.input.isKeyJustPressed(Input.Keys.E));

        // --- mount / dismount ---
        float distToHorse = tmp.set(walker.position).sub(horse.position).len();
        if (mountToggle) {
            if (mounted && horse.grounded) {
                mounted = false;
                horse.forward(tmp);
                // step off to the horse's left side, facing the same way
                walker.position.set(horse.position)
                        .add(tmp.z * 1.2f, 0f, -tmp.x * 1.2f);
                walker.yaw = horse.yaw;
                walker.pace = 0;
                walker.speed = 0f;
                horseAnimator.setRiderVisible(false);
            } else if (!mounted && distToHorse < MOUNT_RANGE) {
                mounted = true;
                horseAnimator.setRiderVisible(true);
            }
        }

        // --- simulation ---
        if (mounted) {
            horse.update(delta, turn, gaitUp, gaitDown, jump);
            resolveEntity(horse.position, Horse.RADIUS);
            course.update(delta, prevX, prevZ, horse.position.x, horse.position.z,
                    horse.position.y, horse.grounded);
        } else {
            walker.update(delta, turn, gaitUp, gaitDown);
            resolveEntity(walker.position, Walker.RADIUS);
            // the horse trails behind on the lead
            walker.forward(tmp);
            float tx = walker.position.x - tmp.x * 1.9f + tmp.z * 0.7f;
            float tz = walker.position.z - tmp.z * 1.9f - tmp.x * 0.7f;
            horse.updateFollow(delta, tx, tz);
            resolveEntity(horse.position, Horse.RADIUS);
            // course crossings can't happen on foot, but a running clock keeps ticking
            course.update(delta, horse.position.x, horse.position.z,
                    horse.position.x, horse.position.z, horse.position.y, horse.grounded);
        }
        if (course.event != null) hud.showMessage(course.event);
        syncHudWithCourse();

        hud.setAction(mounted || distToHorse < MOUNT_RANGE, mounted ? "OFF" : "RIDE");

        horseAnimator.update(horse, delta);
        pasture1.graze(-18f, -33f, 70f, delta);
        pasture2.graze(-13f, -26f, 205f, delta);
        cloudDrift += delta * 0.6f;
        if (cloudDrift > 250f) cloudDrift -= 500f;
        clouds.transform.setToTranslation(cloudDrift, 0f, 0f);

        // --- camera (follows whoever the player is) ---
        if (mounted) {
            horse.forward(tmp);
            float dist = 7f + horse.speed * 0.35f;
            float height = 3.2f + horse.speed * 0.12f;
            camTarget.set(horse.position).mulAdd(tmp, -dist).add(0f, height, 0f);
            tmp.set(horse.position).add(0f, 1.5f, 0f).mulAdd(horse.forward(new Vector3()), 2.5f);
        } else {
            walker.forward(tmp);
            float dist = 4.6f + walker.speed * 0.3f;
            camTarget.set(walker.position).mulAdd(tmp, -dist).add(0f, 2.3f, 0f);
            tmp.set(walker.position).add(0f, 1.3f, 0f).mulAdd(walker.forward(new Vector3()), 2f);
        }
        float alpha = 1f - (float) Math.exp(-4.5f * delta);
        camera.position.lerp(camTarget, alpha);
        camera.direction.set(tmp).sub(camera.position).nor();
        camera.up.set(Vector3.Y);
        camera.update();

        // --- shadow pass ---
        shadowLight.begin(tmp.set(horse.position).add(0f, 0f, 0f), shadowLight.direction);
        shadowBatch.begin(shadowLight.getCamera());
        shadowBatch.render(world.instances);
        shadowBatch.render(course.startLine.instance);
        for (Gate gate : course.gates) shadowBatch.render(gate.instance);
        shadowBatch.render(pasture1Instance());
        shadowBatch.render(pasture2Instance());
        shadowBatch.render(horseInstance);
        if (!mounted) shadowBatch.render(walker.instance());
        shadowBatch.end();
        shadowLight.end();

        // --- main pass ---
        Gdx.gl.glClearColor(SKY.r, SKY.g, SKY.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        skyDome.transform.setToTranslation(camera.position.x, 0f, camera.position.z);
        batch.begin(camera);
        batch.render(skyDome);                       // unlit: no environment
        batch.render(clouds);
        batch.render(world.instances, environment);
        batch.render(course.startLine.instance, environment);
        for (Gate gate : course.gates) batch.render(gate.instance, environment);
        batch.render(pasture1Instance(), environment);
        batch.render(pasture2Instance(), environment);
        batch.render(horseInstance, environment);
        if (!mounted) batch.render(walker.instance(), environment);
        batch.end();

        if (mounted) hud.setGait(horse.gait.label, horse.speed);
        else hud.setGait(walker.paceLabel(), walker.speed);
        hud.update(delta);
        hud.draw();

        maybeScreenshot(delta);
    }

    private float screenshotTimer;

    /** Debug aid: capture one frame to disk and quit (desktop only). */
    private void maybeScreenshot(float delta) {
        if (com.ranchgame.HorseGame.screenshotPath == null) return;
        screenshotTimer += delta;
        if (screenshotTimer < com.ranchgame.HorseGame.screenshotDelay) return;
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        com.badlogic.gdx.graphics.Pixmap raw =
                com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0, 0, w, h);
        // GL's framebuffer origin is bottom-left, so flip into a correctly oriented image
        com.badlogic.gdx.graphics.Pixmap flipped =
                new com.badlogic.gdx.graphics.Pixmap(w, h, raw.getFormat());
        for (int y = 0; y < h; y++) {
            flipped.drawPixmap(raw, 0, y, 0, h - 1 - y, w, 1);
        }
        com.badlogic.gdx.graphics.PixmapIO.writePNG(
                Gdx.files.absolute(com.ranchgame.HorseGame.screenshotPath), flipped);
        raw.dispose();
        flipped.dispose();
        com.ranchgame.HorseGame.screenshotPath = null;
        Gdx.app.exit();
    }

    /** Push an entity out of obstacles and keep it inside the fenced area. */
    private void resolveEntity(Vector3 pos, float radius) {
        collisionPos[0] = pos.x;
        collisionPos[1] = pos.z;
        world.resolveCollision(collisionPos, radius);
        pos.x = MathUtils.clamp(collisionPos[0], -WorldBuilder.HALF + 0.8f, WorldBuilder.HALF - 0.8f);
        pos.z = MathUtils.clamp(collisionPos[1], -WorldBuilder.HALF + 0.8f, WorldBuilder.HALF - 0.8f);
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
        shadowBatch.dispose();
        shadowLight.dispose();
        world.dispose();
        textures.dispose();
        course.dispose();
        hud.dispose();
        horseModel.dispose();
        riderModel.dispose();
        pastureModel1.dispose();
        pastureModel2.dispose();
        skyModel.dispose();
        cloudModel.dispose();
    }
}
