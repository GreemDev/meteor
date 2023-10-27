/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.pressable

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedButton(text: String?, texture: GuiTexture?) : WButton(text, texture), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val theme = roundedTheme()
        val pad = pad()

        renderer.roundedBackground(this, pressed, mouseOver)

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, theme.textColor(), false)
        } else {
            val ts = theme.textHeight()
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, theme.textColor())
        }
    }
}
