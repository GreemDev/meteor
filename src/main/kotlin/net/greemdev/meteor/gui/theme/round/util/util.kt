/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.util

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.utils.render.color.Color

fun GuiRenderer.quadRounded(x: Double, y: Double, width: Double, height: Double, color: Color, round: Double, roundTop: Boolean = true) =
    RoundedRenderer2D.quadRounded(r2D(), x, y, width, height, color, round, roundTop)

fun GuiRenderer.quadRounded(widget: WWidget, color: Color, round: Double, roundTop: Boolean = true) =
    quadRounded(widget.x, widget.y, widget.width, widget.height, color, round, roundTop)

fun GuiRenderer.quadOutlineRounded(x: Double, y: Double, width: Double, height: Double, color: Color, round: Double, s: Double) =
    RoundedRenderer2D.quadRoundedOutline(r2D(), x, y, width, height, color, round, s)

fun GuiRenderer.quadOutlineRounded(widget: WWidget, color: Color, round: Double, s: Double) =
    quadOutlineRounded(widget.x, widget.y, widget.width, widget.height, color, round, s)

fun GuiRenderer.quadRoundedSide(x: Double, y: Double, width: Double, height: Double, color: Color, round: Double, right: Boolean) =
    RoundedRenderer2D.quadRoundedSide(r2D(), x, y, width, height, color, round, right)

fun GuiRenderer.quadRoundedSide(widget: WWidget, color: Color, round: Double, right: Boolean) =
    quadRoundedSide(widget.x, widget.y, widget.width, widget.height, color, round, right)

fun GuiRenderer.circlePart(x: Double, y: Double, round: Double, startAngle: Double, angle: Double, color: Color) =
    RoundedRenderer2D.circlePart(r2D(), x, y, round, startAngle, angle, color)

fun GuiRenderer.circlePartOutline(x: Double, y: Double, round: Double, startAngle: Double, angle: Double, color: Color, outlineWidth: Double) =
    RoundedRenderer2D.circlePartOutline(r2D(), x, y, round, startAngle, angle, color, outlineWidth)
