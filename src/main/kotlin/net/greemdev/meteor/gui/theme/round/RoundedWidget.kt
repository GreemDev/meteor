/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.utils.BaseWidget
import meteordevelopment.meteorclient.gui.widgets.WWidget
import net.greemdev.meteor.invoke

interface RoundedWidget : BaseWidget {
    fun theme() = theme as RoundedTheme
    fun GuiRenderer.roundedBackground(widget: WWidget, pressed: Boolean, mouseOver: Boolean, outline: Boolean = true) {
        val t = theme()
        val s = t.scale(2.0)
        val outlineColor = t.outlineColor(pressed, mouseOver)
        roundRenderer2D.quad(widget.x + s, widget.y + s,
            widget.width - s * 2, widget.height - s * 2,
            t.backgroundColor(pressed, mouseOver), t.round() - s)
        if (outline)
            roundRenderer2D.widgetQuadOutline(widget, outlineColor, t.round(), s)
    }
}
