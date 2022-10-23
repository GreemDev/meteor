/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WLabel
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.awt
import net.greemdev.meteor.util.meteor.invoke
import net.greemdev.meteor.util.meteor.legacyRender

open class WRoundedLabel(text: String?, title: Boolean, colorCodes: Boolean = false) : WLabel(text, title, colorCodes), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        if (!text.isNullOrEmpty()) {
            val color = if (color != null)
                color
            else if (title)
                theme().titleTextColor()
            else
                theme().textColor()

            if (useColorCodes()) {
                renderer.post {
                    theme.textRenderer().begin()
                    theme.textRenderer().legacyRender(text, x, y, color.awt())
                    theme.textRenderer().end()
                }
            } else
                renderer.text(text, x, y, color, title)
        }
    }
}
