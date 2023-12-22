/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.events.entity.player;

import net.greemdev.meteor.Observable;

import static net.greemdev.meteor.observation.*;

public class OffGroundSpeedEvent {
    public static final OffGroundSpeedEvent INSTANCE = new OffGroundSpeedEvent();

    @SuppressWarnings("unchecked")
    private final Observable<Float> speed = observable(0f,
        observer((f1, f2) ->
            speedChanged = true
        )
    );

    private boolean speedChanged;

    public static OffGroundSpeedEvent get(float speed) {
        INSTANCE.speed.set(speed);
        INSTANCE.speedChanged = false;
        return INSTANCE;
    }

    public OffGroundSpeedEvent setSpeed(float speed) {
        this.speed.set(speed);
        return this;
    }

    public boolean wasSpeedChanged() {
        return speedChanged;
    }

    public float getSpeed() {
        return speed.get();
    }
}
