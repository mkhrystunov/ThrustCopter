package com.khrystunov.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopter extends Game {
    public static final int screenWidth = 800;
    public static final int screenHeight = 480;

    public Camera camera;
    public Viewport viewport;

    private FPSLogger fpsLogger;
    public SpriteBatch batch;
    public TextureAtlas atlas;

    public AssetManager manager;

    public ThrustCopter() {
        fpsLogger = new FPSLogger();
        camera = new OrthographicCamera();
        camera.position.set(screenWidth / 2, screenHeight / 2, 0);
        viewport = new FitViewport(screenWidth, screenHeight, camera);
        manager = new AssetManager();
    }

    @Override
    public void create() {
        manager.load("gameover.png", Texture.class);
        manager.load("sounds/journey_3.mp3", Music.class);
        manager.load("sounds/pop.ogg", Sound.class);
        manager.load("sounds/crash.ogg", Sound.class);
        manager.load("sounds/alarm.ogg", Sound.class);
        manager.load("ThrustCopter.pack", TextureAtlas.class);
        manager.finishLoading();

        batch = new SpriteBatch();
        atlas = manager.get("ThrustCopter.pack", TextureAtlas.class);
        setScreen(new ThrustCopterScene(this));
    }

    @Override
    public void render() {
        fpsLogger.log();
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
    }
}
