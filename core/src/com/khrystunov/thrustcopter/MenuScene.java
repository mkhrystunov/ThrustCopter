package com.khrystunov.thrustcopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MenuScene extends ScreenAdapter {

    private final Table table;
    private final TextButton playButton;
    private final TextButton optionsButton;
    private final TextButton exitButton;
    private final Table options;
    private final CheckBox muteCheckbox;
    private final Slider volumeSlider;
    private final TextButton backButton;
    private ThrustCopter game;
    private Stage stage;
    private Skin skin;
    private Image screenBg;
    private Image title;
    private Label helpTip;

    public MenuScene(ThrustCopter thrustCopter) {
        game = thrustCopter;
        stage = new Stage(game.viewport);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        screenBg = new Image(game.atlas.findRegion("background"));
        title = new Image(game.manager.get("title.png", Texture.class));
        helpTip = new Label("Tap around the plane to move it", skin);
        helpTip.setColor(Color.NAVY);
        table = new Table().debug();
        playButton = new TextButton("PLAY GAME", skin);
        table.add(playButton).padBottom(10);
        table.row();
        optionsButton = new TextButton("SOUND OPTIONS", skin);
        table.add(optionsButton).padBottom(10);
        table.row();
        table.add(new TextButton("LEADERBOARD", skin)).padBottom(10);
        table.row();
        exitButton = new TextButton("EXIT GAME", skin);
        table.add(exitButton);
        table.setPosition(400, -200);

        options = new Table().debug();
        Label soundTitle = new Label("SOUND OPTIONS", skin);
        soundTitle.setColor(Color.NAVY);
        options.add(soundTitle).padBottom(25).colspan(2);
        options.row();
        muteCheckbox = new CheckBox(" MUTE ALL", skin);
        options.add(muteCheckbox).padBottom(10).colspan(2);
        options.row();
        options.add(new Label("VOLUME", skin)).padBottom(10).padRight(10);
        volumeSlider = new Slider(0, 2, 0.2f, false, skin);
        options.add(volumeSlider).padTop(10).padBottom(20);
        options.row();
        backButton = new TextButton("BACK", skin);
        options.add(backButton).colspan(2).padTop(20);
        options.setPosition(400, -200);
        muteCheckbox.setChecked(!game.soundEnabled);
        volumeSlider.setValue(game.soundVolume);

        stage.addActor(screenBg);
        stage.addActor(title);
        stage.addActor(helpTip);
        stage.addActor(table);
        stage.addActor(options);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ThrustCopterScene(game));
            }
        });
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(false);
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.soundVolume = volumeSlider.getValue();
            }
        });
        muteCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.soundEnabled = !muteCheckbox.isChecked();
            }
        });
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(true);
            }
        });
    }

    @Override
    public void show() {
        title.setPosition(400 - title.getWidth() / 2, 450);
        helpTip.setPosition(400 - helpTip.getWidth() / 2, 30);

        MoveToAction actionMove = Actions.action(MoveToAction.class);
        actionMove.setPosition(400 - title.getWidth() / 2, 320);
        actionMove.setDuration(2);
        actionMove.setInterpolation(Interpolation.elasticOut);
        title.addAction(actionMove);

        showMenu(true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
        super.render(delta);
    }

    private void showMenu(boolean flag) {
        MoveToAction actionMoveOut = Actions.action(MoveToAction.class);
        actionMoveOut.setPosition(400, -200);
        actionMoveOut.setDuration(1);
        actionMoveOut.setInterpolation(Interpolation.swingIn);

        MoveToAction actionMoveIn = Actions.action(MoveToAction.class);
        actionMoveIn.setPosition(400, 190);
        actionMoveIn.setDuration(1.5f);
        actionMoveIn.setInterpolation(Interpolation.swing);

        table.addAction(flag ? actionMoveIn : actionMoveOut);
        options.addAction(flag ? actionMoveOut : actionMoveIn);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
