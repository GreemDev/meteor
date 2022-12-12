/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import com.google.common.collect.ImmutableList;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.greemdev.meteor.utils;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.function.Consumer;

public class ColorSetting extends Setting<SettingColor> {
    private static final List<String> SUGGESTIONS = ImmutableList.of("0 0 0 255", "225 25 25 255", "25 225 25 255", "25 25 225 255", "255 255 255 255");

    protected ColorSetting(String name, String description, Object defaultValue, Consumer<SettingColor> onChanged, Consumer<Setting<SettingColor>> onModuleActivated, IVisible visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    public static ColorSetting create(String name, String description, SettingColor defaultValue, Consumer<SettingColor> onChanged, Consumer<Setting<SettingColor>> onModuleActivated, IVisible visible, boolean serialize) {
        return new ColorSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    @Override
    protected SettingColor parseImpl(String str) {
        return utils.supplyOrNull(() ->
            utils.colorOf(str).toSetting());
    }

    @Override
    public void resetImpl() {
        if (value == null) value = new SettingColor(getDefaultValue());
        else value.set(getDefaultValue());
    }

    @Override
    protected boolean isValueValid(SettingColor value) {
        value.validate();

        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return SUGGESTIONS;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.put("value", get().toTag());

        return tag;
    }

    @Override
    public SettingColor load(NbtCompound tag) {
        get().fromTag(tag.getCompound("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, SettingColor, ColorSetting> {
        public Builder() {
            super(new SettingColor());
        }

        public Builder defaultValue(Color defaultValue) {
            return defaultValue(defaultValue.toSetting());
        }
        public Builder defaultValue(String defaultValue) {
            return defaultValue(utils.colorOf(defaultValue));
        }

        @Override
        public ColorSetting build() {
            if (onChanged == null)
                onChanged = RainbowColors::handle;

            return new ColorSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
