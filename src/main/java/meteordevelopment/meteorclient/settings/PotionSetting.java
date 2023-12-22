/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.utils.misc.PotionTypes;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PotionSetting extends EnumSetting<PotionTypes> {
    public static Builder builder() {
        return new Builder();
    }

    protected PotionSetting(String name, String description, Object defaultValue, Consumer<PotionTypes> onChanged, Consumer<Setting<PotionTypes>> onModuleActivated, Supplier<Boolean> visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    public static class Builder extends EnumSetting.Builder<PotionTypes> {
        @Override
        public EnumSetting<PotionTypes> build() {
            return new PotionSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
