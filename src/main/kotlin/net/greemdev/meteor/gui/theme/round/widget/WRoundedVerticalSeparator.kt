/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.*
import kotlin.math.round

class WRoundedVerticalSeparator : WVerticalSeparator(), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val theme = theme()
        val colorEdges = theme.separatorEdges()
        val colorCenter = theme.separatorCenter()

        val s = theme.scale(1.0)
        val offsetX = round(width / 2)

        renderer.quad(x + offsetX, y, s, height / 2, colorEdges, colorEdges, colorCenter, colorCenter)
        renderer.quad(x + offsetX, y + height / 2, s, height / 2, colorCenter, colorCenter, colorEdges, colorEdges)
    }
}
