/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.utils.BaseWidget
import meteordevelopment.meteorclient.gui.widgets.WWidget
import net.greemdev.meteor.gui.theme.round.util.quadOutlineRounded
import net.greemdev.meteor.gui.theme.round.util.quadRounded

interface RoundedWidget : BaseWidget {
    fun theme() = theme as RoundedTheme
    fun renderBackground(renderer: GuiRenderer, widget: WWidget, pressed: Boolean, mouseOver: Boolean) {
        val t = theme()
        val r = t.round.get().toDouble()
        val s = t.scale(2.0)
        val outlineColor = t.outlineColor.get(pressed, mouseOver)
        renderer.quadRounded(widget.x, widget.y + s,
            widget.width - s * 2, widget.height - s * 2,
            t.backgroundColor.get(pressed, mouseOver), r - s)
        renderer.quadOutlineRounded(widget, outlineColor, r, s)
    }
}
