/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WQuad
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.invoke

class WRoundedQuad(color: Color) : WQuad(color), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        renderer.roundRenderer2D.quad(x, y, width, height, color, roundedTheme().round())
    }
}
