/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import kotlin.collections.CollectionsKt;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.greemdev.meteor.util.misc.Nbt;
import net.greemdev.meteor.util.misc.NbtUtil;
import net.greemdev.meteor.utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class SettingGroup implements ISerializable<SettingGroup>, Iterable<Setting<?>> {
    public final String name;
    public boolean sectionExpanded;

    final List<Setting<?>> settings = new ArrayList<>(1);

    SettingGroup(String name, boolean sectionExpanded) {
        this.name = name;
        this.sectionExpanded = sectionExpanded;
    }

    public Setting<?> get(String name) {
        return CollectionsKt.firstOrNull(this, setting ->
            setting.name.equals(name)
        );
    }

    public <T> Setting<T> getAs(String name, Class<? extends Setting<T>> clazz) {
        var setting = get(name);
        return setting != null
            ? clazz.cast(setting)
            : null;
    }

    public SettingGroup collapsed() {
        sectionExpanded = false;
        return this;
    }

    public SettingGroup expanded() {
        sectionExpanded = true;
        return this;
    }

    public Setting<?> get(int index) {
        return settings.get(index);
    }

    public <T> Setting<T> getAs(int index, Class<? extends Setting<T>> clazz) {
        return clazz.cast(get(index));
    }

    public <T> Setting<T> add(Setting<T> setting) {
        settings.add(setting);

        return setting;
    }

    public <B extends Setting.SettingBuilder<B, V, S>, V, S extends Setting<V>> Setting<V> add(B builder) {
        return utils.let(builder.build(), s -> {
           settings.add(s);
           return s;
        });
    }

    @Override
    public Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }

    @Override
    public NbtCompound toTag() {
        return Nbt.newCompound(tag -> {
            tag.putString("name", name);
            tag.putBoolean("sectionExpanded", sectionExpanded);

            tag.put("settings", Nbt.newList(l ->
                this.forEach(setting -> {
                    if (setting.serialize && setting.wasChanged())
                        l.add(setting.toTag());
                })
            ));
        });
    }

    @Override
    public SettingGroup fromTag(NbtCompound tag) {
        sectionExpanded = tag.getBoolean("sectionExpanded");

        NbtList settingsTag = tag.getList("settings", 10);
        for (NbtElement t : settingsTag) {
            NbtCompound settingTag = (NbtCompound) t;

            Setting<?> setting = get(settingTag.getString("name"));
            if (setting != null && setting.serialize)
                setting.fromTag(settingTag);
        }

        return this;
    }
}
