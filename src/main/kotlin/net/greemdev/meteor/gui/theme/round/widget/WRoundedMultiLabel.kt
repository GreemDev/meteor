/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.awt
import net.greemdev.meteor.util.meteor.colorCodeRegex
import net.greemdev.meteor.util.meteor.renderLegacy

class WRoundedMultiLabel(text: String?, title: Boolean, maxWidth: Double)
    : WMultiLabel(text, title, maxWidth), RoundedWidget {

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val h = theme.textHeight(title)
        val defaultColor = theme().textColor()

        lines.forEachIndexed { i, line ->
            if (legacyColorCodes && colorCodeRegex in line) {
                theme.textRenderer().begin(theme.scale(1.0))
                theme.textRenderer().renderLegacy(line, x, y + h * i, (color ?: defaultColor).awt())
                theme.textRenderer().end()
            } else
                renderer.text(line, x, y + h * i, color ?: defaultColor, false)
        }
    }
}
