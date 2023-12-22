/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import com.google.common.collect.ImmutableList;
import net.greemdev.meteor.Lambdas;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoolSetting extends Setting<Boolean> {
    public static Builder builder() {
        return new Builder();
    }

    private static final List<String> SUGGESTIONS = ImmutableList.of("true", "false", "toggle");

    protected BoolSetting(String name, String description, Object defaultValue, Consumer<Boolean> onChanged, Consumer<Setting<Boolean>> onModuleActivated, Supplier<Boolean> visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    @Override
    protected Boolean parseImpl(String str) {
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1")) return true;
        else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("0")) return false;
        else if (str.equalsIgnoreCase("toggle")) return !get();
        return null;
    }

    public Supplier<Boolean> inverse() {
        return Lambdas.invert(this);
    }

    @Override
    protected boolean isValueValid(Boolean value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return SUGGESTIONS;
    }

    @Override
    public void save(NbtCompound tag) {
        tag.putBoolean("value", get());
    }

    @Override
    public Boolean load(NbtCompound tag) {
        set(tag.getBoolean("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Boolean, BoolSetting> {
        public Builder() {
            super(false);
        }

        @Override
        public BoolSetting build() {
            return new BoolSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
