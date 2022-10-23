/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import net.greemdev.meteor.util.Util;
import net.greemdev.meteor.util.meteor.LegacyText;

public class WMeteorLabel extends WLabel implements MeteorWidget {
    public WMeteorLabel(String text, boolean title) {
        super(text, title);
    }
    public WMeteorLabel(String text, boolean title, boolean colorCodes) {
        super(text, title, colorCodes);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty())
            if (useColorCodes()) {
                theme().textRenderer().begin();
                LegacyText.render(theme().textRenderer(), text, x, y, Util.awt(
                    color != null
                        ? color
                        : title
                            ? theme().titleTextColor.get()
                            : theme().textColor.get()),
                    false);
                theme().textRenderer().end();
            } else
                renderer.text(text, x, y, color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get()), title);
    }
}
