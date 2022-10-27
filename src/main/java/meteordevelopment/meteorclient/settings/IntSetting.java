/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

public class IntSetting extends Setting<Integer> {
    public int min, max;
    public int sliderMin, sliderMax;
    public final boolean noSlider;

    protected IntSetting(String name, String description, Object defaultValue, Consumer<Integer> onChanged, Consumer<Setting<Integer>> onModuleActivated, IVisible visible, boolean serialize, int min, int max, int sliderMin, int sliderMax, boolean noSlider) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.noSlider = noSlider;
    }

    @Override
    protected Integer parseImpl(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(Integer value) {
        return value >= min && value <= max;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putInt("value", get());

        return tag;
    }

    @Override
    public Integer load(NbtCompound tag) {
        set(tag.getInt("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Integer, IntSetting> {
        private int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
        private int sliderMin = 0, sliderMax = 10;
        private boolean noSlider = false;

        public Builder() {
            super(0);
        }



        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public Builder min(int min) {
            this.min = min;
            this.sliderMin = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            this.sliderMax = max;
            return this;
        }

        public Builder range(int min, int max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            sliderRange(min, max);
            return this;
        }

        public Builder sliderMin(int min) {
            this.sliderMin = Math.max(min, this.min);
            return this;
        }

        public Builder sliderMax(int max) {
            this.sliderMax = Math.min(max, this.max);
            return this;
        }

        public Builder sliderRange(int min, int max) {
            this.sliderMin = Math.max(min, this.min);
            this.sliderMax = Math.min(max, this.max);
            return this;
        }

        public Builder noSlider() {
            noSlider = true;
            return this;
        }

        @Override
        public IntSetting build() {
            return new IntSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize, min, max, sliderMin, sliderMax, noSlider);
        }
    }
}
