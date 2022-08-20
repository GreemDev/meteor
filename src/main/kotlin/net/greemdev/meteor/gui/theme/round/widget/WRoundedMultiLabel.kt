/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedMultiLabel(text: String?, title: Boolean, maxWidth: Double)
    : WMultiLabel(text, title, maxWidth), RoundedWidget {

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val h = theme.textHeight(title)
        val color = theme().textColor()

        lines.forEachIndexed { i, text ->
            renderer.text(text, x, y + h * i, color, false)
        }
    }
}
