/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleSetting extends Setting<Double> {
    public static Builder builder() {
        return new Builder();
    }

    public final double min, max;
    public final double sliderMin, sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    protected DoubleSetting(String name, String description, Object defaultValue, Consumer<Double> onChanged, Consumer<Setting<Double>> onModuleActivated, Supplier<Boolean> visible, double min, double max, double sliderMin, double sliderMax, boolean onSliderRelease, int decimalPlaces, boolean noSlider) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.decimalPlaces = decimalPlaces;
        this.onSliderRelease = onSliderRelease;
        this.noSlider = noSlider;
    }

    public float getAsFloat() {
        return get().floatValue();
    }

    @Override
    protected Double parseImpl(String str) {
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(Double value) {
        return value >= min && value <= max;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.putDouble("value", get());

        return tag;
    }

    @Override
    public Double load(NbtCompound tag) {
        set(tag.getDouble("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Double, DoubleSetting> {
        public double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
        public double sliderMin = 0, sliderMax = 10;
        public boolean onSliderRelease = false;
        public int decimalPlaces = 3;
        public boolean noSlider = false;

        public Builder() {
            super(0D);
        }

        public Builder defaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return sliderMin(min);
        }

        public Builder max(double max) {
            this.max = max;
            return sliderMax(max);
        }

        public Builder range(double min, double max) {
            return min(Math.min(min, max)).max(Math.max(min, max));
        }

        public Builder sliderMin(double min) {
            sliderMin = min;
            return this;
        }

        public Builder sliderMax(double max) {
            sliderMax = max;
            return this;
        }

        public Builder sliderRange(double min, double max) {
            return sliderMin(Math.min(min, max)).sliderMax(Math.max(min, max));
        }

        public Builder onSliderRelease() {
            onSliderRelease = true;
            return this;
        }

        public Builder decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            return this;
        }

        public Builder noSlider() {
            noSlider = true;
            return this;
        }

        public DoubleSetting build() {
            return new DoubleSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, min, max, Math.max(sliderMin, min), Math.min(sliderMax, max), onSliderRelease, decimalPlaces, noSlider);
        }
    }
}
