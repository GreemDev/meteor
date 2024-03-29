/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import net.greemdev.meteor.util.Strings;
import net.greemdev.meteor.util.meteor.LegacyText;

public abstract class WTooltip extends WContainer implements WRoot {
    private boolean valid;

    protected String text;
    protected int lineCount;

    private Cell<WLabel> tooltipLabelCell;

    public WTooltip(String text) {
        this.text = text;
        this.lineCount = Strings.lineCount(text);
    }

    @Override
    public void init() {
        tooltipLabelCell = add(theme.label(text)).pad(4);
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!valid) {
            calculateSize();
            calculateWidgetPositions();

            valid = true;
        }

        return super.render(renderer, mouseX, mouseY, delta);
    }

    protected double adjustWidth() {
        return lineCount == 1
            ? width
            : padded(theme.scale(Strings.widestLine(text, (l) -> LegacyText.getLegacyWidth(theme.textRenderer(), l))), true);
    }


    protected double adjustHeight() {
        return lineCount == 1
            ? height
            : padded(
                theme.scale((lineCount * (theme.textRenderer().getHeight() + LegacyText.betweenLines)) - LegacyText.betweenLines),
            false);
    }

    private double padded(double value, boolean isWidth) {
        return isWidth
            ? tooltipLabelCell.padLeft() + value + tooltipLabelCell.padRight()
            : tooltipLabelCell.padTop() + value + tooltipLabelCell.padBottom();
    }
}
