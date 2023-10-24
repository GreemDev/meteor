/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.utils;
import net.greemdev.meteor.util.meteor.LegacyText;

public class WMeteorMultiLabel extends WMultiLabel implements MeteorWidget {
    public WMeteorMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color c = getEffectiveColor(theme);

        utils.indexedForEach(lines, (index, line) -> {
            if (LegacyText.needsSpecialRenderer(line))
                renderer.legacyText(line, x, y + h * index, c, title, false);
            else
                renderer.text(line, x, y + h * index, c, title);
        });
    }
}
