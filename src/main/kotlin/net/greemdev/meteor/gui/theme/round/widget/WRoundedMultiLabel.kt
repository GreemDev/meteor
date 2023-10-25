/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.lineCount
import net.greemdev.meteor.util.meteor.colorCodeRegex
import net.greemdev.meteor.util.meteor.needsSpecialRenderer
import net.greemdev.meteor.util.meteor.renderLegacy

class WRoundedMultiLabel(text: String?, title: Boolean, maxWidth: Double)
    : WMultiLabel(text, title, maxWidth), RoundedWidget {

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val h = theme.textHeight(title)
        val c = getEffectiveColor(theme)

        lines.forEachIndexed { i, line ->
            if (needsSpecialRenderer(line))
                renderer.legacyText(line, x, y + h * i, c, title, false)
            else
                renderer.text(line, x, y + h * i, c, title)
        }
    }
}
