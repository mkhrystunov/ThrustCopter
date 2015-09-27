package com.khrystunov.thrustcopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ThrustCopterScene extends ScreenAdapter {
    private ThrustCopter game;

    static enum GameState {
        INIT,
        ACTION,
        GAME_OVER
    }

    TextureRegion gameOver;

    GameState gameState = GameState.INIT;
    SpriteBatch batch;
    Camera camera;

    Music music;
    Sound tapSound, crashSound, spawnSound;

    TextureAtlas atlas;
    TextureRegion background;

    TextureRegion terrainBelow, terrainAbove;
    int terrainOffset = 0;

    Animation plane;
    float planeAnimTime = 0.00f;
    Vector2 planeVelocity = new Vector2();
    Vector2 planePosition = new Vector2();
    Vector2 planeDefaultPosition = new Vector2();
    Vector2 gravity = new Vector2();
    Vector2 scrollVelocity = new Vector2();
    private static final Vector2 damping = new Vector2(0.99f, 0.99f);

    Animation shield;

    Array<Vector2> pillars = new Array<Vector2>();
    Vector2 lastPillarPosition = new Vector2();
    TextureRegion pillarUp, pillarDown;

    Array<TextureAtlas.AtlasRegion> meteorTextures = new Array<TextureAtlas.AtlasRegion>();
    TextureRegion selectedMeteorTexture;
    boolean meteorInScene;
    private static final int METEOR_SPEED = 25;
    Vector2 meteorPosition = new Vector2();
    Vector2 meteorVelocity = new Vector2();
    float nextMeteorIn;

    Vector3 pickupTiming = new Vector3();
    Array<Pickup> pickupsInScene = new Array<Pickup>();
    int starCount;
    float score;
    float fuelCount;
    float shieldCount;
    TextureRegion fuelIndicator;
    float fuelPercentage;

    InputAdapter inputAdapter = new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (game.soundEnabled) tapSound.play();
            touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);

            camera.unproject(touchPosition);

            if (fuelCount > 0) {
                boolean isUp = touchPosition.y < game.viewport.getWorldHeight() / 2;
                tmpVector.set(0, isUp ? TOUCH_IMPULSE : -TOUCH_IMPULSE);
                planeVelocity.add(tmpVector);
            }
//            tmpVector.set(planePosition.x, planePosition.y);
//            tmpVector.sub(touchPosition.x, touchPosition.y).nor();
//            planeVelocity.mulAdd(
//                    tmpVector,
//                    TOUCH_IMPULSE - MathUtils.clamp(
//                            Vector2.dst(touchPosition.x, touchPosition.y, planePosition.x, planePosition.y),
//                            0,
//                            TOUCH_IMPULSE
//                    )
//            );
            tapDrawTime = TAP_DRAW_TIME_MAX;
            return true;
        }
    };

    Vector3 touchPosition = new Vector3();
    Vector2 tmpVector = new Vector2();
    private static final int TOUCH_IMPULSE = 250;
    TextureRegion tap1, tap2;
    float tapDrawTime;
    private static final float TAP_DRAW_TIME_MAX = 0.5f;

    ParticleEffect smoke, explosion;

    public ThrustCopterScene(ThrustCopter thrustCopter) {
        game = thrustCopter;
        batch = thrustCopter.batch;
        camera = thrustCopter.camera;
        atlas = thrustCopter.atlas;

        music = thrustCopter.manager.get("sounds/journey_3.mp3", Music.class);
        music.setLooping(true);
        if (game.soundEnabled) music.play();
        if (game.soundEnabled) {
            tapSound = thrustCopter.manager.get("sounds/pop.ogg", Sound.class);
            crashSound = thrustCopter.manager.get("sounds/crash.ogg", Sound.class);
            spawnSound = thrustCopter.manager.get("sounds/alarm.ogg", Sound.class);
        }

        background = atlas.findRegion("background");
        terrainBelow = atlas.findRegion("groundGrass");
        terrainAbove = new TextureRegion(terrainBelow);
        terrainAbove.flip(true, true);

        pillarUp = atlas.findRegion("rockGrassUp");
        pillarDown = atlas.findRegion("rockGrassDown");

        meteorTextures.add(atlas.findRegion("meteorBrown_med1"));
        meteorTextures.add(atlas.findRegion("meteorBrown_med2"));
        meteorTextures.add(atlas.findRegion("meteorBrown_small1"));
        meteorTextures.add(atlas.findRegion("meteorBrown_small2"));
        meteorTextures.add(atlas.findRegion("meteorBrown_tiny1"));
        meteorTextures.add(atlas.findRegion("meteorBrown_tiny2"));

        fuelIndicator = atlas.findRegion("life");

        plane = new Animation(
                0.05f,
                atlas.findRegion("planeRed1"),
                atlas.findRegion("planeRed2"),
                atlas.findRegion("planeRed3")
        );
        plane.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        shield = new Animation(
                0.05f,
                atlas.findRegion("new_shield1"),
                atlas.findRegion("new_shield2"),
                atlas.findRegion("new_shield3")
        );
        shield.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        gameOver = atlas.findRegion("gameover");

        smoke = thrustCopter.manager.get("particles/smoke", ParticleEffect.class);
        explosion = thrustCopter.manager.get("particles/explosion", ParticleEffect.class);

        tap1 = atlas.findRegion("tap1");
        tap2 = atlas.findRegion("tap2");
        Gdx.input.setInputProcessor(inputAdapter);
        resetScene();
    }

    private void resetScene() {
        pillars.clear();
        pickupsInScene.clear();
        pickupTiming.x = 1 + (float) Math.random() * 2;
        pickupTiming.y = 3 + (float) Math.random() * 2;
        pickupTiming.z = 1 + (float) Math.random() * 3;
        fuelCount = 100;
        shieldCount = 5;
        starCount = 0;
        score = 0;
        fuelPercentage = 114;
        lastPillarPosition = new Vector2();
        meteorInScene = false;
        nextMeteorIn = (float) Math.random() * 5;
        terrainOffset = 0;
        planeAnimTime = 0;
        planeVelocity.set(400, 0);
        scrollVelocity.set(4, 0);
        gravity.set(0, -4);
        planeDefaultPosition.set(200 - 88 / 2, 240 - 73 / 2);
        planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
        smoke.reset();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        updateScene();
        drawScene();
    }

    private void updateScene() {
        if (Gdx.input.justTouched()) {
            switch (gameState) {
                case INIT:
                    gameState = GameState.ACTION;
                    break;
                case GAME_OVER:
                    gameState = GameState.INIT;
                    resetScene();
                    break;
            }
        }
        float deltaTime = Gdx.graphics.getDeltaTime();

        tapDrawTime -= deltaTime;

        if (gameState != GameState.ACTION) {
            if (gameState == GameState.GAME_OVER) explosion.update(deltaTime);
            return;
        }
        checkAndAddPickup(deltaTime);

        planeAnimTime += deltaTime * 0.5;
        score += deltaTime;

        planeVelocity.scl(damping);
        planeVelocity.add(gravity);
        planeVelocity.add(scrollVelocity);
        planePosition.mulAdd(planeVelocity, deltaTime);
        float deltaPosition = planePosition.x - planeDefaultPosition.x;

        fuelCount -= 6 * deltaTime;
        fuelPercentage = fuelIndicator.getRegionWidth() * fuelCount / 100;
        shieldCount -= deltaTime;

        terrainOffset -= deltaPosition;
        planePosition.x = planeDefaultPosition.x;

        smoke.setPosition(planePosition.x + 20, planePosition.y + 30);
        smoke.update(deltaTime);

        if (lastPillarPosition.x < 600) {
            addPillar();
        }

        if (terrainOffset * -1 > terrainBelow.getRegionWidth()) {
            terrainOffset = 0;
        } else if (terrainOffset > 0) {
            terrainOffset = -terrainBelow.getRegionWidth();
        }

        if (planePosition.y < terrainBelow.getRegionHeight() - 35 ||
                planePosition.y + 73 > 480 - terrainAbove.getRegionHeight() + 35) {
            endGame();
        }

        if (meteorInScene) {
            meteorPosition.mulAdd(meteorVelocity, deltaTime);
            meteorPosition.x -= deltaPosition;
            if (meteorPosition.x < -10) {
                meteorInScene = false;
            }
        }
        nextMeteorIn -= deltaTime;
        if (nextMeteorIn <= 0) {
            launchMeteor();
        }

        Rectangle planeRect = new Rectangle();
        Rectangle obstacleRect = new Rectangle();

        planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);
        if (meteorInScene) {
            obstacleRect.set(
                    meteorPosition.x + 2,
                    meteorPosition.y + 2,
                    selectedMeteorTexture.getRegionWidth() - 4,
                    selectedMeteorTexture.getRegionHeight() - 4
            );
            if (planeRect.overlaps(obstacleRect) && shieldCount <= 0) {
                endGame();
            }
        }
        for (Vector2 vec : pillars) {
            vec.x -= deltaPosition;
            if (vec.x + pillarUp.getRegionWidth() < -10) {
                pillars.removeValue(vec, false);
            }

            if (vec.y == 1) {
                obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth() - 20, pillarUp.getRegionHeight() - 10);
            } else {
                obstacleRect.set(
                        vec.x + 10,
                        480 - pillarDown.getRegionHeight() + 10,
                        pillarUp.getRegionWidth() - 20,
                        pillarUp.getRegionHeight()
                );
            }

            if (planeRect.overlaps(obstacleRect) && shieldCount <= 0) {
                endGame();
            }
        }
        for (Pickup pickup : pickupsInScene) {
            pickup.pickupPosition.x -= deltaPosition;
            if (pickup.pickupPosition.x + pickup.pickupTexture.getRegionWidth() < -10) {
                pickupsInScene.removeValue(pickup, false);
            }
            obstacleRect.set(
                    pickup.pickupPosition.x,
                    pickup.pickupPosition.y,
                    pickup.pickupTexture.getRegionWidth(),
                    pickup.pickupTexture.getRegionHeight()
            );
            if (planeRect.overlaps(obstacleRect)) {
                pickIt(pickup);
            }
        }
    }

    private void drawScene() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.disableBlending();
        batch.draw(background, 0, 0);
        batch.enableBlending();
        for (Vector2 vec : pillars) {
            if (vec.y == 1) {
                batch.draw(pillarUp, vec.x, 0);
            } else {
                batch.draw(pillarDown, vec.x, 480 - pillarDown.getRegionHeight());
            }
        }
        if (meteorInScene) {
            batch.draw(selectedMeteorTexture, meteorPosition.x, meteorPosition.y);
        }
        for (Pickup pickup : pickupsInScene) {
            batch.draw(pickup.pickupTexture, pickup.pickupPosition.x, pickup.pickupPosition.y);
        }
        batch.draw(terrainBelow, terrainOffset, 0);
        batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
        batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
        batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480 - terrainAbove.getRegionHeight());

        TextureRegion planeKeyFrame = plane.getKeyFrame(planeAnimTime);

        if (tapDrawTime > 0) {
            batch.draw(tap2, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
        }
        switch (gameState) {
            case INIT:
                batch.draw(planeKeyFrame, planePosition.x, planePosition.y);
                batch.draw(tap1, planePosition.x, planePosition.y - 80);
                break;
            case GAME_OVER:
                batch.draw(gameOver, 400 - 206, 240 - 80);
                explosion.draw(batch);
                break;
            case ACTION:
                batch.draw(planeKeyFrame, planePosition.x, planePosition.y);
                if (shieldCount > 0) {
                    TextureRegion shieldKeyFrame = shield.getKeyFrame(planeAnimTime);
                    float x = planePosition.x - (shieldKeyFrame.getRegionWidth() - planeKeyFrame.getRegionWidth()) / 2;
                    float y = planePosition.y - (planeKeyFrame.getRegionWidth() - planeKeyFrame.getRegionHeight()) / 2;
                    batch.setColor(Color.BLUE);
                    batch.draw(shield.getKeyFrame(planeAnimTime), x, y);
                    batch.setColor(Color.WHITE);
                }
                smoke.draw(batch);
                if (shieldCount > 0) {
                    game.font.draw(batch, String.format("%d", (int) shieldCount), 390, 450);
                }
                game.font.draw(batch, String.format("%d", (int) (starCount + score)), 700, 450);
                break;
        }
        batch.setColor(Color.BLACK);
        batch.draw(fuelIndicator, 10, 350);
        batch.setColor(Color.WHITE);
        batch.draw(fuelIndicator, 10, 350, 0, 0, fuelPercentage, fuelIndicator.getRegionHeight(), 1, 1, 0);
        batch.end();
    }

    private void addPillar() {
        Vector2 pillarPosition = new Vector2();
        if (pillars.size == 0) {
            pillarPosition.x = (float) (800 + Math.random() * 600);
        } else {
            pillarPosition.x = lastPillarPosition.x + (float) (600 + Math.random() * 600);
        }
        if (MathUtils.randomBoolean()) {
            pillarPosition.y = 1;
        } else {
            pillarPosition.y = -1;
        }
        lastPillarPosition = pillarPosition;
        pillars.add(pillarPosition);
    }

    private void launchMeteor() {
        nextMeteorIn = 1.5f + (float) Math.random() * 5;
        if (meteorInScene) {
            return;
        }
        if (game.soundEnabled) spawnSound.play();
        meteorInScene = true;
        int id = (int) (Math.random() * meteorTextures.size);
        selectedMeteorTexture = meteorTextures.get(id);
        meteorPosition.x = 810;
        meteorPosition.y = (float) (80 + Math.random() * 320);
        Vector2 destination = new Vector2();
        destination.x -= 10;
        destination.y = (float) (80 + Math.random() * 320);
        destination.sub(meteorPosition).nor();
        meteorVelocity.mulAdd(destination, METEOR_SPEED);
    }

    private void checkAndAddPickup(float delta) {
        pickupTiming.sub(delta);
        if (pickupTiming.x <= 0) {
            pickupTiming.x = (float) (0.5 + Math.random() * 0.5);
            if (addPickup(Pickup.STAR)) {
                pickupTiming.x = 1 + (float) Math.random() * 2;
            }
        }
        if (pickupTiming.y <= 0) {
            pickupTiming.y = (float) (0.5 + Math.random() * 0.5);
            if (addPickup(Pickup.FUEL)) {
                pickupTiming.y = 3 + (float) Math.random() * 2;
            }
        }
        if (pickupTiming.z <= 0) {
            pickupTiming.z = (float) (0.5 + Math.random() * 0.5);
            if (addPickup(Pickup.SHIELD)) {
                pickupTiming.z = 10 + (float) Math.random() * 3;
            }
        }
    }

    private boolean addPickup(int pickupType) {
        Vector2 randomPosition = new Vector2();
        randomPosition.x = 820;
        randomPosition.y = (float) (80 + Math.random() * 320);
        Rectangle obstacleRect = new Rectangle();
        for (Vector2 vec : pillars) {
            obstacleRect.set(
                    vec.x,
                    vec.y == 1 ? 0 : 480 - pillarDown.getRegionHeight(),
                    pillarUp.getRegionHeight(),
                    pillarUp.getRegionWidth()
            );
            if (obstacleRect.contains(randomPosition)) {
                return false;
            }
        }
        Pickup tempPickup = new Pickup(pickupType, game.manager);
        tempPickup.pickupPosition.set(randomPosition);
        pickupsInScene.add(tempPickup);
        return true;
    }

    private void pickIt(Pickup pickup) {
        if (game.soundEnabled) pickup.pickupSound.play();
        switch (pickup.pickupType) {
            case Pickup.STAR:
                starCount += pickup.pickupValue;
                break;
            case Pickup.SHIELD:
                shieldCount = pickup.pickupValue;
                break;
            case Pickup.FUEL:
                fuelCount = pickup.pickupValue;
                break;
        }
        pickupsInScene.removeValue(pickup, false);
    }

    private void endGame() {
        if (gameState != GameState.GAME_OVER) {
            if (game.soundEnabled) crashSound.play();
            gameState = GameState.GAME_OVER;
            explosion.reset();
            explosion.setPosition(planePosition.x + 40, planePosition.y + 40);
        }
    }
}
