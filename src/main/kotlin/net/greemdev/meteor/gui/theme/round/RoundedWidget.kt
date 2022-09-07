/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.utils.BaseWidget
import meteordevelopment.meteorclient.gui.widgets.WWidget
import net.greemdev.meteor.gui.theme.round.util.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*

interface RoundedWidget : BaseWidget {
    fun theme() = theme as RoundedTheme
    fun renderBackground(renderer: GuiRenderer, widget: WWidget, pressed: Boolean, mouseOver: Boolean) {
        val t = theme()
        val s = t.scale(2.0)
        val outlineColor = t.outlineColor.get(pressed, mouseOver)
        val r = renderer.r2D().rounded()
        r.quad(widget.x, widget.y + s,
            widget.width - s * 2, widget.height - s * 2,
            t.backgroundColor.get(pressed, mouseOver), t.round() - s)
        r.widgetQuadOutline(widget, outlineColor, t.round(), s)
    }
}
