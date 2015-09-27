package com.khrystunov.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.matsemann.libgdxloadingscreen.screen.LoadingScreen;

public class ThrustCopter extends Game {
    public static final int screenWidth = 800;
    public static final int screenHeight = 480;

    public Camera camera;
    public Viewport viewport;

    private FPSLogger fpsLogger;
    public SpriteBatch batch;
    public TextureAtlas atlas;

    public AssetManager manager;

    public BitmapFont font;
    public float soundVolume = 1;
    public boolean soundEnabled = true;

    public ThrustCopter() {
        fpsLogger = new FPSLogger();
        camera = new OrthographicCamera();
        camera.position.set(screenWidth / 2, screenHeight / 2, 0);
        viewport = new FitViewport(screenWidth, screenHeight, camera);
        manager = new AssetManager();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new LoadingScreen(this));
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
