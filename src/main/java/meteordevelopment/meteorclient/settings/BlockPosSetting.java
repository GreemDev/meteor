/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.greemdev.meteor.utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class BlockPosSetting extends Setting<BlockPos> {
    protected BlockPosSetting(String name, String description, Object defaultValue, Consumer<BlockPos> onChanged, Consumer<Setting<BlockPos>> onModuleActivated, IVisible visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    @Override
    protected BlockPos parseImpl(String str) {
        List<String> values = List.of(str.split(","));
        if (values.size() != 3) return null;

        return utils.supplyOrNull(() ->
            new BlockPos(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), Integer.parseInt(values.get(2)))
        );
    }

    @Override
    protected boolean isValueValid(BlockPos value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.putIntArray("value", new int[] {value.getX(), value.getY(), value.getZ()});

        return tag;
    }

    @Override
    protected BlockPos load(NbtCompound tag) {
        utils.apply(tag.getIntArray("value"), xyz ->
            set(new BlockPos(
                xyz[0], xyz[1], xyz[2]
            ))
        );

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, BlockPos, BlockPosSetting> {
        public Builder() {
            super(new BlockPos(0, 0, 0));
        }

        @Override
        public BlockPosSetting build() {
            return new BlockPosSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
        }
    }
}
