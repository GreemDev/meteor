/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.nbt.NbtCompound;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumSetting<T extends Enum<?>> extends Setting<T> {
    public static <T extends Enum<?>> Builder<T> builder() {
        return new Builder<>();
    }

    private T[] values;

    private final List<String> suggestions;

    protected EnumSetting(String name, String description, Object defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, Supplier<Boolean> visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);

        try {
            values = (T[]) defaultValue.getClass().getMethod("values").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            MeteorClient.LOG.error("EnumSetting constructor encountered error", e);
        }

        suggestions = new ArrayList<>(values.length);
        for (T value : values) suggestions.add(value.toString());
    }

    @Override
    protected T parseImpl(String str) {
        for (T possibleValue : values) {
            if (str.equalsIgnoreCase(possibleValue.toString())) return possibleValue;
        }

        return null;
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public void save(NbtCompound tag) {
        tag.putString("value", get().toString());
    }

    @Override
    public T load(NbtCompound tag) {
        parse(tag.getString("value"));

        return get();
    }

    public static class Builder<T extends Enum<?>> extends SettingBuilder<Builder<T>, T, EnumSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public EnumSetting<T> build() {
            return new EnumSetting<>(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
