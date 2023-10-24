/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ActiveModulesChangedEvent extends Cancellable {

    private Module changed;

    private boolean toggled;

    public Module getChanged() {
        return changed;
    }

    public boolean wasEnabled() {
        return toggled;
    }

    private static final ActiveModulesChangedEvent INSTANCE = new ActiveModulesChangedEvent();

    public static ActiveModulesChangedEvent get(Module changed, boolean wasEnabled) {
        INSTANCE.setCancelled(false);
        INSTANCE.changed = changed;
        INSTANCE.toggled = wasEnabled;
        return INSTANCE;
    }
}
