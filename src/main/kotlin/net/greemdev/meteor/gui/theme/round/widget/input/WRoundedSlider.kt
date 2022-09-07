/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.input

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.input.WSlider
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.meteor.invoke

class WRoundedSlider(value: Double, min: Double, max: Double) : WSlider(value, min, max), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val valueWidth = valueWidth()

        renderer.renderBar(valueWidth)
        renderer.renderHandle(valueWidth)
    }

    private fun GuiRenderer.renderBar(valueWidth: Double) {
        val theme = theme()

        val s = theme.scale(3.0)
        val handleSize = handleSize()

        val x = x + handleSize / 2
        val y = y + height / 2 - s / 2

        quad(x, y, valueWidth, s, theme.sliderLeft())
        quad(x + valueWidth, y, width - valueWidth - handleSize, s, theme.sliderRight())
    }

    private fun GuiRenderer.renderHandle(valueWidth: Double) {
        val theme = theme()
        val s = handleSize()

        quad(x + valueWidth, y, s, s, GuiRenderer.CIRCLE, theme.sliderHandle.get(dragging, handleMouseOver))
    }

}
