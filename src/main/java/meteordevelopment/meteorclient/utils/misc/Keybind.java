/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import net.minecraft.nbt.NbtCompound;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class Keybind implements ISerializable<Keybind>, ICopyable<Keybind> {
    private boolean isKey;
    private int value;
    public boolean onRelease = false;

    private Keybind(boolean isKey, int value) {
        set(isKey, value);
    }

    public static Keybind none() {
        return new Keybind(true, -1);
    }

    public static Keybind fromKey(int key) {
        return new Keybind(true, key);
    }

    public static Keybind fromButton(int button) {
        return new Keybind(false, button);
    }

    public int getValue() {
        return value;
    }

    public boolean isSet() {
        return value != -1;
    }

    public boolean canBindTo(boolean isKey, int value) {
        if (isKey) return value != GLFW_KEY_ESCAPE;
        return value != GLFW_MOUSE_BUTTON_LEFT && value != GLFW_MOUSE_BUTTON_RIGHT;
    }

    public void set(boolean isKey, int value, boolean onRelease) {
        set(isKey, value);
        this.onRelease = onRelease;
    }

    public void set(boolean isKey, int value) {
        this.isKey = isKey;
        this.value = value;
    }

    @Override
    public Keybind set(Keybind value) {
        set(value.isKey, value.value, value.onRelease);
        return this;
    }

    public void unset() {
        set(Keybind.none());
    }

    public boolean matches(KeyEvent event) {
        return ((event.action == KeyAction.Release && onRelease) || (event.action == KeyAction.Press && !onRelease)) && matches(true, event.key);
    }

    public boolean matches(MouseButtonEvent event) {
        return ((event.action == KeyAction.Release && onRelease) || (event.action == KeyAction.Press && !onRelease)) && matches(false, event.button);
    }

    public boolean matches(boolean isKey, int value) {
        if (this.isKey != isKey) return false;
        return this.value == value;
    }

    public boolean isKey() {
        return isKey;
    }

    public boolean isPressed() {
        return isKey ? Input.isKeyPressed(value) : Input.isButtonPressed(value);
    }

    @Override
    public Keybind copy() {
        return new Keybind(isKey, value);
    }

    @Override
    public String toString() {
        if (value == -1) return "None";
        return isKey ? Utils.getKeyName(value) : Utils.getButtonName(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keybind keybind = (Keybind) o;
        return isKey == keybind.isKey && value == keybind.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isKey, value);
    }

    // Serialization

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putBoolean("isKey", isKey);
        tag.putInt("value", value);
        tag.putBoolean("onRelease", onRelease);

        return tag;
    }

    @Override
    public Keybind fromTag(NbtCompound tag) {
        isKey = tag.getBoolean("isKey");
        value = tag.getInt("value");
        if (tag.contains("onRelease"))
            onRelease = tag.getBoolean("onRelease");

        return this;
    }
}
