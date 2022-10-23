/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.systems.waypoints.Waypoint

class WWaypointIcon(private val waypoint: Waypoint) : WWidget() {
    override fun onCalculateSize() {
        val s = theme.scale(32.0)
        width = s
        height = s
    }

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        renderer.post { waypoint.renderIcon(x, y, 1.0, width) }
    }
}
