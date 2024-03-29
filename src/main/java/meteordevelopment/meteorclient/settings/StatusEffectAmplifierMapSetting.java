/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StatusEffectAmplifierMapSetting extends Setting<Object2IntMap<StatusEffect>> {
    public static Builder builder() {
        return new Builder();
    }

    protected StatusEffectAmplifierMapSetting(String name, String description, Object defaultValue, Consumer<Object2IntMap<StatusEffect>> onChanged, Consumer<Setting<Object2IntMap<StatusEffect>>> onModuleActivated, Supplier<Boolean> visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        value = new Object2IntArrayMap<>(getDefaultValue());
    }

    @Override
    protected Object2IntMap<StatusEffect> parseImpl(String str) {
        String[] values = str.split(",");
        Object2IntMap<StatusEffect> effects = Utils.createStatusEffectMap();

        try {
            for (String value : values) {
                String[] split = value.split(" ");

                StatusEffect effect = parseId(Registries.STATUS_EFFECT, split[0]);
                int level = Integer.parseInt(split[1]);

                effects.put(effect, level);
            }
        } catch (Exception ignored) {}

        return effects;
    }

    @Override
    protected boolean isValueValid(Object2IntMap<StatusEffect> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (StatusEffect statusEffect : get().keySet()) {
            Identifier id = Registries.STATUS_EFFECT.getId(statusEffect);
            if (id != null) valueTag.putInt(id.toString(), get().getInt(statusEffect));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public Object2IntMap<StatusEffect> load(NbtCompound tag) {
        get().clear();

        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            StatusEffect statusEffect = Registries.STATUS_EFFECT.get(new Identifier(key));
            if (statusEffect != null) get().put(statusEffect, valueTag.getInt(key));
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Object2IntMap<StatusEffect>, StatusEffectAmplifierMapSetting> {
        public Builder() {
            super(new Object2IntArrayMap<>(0));
        }

        @Override
        public StatusEffectAmplifierMapSetting build() {
            return new StatusEffectAmplifierMapSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
