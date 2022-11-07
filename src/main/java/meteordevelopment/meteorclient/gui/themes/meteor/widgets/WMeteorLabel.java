/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.Util;
import net.greemdev.meteor.util.meteor.LegacyText;

public class WMeteorLabel extends WLabel implements MeteorWidget {
    public WMeteorLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Color c = color != null
            ? color
            : title
                ? theme().titleTextColor.get()
                : theme().textColor.get();

        if (text != null && !text.isEmpty())
            if (useColorCodes()) {
                theme().textRenderer().begin(theme.scale(1));
                LegacyText.render(theme().textRenderer(), text, x, y, Util.awt(c), false);
                theme().textRenderer().end();
            } else
                renderer.text(text, x, y, c, title);
    }
}
