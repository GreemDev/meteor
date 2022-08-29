/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.util

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.utils.render.color.Color

fun RoundedRenderer2D.quad(widget: WWidget, color: Color, round: Double, roundTop: Boolean = true) =
    quad(widget.x, widget.y, widget.width, widget.height, color, round, roundTop)

fun RoundedRenderer2D.quadOutline(widget: WWidget, color: Color, round: Double, s: Double) =
    quadOutline(widget.x, widget.y, widget.width, widget.height, color, round, s)

fun RoundedRenderer2D.quadSide(widget: WWidget, color: Color, round: Double, right: Boolean) =
    quadSide(widget.x, widget.y, widget.width, widget.height, color, round, right)

fun GuiRenderer.circlePart(x: Double, y: Double, round: Double, startAngle: Double, angle: Double, color: Color) =
    RoundedRenderer2D.of(r2D()).circlePart(x, y, round, startAngle, angle, color)

fun GuiRenderer.circlePartOutline(x: Double, y: Double, round: Double, startAngle: Double, angle: Double, color: Color, outlineWidth: Double) =
    RoundedRenderer2D.of(r2D()).circlePartOutline(x, y, round, startAngle, angle, color, outlineWidth)
