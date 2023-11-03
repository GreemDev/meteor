/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.nbt.NbtCompound;
import org.joml.Vector3d;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Vector3dSetting extends Setting<Vector3d> {
    public static Builder builder() {
        return new Builder();
    }

    public final double min, max;
    public final double sliderMin, sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    protected Vector3dSetting(String name, String description, Object defaultValue, Consumer<Vector3d> onChanged, Consumer<Setting<Vector3d>> onModuleActivated, Supplier<Boolean> visible, double min, double max, double sliderMin, double sliderMax, boolean onSliderRelease, int decimalPlaces, boolean noSlider) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.decimalPlaces = decimalPlaces;
        this.onSliderRelease = onSliderRelease;
        this.noSlider = noSlider;
    }

    public boolean set(double x, double y, double z) {
        value.set(x, y, z);
        return super.set(value);
    }

    @Override
    protected void resetImpl() {
        if (value == null)
            value = getDefaultValue();
        else
            value.set(getDefaultValue());
    }

    @Override
    protected Vector3d parseImpl(String str) {
        try {
            String[] strs = str.split(" ");
            return new Vector3d(Double.parseDouble(strs[0]), Double.parseDouble(strs[1]), Double.parseDouble(strs[2]));
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(Vector3d value) {
        return value.x >= min && value.x <= max && value.y >= min && value.y <= max && value.z >= min && value.z <= max;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        valueTag.putDouble("x", get().x);
        valueTag.putDouble("y", get().y);
        valueTag.putDouble("z", get().z);

        tag.put("value", valueTag);

        return tag;
    }

    @Override
    protected Vector3d load(NbtCompound tag) {
        NbtCompound valueTag = tag.getCompound("value");

        set(
            valueTag.getDouble("x"),
            valueTag.getDouble("y"),
            valueTag.getDouble("z")
        );

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Vector3d, Vector3dSetting> {
        public double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
        public double sliderMin = 0, sliderMax = 10;
        public boolean onSliderRelease = false;
        public int decimalPlaces = 3;
        public boolean noSlider = false;

        public Builder() {
            super(new Vector3d());
        }

        public Builder defaultValue(double x, double y, double z) {
            this.defaultValue = new Vector3d(x, y, z);
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }

        public Builder sliderMin(double min) {
            this.sliderMin = min;
            return this;
        }

        public Builder sliderMax(double max) {
            this.sliderMax = max;
            return this;
        }

        public Builder sliderRange(double min, double max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
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

        @Override
        public Vector3dSetting build() {
            return new Vector3dSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, min, max, sliderMin, sliderMax, onSliderRelease, decimalPlaces, noSlider);
        }
    }
}
