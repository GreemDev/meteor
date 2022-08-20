/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.gui.theme.round.util.quadRounded
import net.greemdev.meteor.util.*

class WRoundedWindow(icon: WWidget?, title: String?) : WWindow(icon, title), RoundedWidget {
    override fun header(icon: WWidget?): WHeader = WRoundedHeader(icon)

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        if (expanded || animProgress > 0)
            renderer.quadRounded(x, y + header.height / 2,
                width, height - header.height / 2,
                theme().backgroundColor(), theme().round(),
                false)
    }

    private inner class WRoundedHeader(icon: WWidget?) : WHeader(icon) {
        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            renderer.quadRounded(this, theme().accentColor(), theme().round())
        }
    }

}
