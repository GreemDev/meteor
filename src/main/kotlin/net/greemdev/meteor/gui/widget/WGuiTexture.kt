/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture
import meteordevelopment.meteorclient.gui.widgets.WWidget
import net.greemdev.meteor.MeteorColor

class WGuiTexture @JvmOverloads constructor(val tex: GuiTexture, val color: MeteorColor, w: Double? = null, h: Double? = null) : WWidget() {
    val w by lazy { w ?: theme.textHeight() }
    val h by lazy { h ?: theme.textHeight() }

    override fun onCalculateSize() {
        pad().also {
            width = it + theme.scale(w) + it
            height = it + theme.scale(h) + it
        }
    }

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        pad().also {
            renderer.quad(x + it, y + it, theme.scale(w), theme.scale(h), tex, color)
        }
    }
}
