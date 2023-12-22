/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.greemdev.meteor.util.misc.Nbt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SettingGroup implements ISerializable<SettingGroup>, Iterable<Setting<?>> {
    public final String name;
    public boolean sectionExpanded;

    final List<Setting<?>> settings = new ArrayList<>(1);

    SettingGroup(String name, boolean sectionExpanded) {
        this.name = name;
        this.sectionExpanded = sectionExpanded;
    }

    public Setting<?> get(String name) {
        for (Setting<?> setting : this) {
            if (setting.name.equals(name)) return setting;
        }

        return null;
    }

    public SettingGroup collapsed() {
        this.sectionExpanded = false;
        return this;
    }

    public <V, S extends Setting<V>> S add(S setting) {
        settings.add(setting);

        return setting;
    }

    public <B extends Setting.SettingBuilder<B, V, S>, V, S extends Setting<V>> S add(B settingBuilder) {
        return add(settingBuilder.build());
    }

    public Setting<?> getByIndex(int index) {
        return settings.get(index);
    }

    @Override
    public Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        tag.putBoolean("sectionExpanded", sectionExpanded);

        tag.put("settings", Nbt.newList(list ->
            settings.stream()
                .filter(Setting::serialize)
                .forEach(setting -> {
                    if (setting.wasChanged()) list.add(setting.toTag());
                })
        ));

        return tag;
    }

    @Override
    public SettingGroup fromTag(NbtCompound tag) {
        sectionExpanded = tag.getBoolean("sectionExpanded");

        NbtList settingsTag = tag.getList("settings", 10);
        for (NbtElement t : settingsTag) {
            NbtCompound settingTag = (NbtCompound) t;

            Setting<?> setting = get(settingTag.getString("name"));
            if (setting != null) setting.fromTag(settingTag);
        }

        return this;
    }
}
