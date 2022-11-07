/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.Util;
import net.greemdev.meteor.util.meteor.LegacyText;

public class WMeteorMultiLabel extends WMultiLabel implements MeteorWidget {
    public WMeteorMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color c = color != null
            ? color
            : title
                ? theme().titleTextColor.get()
                : theme().textColor.get();

        for (int i = 0; i < lines.size(); i++) {
            if (legacyColorCodes && LegacyText.getColorCodeRegex().containsMatchIn(lines.get(i))) {
                theme.textRenderer().begin(theme.scale(1));
                LegacyText.render(theme.textRenderer(), lines.get(i), x, y + h * i, Util.awt(c), false);
                theme.textRenderer().end();
            } else
                renderer.text(lines.get(i), x, y + h * i, c, false);
        }
    }
}
