/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import kotlin.text.StringsKt;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> implements Supplier<T>, ISerializable<T> {
    private static final List<String> NO_SUGGESTIONS = new ArrayList<>(0);

    public final String name, title, description;

    private final IVisible visible;
    public boolean serialize = true;

    protected final Object defaultValue;
    protected T value;

    public final Consumer<Setting<T>> onModuleActivated;
    private final Consumer<T> onChanged;

    public Module module;
    public boolean lastWasVisible;

    public Setting(String name, String description, Object defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.defaultValue = defaultValue;
        this.onChanged = onChanged;
        this.onModuleActivated = onModuleActivated;
        this.visible = visible;

        resetImpl();
    }

    public Setting(String name, String description, Object defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible, boolean serialize) {
        this(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.serialize = serialize;
    }

    @Override
    public T get() {
        return value;
    }

    public boolean set(T value) {
        if (!isValueValid(value)) return false;
        this.value = value;
        onChanged();
        return true;
    }

    protected void resetImpl() {
        value = getDefaultValue();
    }

    public void reset() {
        resetImpl();
        onChanged();
    }

    public T getDefaultValue() {
        return Utils.cast(
            defaultValue instanceof Supplier<?> s
                ? s.get()
                : defaultValue
        );
    }

    public boolean parse(String str) {
        T newValue = parseImpl(str);

        if (newValue != null) {
            if (isValueValid(newValue)) {
                value = newValue;
                onChanged();
            }
        }

        return newValue != null;
    }

    public boolean wasChanged() {
        return !Objects.equals(value, getDefaultValue());
    }

    public void onChanged() {
        if (onChanged != null) onChanged.accept(value);
    }

    public void onActivated() {
        if (onModuleActivated != null) onModuleActivated.accept(this);
    }

    public boolean isVisible() {
        return visible == null || visible.isVisible();
    }

    protected abstract T parseImpl(String str);

    protected boolean isValueValid(T value) {
        return true;
    }

    public Iterable<Identifier> getIdentifierSuggestions() {
        return null;
    }

    public List<String> getSuggestions() {
        return NO_SUGGESTIONS;
    }

    protected abstract NbtCompound save(NbtCompound tag);

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        save(tag);

        return tag;
    }

    protected abstract T load(NbtCompound tag);

    @Override
    public T fromTag(NbtCompound tag) {
        T value = load(tag);
        onChanged();

        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Setting<?> setting = (Setting<?>) o;
        return Objects.equals(name, setting.name) && Objects.equals(module, setting.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static <T> T parseId(Registry<T> registry, String name) {
        name = name.trim();

        Identifier id = name.contains(":")
            ? new Identifier(name)
            : new Identifier("minecraft", name);

        return registry.containsId(id)
            ? registry.get(id)
            : null;
    }

    public abstract static class SettingBuilder<B, V, S> {
        protected String name = "undefined", description = "";
        protected Object defaultValue;
        protected IVisible visible;
        protected Consumer<V> onChanged;
        protected Consumer<Setting<V>> onModuleActivated;
        protected boolean serialize = true;

        protected SettingBuilder(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        public B name(String name) {
            this.name = name;
            return Utils.cast(this);
        }

        public B description(String description) {
            this.description = StringsKt.trimIndent(description); //automatic support for kotlin multiline strings
            return Utils.cast(this);
        }

        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return Utils.cast(this);
        }

        public B defaultValue(Supplier<V> defaultValue) {
            this.defaultValue = defaultValue;
            return Utils.cast(this);
        }

        public B serialize(boolean value) {
            this.serialize = value;
            return Utils.cast(this);
        }

        public B visible(IVisible visible) {
            this.visible = visible;
            return Utils.cast(this);
        }

        /**
         * Marks a setting as invisible in the UI.<br/>
         * Useful for values you want to use the setting system for but don't want to be directly configurable.
         * @return The current {@link Setting}
         */
        public B invisible() {
            this.visible = () -> false;
            return Utils.cast(this);
        }

        public B onChanged(Consumer<V> onChanged) {
            this.onChanged = onChanged;
            return Utils.cast(this);
        }

        public B onModuleActivated(Consumer<Setting<V>> onModuleActivated) {
            this.onModuleActivated = onModuleActivated;
            return Utils.cast(this);
        }

        public abstract S build();
    }
}
