/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.meteor.MetaKt;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public abstract class WTopBar extends WHorizontalList {

    public static boolean NEEDS_REFRESH = false;

    protected abstract Color getButtonColor(boolean pressed, boolean hovered);

    protected abstract Color getNameColor();

    public WTopBar() {
        spacing = 0;
    }

    @Override
    public void init() {
        clear();
        var tabs = MetaKt.renderOrder(Tabs.get());

        tabs.getFirst().forEach(t -> add(new WTopBarButton(t)));

        tabs.getSecond().forEach(t -> add(new WTopBarButton(t)));
    }

    protected class WTopBarButton extends WPressable {
        private final Tab tab;

        public WTopBarButton(Tab tab) {
            this.tab = tab;
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = tab.displayIcon.get()
                ? pad + theme.textHeight() + pad
                : pad + theme.textWidth(tab.name) + pad;

            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            if (!(mc.currentScreen instanceof TabScreen ts) || !ts.tab.equals(tab)) {
                tab.openScreen(theme);
                glfwSetCursorPos(mc.getWindow().getHandle(), mc.mouse.getX(), mc.mouse.getY());
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            Color color = getButtonColor(pressed || (mc.currentScreen instanceof TabScreen ts && ts.tab.equals(tab)), mouseOver);

            renderer.quad(x, y, width, height, color);

            if (tab.displayIcon.get()) {
                renderer.quad(
                    x + pad,
                    y + pad,
                    theme.textHeight(),
                    theme.textHeight(),
                    Objects.requireNonNull(tab.icon), //tab icon will always be present when displayIcon has even a possibility of being true due to design of Tab constructors
                    getNameColor()
                );
            }
            else
                renderer.text(tab.name, x + pad, y + pad, getNameColor(), false);
        }
    }
}
