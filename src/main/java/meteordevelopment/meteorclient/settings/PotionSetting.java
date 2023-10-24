/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.utils.misc.PotionTypes;

import java.util.function.Consumer;

public class PotionSetting extends EnumSetting<PotionTypes> {
    protected PotionSetting(String name, String description, Object defaultValue, Consumer<PotionTypes> onChanged, Consumer<Setting<PotionTypes>> onModuleActivated, IVisible visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    public static class Builder extends EnumSetting.Builder<PotionTypes> {
        @Override
        public EnumSetting<PotionTypes> build() {
            return new PotionSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
