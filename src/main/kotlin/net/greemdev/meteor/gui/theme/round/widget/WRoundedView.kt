/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.containers.WView
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedView : WView(), RoundedWidget {
    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        if (canScroll and hasScrollBar)
            renderer.quad(handleX(), handleY(), handleWidth(), handleHeight(), roundedTheme().scrollbarColor(handlePressed, handleMouseOver))
    }
}
