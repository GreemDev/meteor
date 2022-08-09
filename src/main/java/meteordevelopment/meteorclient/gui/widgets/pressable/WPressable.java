/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets.pressable;

import kotlin.Unit;
import kotlin.reflect.KCallable;
import kotlin.reflect.KFunction;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WPressable extends WWidget {
    public Runnable action;

    protected boolean pressed;

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (mouseOver && (button == GLFW_MOUSE_BUTTON_LEFT || button == GLFW_MOUSE_BUTTON_RIGHT) && !used) pressed = true;
        return pressed;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (pressed) {
            onPressed(button);
            if (action != null) action.run();

            pressed = false;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends WWidget> T action(Runnable action) {
        this.action = action;
        return (T)this;
    }

    protected void onPressed(int button) {}
}
