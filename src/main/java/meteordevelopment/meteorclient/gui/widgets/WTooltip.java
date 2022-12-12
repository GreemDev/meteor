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
import net.greemdev.meteor.utils;
import net.minecraft.util.math.Direction;

import java.util.Comparator;

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
            : padded(tooltipLabelCell, theme.scale(
            Strings.widestLine(text, (l) ->
                LegacyText.getLegacyWidth(theme.textRenderer(), l, false)
            )
        ), true);
    }


    protected double adjustHeight() {
        return lineCount == 1
            ? height
            : padded(tooltipLabelCell, theme.scale(
            (lineCount * (theme.textRenderer().getHeight() + LegacyText.betweenLines)) - LegacyText.betweenLines
        ), false);
    }

    private double padded(Cell<?> cell, double value, boolean isWidth) {
        return isWidth
            ? cell.padLeft() + value + cell.padRight()
            : cell.padTop() + value + cell.padBottom();
    }
}
