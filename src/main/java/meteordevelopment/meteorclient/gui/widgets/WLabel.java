/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.Strings;
import net.greemdev.meteor.util.meteor.LegacyText;

public abstract class WLabel extends WPressable {
    public Color color;

    protected String text;
    protected boolean title;

    public WLabel(String text, boolean title) {
        this.text = text;
        this.title = title;
    }

    public Color getEffectiveColor(GuiTheme theme) {
        return color != null
            ? color
            : title
                ? theme.titleTextColor()
                : theme.textColor();
    }

    @Override
    protected void onCalculateSize() {
        width = LegacyText.getColorCodeRegex().containsMatchIn(text)
            ? LegacyText.getLegacyWidth(theme.textRenderer(), text, false)
            : theme.textWidth(text);

        height = theme.textHeight();
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        return action != null && super.onMouseClicked(mouseX, mouseY, button, used);
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return action != null && super.onMouseReleased(mouseX, mouseY, button);
    }

    public void set(String text) {
        if (LegacyText.isApplicableTo(text)) {
            if (Math.round(LegacyText.getLegacyWidth(theme.textRenderer(), text, false)) != width)
                invalidate();
        } else
            if (Math.round(theme.textWidth(text, text.length(), title)) != width)
                invalidate();

        this.text = text;
    }

    public String get() {
        return text;
    }

    public WLabel color(Color color) {
        this.color = color;
        return this;
    }
}
