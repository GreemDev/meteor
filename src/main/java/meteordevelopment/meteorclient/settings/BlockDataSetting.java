/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.utils.misc.*;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BlockDataSetting<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends Setting<Map<Block, T>> {
    public final IGetter<T> defaultData;

    protected BlockDataSetting(String name, String description, Object defaultValue, Consumer<Map<Block, T>> onChanged, Consumer<Setting<Map<Block, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);

        this.defaultData = defaultData;
    }

    @Override
    public void resetImpl() {
        value = new HashMap<>(getDefaultValue());
    }

    @Override
    protected Map<Block, T> parseImpl(String str) {
        return new HashMap<>(0);
    }

    @Override
    protected boolean isValueValid(Map<Block, T> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (Block block : get().keySet()) {
            valueTag.put(Registry.BLOCK.getId(block).toString(), get().get(block).toTag());
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    protected Map<Block, T> load(NbtCompound tag) {
        get().clear();

        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            get().put(Registry.BLOCK.get(new Identifier(key)), defaultData.get().copy().fromTag(valueTag.getCompound(key)));
        }

        return get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends SettingBuilder<Builder<T>, Map<Block, T>, BlockDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(new HashMap<>(0));
        }

        public Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override
        public BlockDataSetting<T> build() {
            return new BlockDataSetting<>(name, description, defaultValue, onChanged, onModuleActivated, defaultData, visible, serialize);
        }
    }
}
