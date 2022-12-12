/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import kotlin.text.StringsKt;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.greemdev.meteor.util.Strings;
import net.greemdev.meteor.utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ColorListSetting extends Setting<List<SettingColor>> {
    protected ColorListSetting(String name, String description, Object defaultValue, Consumer<List<SettingColor>> onChanged, Consumer<Setting<List<SettingColor>>> onModuleActivated, IVisible visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    @Override
    protected List<SettingColor> parseImpl(String str) {
        var split = StringsKt.split(str, new String[] {" "}, false, 0);

        if (split.size() != 1 && !split.get(0).equals(str)) {
            ArrayList<Color> colors = new ArrayList<>();

            for (Object part : split) {
                if (part instanceof String sp && sp.startsWith("(") && sp.endsWith(")") && sp.contains(","))
                    part = StringsKt.removeSuffix(StringsKt.removePrefix(sp, "("), ")");

                if (part instanceof String sp && StringsKt.all(sp, Character::isDigit)) {
                    part = Integer.parseInt(sp);
                }

                final Object fpart = part;

                utils.runOrIgnore(() -> colors.add(utils.colorOf(fpart)));
            }
            return colors.stream().map(Color::toSetting).toList();
        }

        return new ArrayList<>();
    }

    @Override
    protected boolean isValueValid(List<SettingColor> value) {
        return true;
    }

    @Override
    protected void resetImpl() {
        var d = getDefaultValue();
        value = new ArrayList<>(d.size());

        d.forEach(sc -> value.add(new SettingColor(sc)));
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.put("value", NbtUtils.listToTag(get()));

        return tag;
    }

    @Override
    protected List<SettingColor> load(NbtCompound tag) {
        get().clear();

        for (NbtElement e : tag.getList("value", NbtElement.COMPOUND_TYPE)) {
            get().add(new SettingColor().fromTag((NbtCompound) e));
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<SettingColor>, ColorListSetting> {
        public Builder() {
            super(new ArrayList<>());
        }

        @Override
        public ColorListSetting build() {
            return new ColorListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
