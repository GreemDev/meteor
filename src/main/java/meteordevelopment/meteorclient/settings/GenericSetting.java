/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericSetting<T extends ICopyable<T> & ISerializable<T> & IScreenFactory> extends Setting<T> {
    public static <T extends ICopyable<T> & ISerializable<T> & IScreenFactory> Builder<T> builder() {
        return new Builder<>();
    }

    protected GenericSetting(String name, String description, Object defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, Supplier<Boolean> visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    @Override
    public void resetImpl() {
        if (value == null)
            value = getDefaultValue().copy();
        else
            value.set(getDefaultValue());
    }

    @Override
    protected T parseImpl(String str) {
        return getDefaultValue().copy();
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    public void save(NbtCompound tag) {
        tag.put("value", get().toTag());
    }

    @Override
    public T load(NbtCompound tag) {
        get().fromTag(tag.getCompound("value"));

        return get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IScreenFactory> extends SettingBuilder<Builder<T>, T, GenericSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public GenericSetting<T> build() {
            return new GenericSetting<>(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
