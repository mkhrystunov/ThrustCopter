package com.khrystunov.thrustcopter.box2d;

import com.badlogic.gdx.physics.box2d.World;
import com.khrystunov.thrustcopter.BaseScene;
import com.khrystunov.thrustcopter.ThrustCopter;

public class ThrustCopterSceneBox2D extends BaseScene {

    World world;

    public ThrustCopterSceneBox2D(ThrustCopter thrustCopter) {
        super(thrustCopter);
    }
}
