/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WLabel
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.invoke

open class WRoundedLabel(text: String?, title: Boolean) : WLabel(text, title), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        if (!text.isNullOrEmpty()) {
            val color = if (color != null)
                color
            else if (title)
                theme().titleTextColor()
            else
                theme().textColor()

            renderer.text(text, x, y, color, title)
        }
    }
}
