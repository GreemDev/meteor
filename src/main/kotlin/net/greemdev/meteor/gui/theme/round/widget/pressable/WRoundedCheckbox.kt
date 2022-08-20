/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.pressable

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.gui.theme.round.util.quadRounded
import net.greemdev.meteor.util.clamp

class WRoundedCheckbox(checked: Boolean) : WCheckbox(checked), RoundedWidget {

    private var animProgress: Double

    init {
        animProgress = if (checked) 1.0 else 0.0
    }

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val theme = theme()

        animProgress += (if (checked) 1 else -1) * delta * 14
        animProgress = animProgress.clamp(0, 1)

        renderBackground(renderer, this, pressed, mouseOver)

        if (animProgress > 0.0) {
            val cs = (width - theme.scale(2.0)) / 1.75 * animProgress
            renderer.quadRounded(x + (width - cs) / 2, y + (height - cs) / 2, cs, cs, theme.checkboxColor.get(), theme.round.get())
        }
    }
}
