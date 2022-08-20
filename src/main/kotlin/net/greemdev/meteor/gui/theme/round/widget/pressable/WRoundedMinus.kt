/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.pressable

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedMinus : WMinus(), RoundedWidget {

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val pad = pad()
        val s = theme.scale(3.0)

        renderBackground(renderer, this, pressed, mouseOver)
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme().minusColor.get())
    }
}
