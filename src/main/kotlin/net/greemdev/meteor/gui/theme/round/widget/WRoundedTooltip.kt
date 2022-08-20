/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WTooltip
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedTooltip(text: String) : WTooltip(text), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        renderer.quad(this, theme().backgroundColor.get())
    }
}
