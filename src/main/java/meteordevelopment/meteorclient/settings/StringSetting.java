/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

public class StringSetting extends Setting<String> {
    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;
    public final boolean wide;

    protected StringSetting(String name, String description, Object defaultValue, Consumer<String> onChanged, Consumer<Setting<String>> onModuleActivated, CharFilter filter, IVisible visible, boolean serialize, Class<? extends WTextBox.Renderer> renderer, boolean wide) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);

        this.renderer = renderer;
        this.filter = filter;
        this.wide = wide;
    }

    @Override
    protected String parseImpl(String str) {
        return str;
    }

    @Override
    protected boolean isValueValid(String value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", get());

        return tag;
    }

    @Override
    public String load(NbtCompound tag) {
        set(tag.getString("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, String, StringSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private CharFilter filter;
        private boolean wide;

        public Builder() {
            super(null);
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder filter(CharFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder wide() {
            wide = true;
            return this;
        }

        @Override
        public StringSetting build() {
            return new StringSetting(name, description, defaultValue, onChanged, onModuleActivated, filter, visible, serialize, renderer, wide);
        }
    }
}
