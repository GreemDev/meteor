/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.meteor.LegacyText;

public abstract class WLabel extends WPressable {
    public Color color;

    protected String text;
    protected boolean title;
    public boolean legacyColorCodes = false;

    public WLabel(String text, boolean title) {
        this.text = text;
        this.title = title;
    }

    @Override
    protected void onCalculateSize() {
        width = legacyColorCodes
            ? LegacyText.getWidth(theme.textRenderer(), text)
            : theme.textWidth(text, text.length(), title);

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
        if (useColorCodes())
            if (Math.round(LegacyText.getWidth(theme.textRenderer(), text)) != width)
                invalidate();
        else
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

    public boolean useColorCodes() {
        return legacyColorCodes && LegacyText.getColorCodeRegex().containsMatchIn(text);
    }
}
