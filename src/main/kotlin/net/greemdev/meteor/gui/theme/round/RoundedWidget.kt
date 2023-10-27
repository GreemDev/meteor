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
    fun roundedTheme() = theme as RoundedTheme

    fun roundness(): Double = roundedTheme().round()

    fun GuiRenderer.roundedBackground(widget: WWidget, pressed: Boolean, mouseOver: Boolean, outline: Boolean = true, bypassDisableHover: Boolean = false) {
        val t = roundedTheme()
        val s = t.scale(2.0)

        roundRenderer2D.quad(widget.x + s, widget.y + s,
            widget.width - s * 2, widget.height - s * 2,
            t.backgroundColor(pressed, mouseOver, bypassDisableHover), t.round() - s)

        if (outline)
            roundRenderer2D.widgetQuadOutline(widget, t.outlineColor(pressed, mouseOver, bypassDisableHover), t.round(), s)
    }
}
