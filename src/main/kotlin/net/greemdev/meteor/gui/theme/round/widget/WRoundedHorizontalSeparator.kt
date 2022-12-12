/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.meteor.invoke
import kotlin.math.round

class WRoundedHorizontalSeparator(text: String?) : WHorizontalSeparator(text), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val theme = theme()
        if (text == null) { //render without text
            val s = theme.scalar()
            val w = width / 2

            renderer.quad(x, y + s, w, s, theme.separatorEdges(), theme.separatorCenter())
            renderer.quad(x + w, y + s, w, s, theme.separatorCenter(), theme.separatorEdges())
        } else { //render with text
            val s = theme.scale(2.0)
            val h = theme.scalar()

            val textStart = round(width / 2 - textWidth / 2 - s)
            val textEnd = s + textStart + textWidth + s

            val offsetY = round(height / 2)

            renderer.quad(x, y + offsetY, textStart, h, theme.separatorEdges(), theme.separatorCenter())
            renderer.text(text, x + textStart + s, y, theme.separatorText(), false)
            renderer.quad(x + textEnd, y + offsetY, width - textEnd, h, theme.separatorCenter(), theme.separatorEdges())
        }
    }
}
