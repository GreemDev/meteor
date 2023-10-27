/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WLabel
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.lineCount
import net.greemdev.meteor.util.meteor.colorCodeRegex
import net.greemdev.meteor.util.meteor.renderLegacy

open class WRoundedLabel(text: String?, title: Boolean) : WLabel(text, title), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        if (!text.isNullOrEmpty()) {
            val c = getEffectiveColor(theme)

            if (colorCodeRegex in text || text.lineCount() > 1)
                renderer.legacyText(text, x, y, c, title, false)
            else
                renderer.text(text, x, y, c, title)
        }
    }
}
