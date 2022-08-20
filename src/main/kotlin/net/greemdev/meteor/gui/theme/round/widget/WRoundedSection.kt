/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.gui.widgets.containers.WSection
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedSection(title: String, expanded: Boolean, headerWidget: WWidget?)
    : WSection(title, expanded, headerWidget) {
    override fun createHeader(): WHeader = WRoundedHeader(title)

    private inner class WRoundedHeader(title: String) : WHeader(title) {
        private lateinit var triangle: WTriangle
        override fun init() {
            add(theme.horizontalSeparator(title)).expandX()

            if (headerWidget != null) add(headerWidget)

            triangle = WHeaderTriangle()
            triangle.theme = theme
            triangle.action = Runnable(this::onClick)

            add(triangle)
        }

        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            triangle.rotation = (1 - animProgress) * -90
        }
    }

    private class WHeaderTriangle : WTriangle(), RoundedWidget {
        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            renderer.rotatedQuad(x, y, width, height, rotation, GuiRenderer.TRIANGLE, theme().textColor())
        }
    }
}
