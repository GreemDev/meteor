/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockPosSetting extends Setting<BlockPos> {
    public static Builder builder() {
        return new Builder();
    }

    protected BlockPosSetting(String name, String description, Object defaultValue, Consumer<BlockPos> onChanged, Consumer<Setting<BlockPos>> onModuleActivated, Supplier<Boolean> visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);
    }

    @Override
    protected BlockPos parseImpl(String str) {
        List<String> values = List.of(str.split(","));
        if (values.size() != 3) return null;

        BlockPos bp = null;
        try {
            bp = new BlockPos(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), Integer.parseInt(values.get(2)));
        }
        catch (NumberFormatException ignored) {}
        return bp;
    }

    @Override
    protected boolean isValueValid(BlockPos value) {
        return true;
    }

    @Override
    protected void save(NbtCompound tag) {
        tag.putIntArray("value", new int[] {value.getX(), value.getY(), value.getZ()});
    }

    @Override
    protected BlockPos load(NbtCompound tag) {
        int[] value = tag.getIntArray("value");
        set(new BlockPos(value[0], value[1], value[2]));

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
