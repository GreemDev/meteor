/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.meteor.LegacyText;

import java.util.function.Supplier;

public abstract class WLabel extends WPressable implements Supplier<String> {
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
            ? LegacyText.getLegacyWidth(theme.textRenderer(), text)
            : theme.textWidth(text);
        height = theme.textHeight(title);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (action != null) return super.onMouseClicked(mouseX, mouseY, button, used);
        return false;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (action != null) return super.onMouseReleased(mouseX, mouseY, button);
        return false;
    }


    public void set(String text) {
        if (LegacyText.needsSpecialRenderer(text))
            if (Math.round(LegacyText.getLegacyWidth(theme.textRenderer(), text)) != width)
                invalidate();
        else
            if (Math.round(theme.textWidth(text, text.length(), title)) != width)
                invalidate();

        this.text = text;
    }

    @Override
    public String get() {
        return text;
    }

    public WLabel color(Color color) {
        this.color = color;
        return this;
    }

    public WLabel prepend(String text) {
        this.text = text + this.text;
        return this;
    }

    public WLabel append(String text) {
        this.text = this.text + text;
        return this;
    }
}
