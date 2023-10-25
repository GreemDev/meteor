/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.input

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedDropdown<T>(values: Array<out T>, value: T) : WDropdown<T>(values, value), RoundedWidget {
    override fun createRootWidget(): WDropdownRoot = WRoot()
    override fun createValueWidget(): WDropdownValue = WValue()

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val theme = theme()
        val pad = pad()
        val s = theme.textHeight()

        renderer.roundedBackground(this, pressed, mouseOver)

        val text = get().toString()
        val w = theme.textWidth(text)
        renderer.text(text, x + pad + maxValueWidth / 2 - w / 2, y + pad, theme.textColor(), false)
        renderer.rotatedQuad(x + pad + maxValueWidth + pad, y + pad, s, s, 0.0, GuiRenderer.TRIANGLE, theme.textColor())
    }

    private inner class WRoot : WDropdownRoot(), RoundedWidget {
        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            val theme = theme()
            val s = theme.scale(2.0)
            val c = theme.outlineColor()

            renderer.roundedBackground(this, pressed, mouseOver, false)

            renderer.quad(x, y + height - s, width, s, c)
            renderer.quad(x, y, s, height - s, c)
            renderer.quad(x + width - s, y, s, height - s, c)
        }
    }

    private inner class WValue : WDropdownValue(), RoundedWidget {
        override fun onCalculateSize() {
            val pad = pad()

            width = pad + theme.textWidth(value.toString()) + pad
            height = pad + theme.textHeight() + pad
        }

        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            val theme = theme()

            val color = theme.backgroundColor(pressed, mouseOver, true)
            val preA = color.a
            color.a /= 2
            color.validate()

            renderer.quad(this, color)

            color.a = preA

            val text = value.toString()
            renderer.text(text, x + width / 2 - theme.textWidth(text) / 2, y + pad(), theme.textColor(), false)
        }
    }
}
