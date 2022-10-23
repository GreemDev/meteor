/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.util

import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.renderer.Mesh
import meteordevelopment.meteorclient.renderer.Renderer2D
import meteordevelopment.meteorclient.renderer.Renderer2D.*
import meteordevelopment.meteorclient.utils.render.color.Color
import org.lwjgl.system.MemoryUtil
import kotlin.math.*

const val circleNone = 0.0
const val circleQuarter = PI / 2
const val circleHalf = circleQuarter * 2
const val circle3Quarter = circleQuarter * 3

fun Renderer2D.rounded() = RoundedRenderer2D.of(this)

// Credit to meteor-rejects.
@Suppress("FunctionName") //internal names
class RoundedRenderer2D(val r2d: Renderer2D) {
    companion object {
        private val textured by lazy { RoundedRenderer2D(TEXTURE) }
        private val normal by lazy { RoundedRenderer2D(COLOR) }
        fun textured() = textured
        fun normal() = normal
        fun of(r2d: Renderer2D) = RoundedRenderer2D(r2d)
    }

    fun quad(x: Number, y: Number, width: Number, height: Number, color: Color, r: Number, roundTop: Boolean = true) =
        _quadRounded(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), color, r.toDouble(), roundTop)

    fun quadOutline(x: Number, y: Number, width: Number, height: Number, color: Color, r: Number, s: Number) =
        _quadRoundedOutline(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), color, r.toDouble(), s.toDouble())

    fun quadSide(x: Number, y: Number, width: Number, height: Number, color: Color, r: Number, right: Boolean) =
        _quadRoundedSide(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), color, r.toDouble(), right)

    fun circlePart(x: Number, y: Number, r: Number, startAngle: Number, angle: Number, color: Color) =
        _circlePart(x.toDouble(), y.toDouble(), r.toDouble(), startAngle.toDouble(), angle.toDouble(), color)

    fun circlePartOutline(x: Number, y: Number, r: Number, startAngle: Number, angle: Number, color: Color, outlineWidth: Number) =
        _circlePartOutline(x.toDouble(), y.toDouble(), r.toDouble(), startAngle.toDouble(), angle.toDouble(), color, outlineWidth.toDouble())

    fun widgetQuad(widget: WWidget, color: Color, round: Double, roundTop: Boolean = true) =
        quad(widget.x, widget.y, widget.width, widget.height, color, round, roundTop)

    fun widgetQuadOutline(widget: WWidget, color: Color, round: Double, s: Double) =
        quadOutline(widget.x, widget.y, widget.width, widget.height, color, round, s)

    fun widgetQuadSide(widget: WWidget, color: Color, round: Double, right: Boolean) =
        quadSide(widget.x, widget.y, widget.width, widget.height, color, round, right)


    private fun _quadRoundedOutline(x: Double, y: Double, width: Double, height: Double, color: Color, desiredR: Double, s: Double) {
        val r = getR(desiredR, width, height)
        if (r <= 0) {
            r2d.quad(x, y, width, s, color)
            r2d.quad(x, y + height - s, width, s, color)
            r2d.quad(x, y + s, s, height - s * 2, color)
            r2d.quad(x + width - s, y + s, s, height - s * 2, color)
        } else {
            //top
            circlePartOutline(x + r, y + r, r, circle3Quarter, circleQuarter, color, s)
            r2d.quad(x + r, y, width - r * 2, s, color)
            circlePartOutline(x + width - r, y + r, r, circleNone, circleQuarter, color, s)
            //middle
            r2d.quad(x, y + r, s, height - r * 2, color)
            r2d.quad(x + width - s, y + r, s, height - r * 2, color)
            //bottom
            circlePartOutline(x + width - r, y + height - r, r, circleQuarter, circleQuarter, color, s)
            r2d.quad(x + r, y + height - s, width - r * 2, s, color)
            circlePartOutline(x + r, y + height - r, r, circleHalf, circleQuarter, color, s)
        }
    }

    private fun _quadRounded(x: Double, y: Double, width: Double, height: Double, color: Color, desiredR: Double, roundTop: Boolean) {
        val r = getR(desiredR, width, height)
        if (r <= 0)
            r2d.quad(x, y, width, height, color)
        else {
            if (roundTop) {
                //top
                circlePart(x + r, y + r, r, circle3Quarter, circleQuarter, color)
                r2d.quad(x + r, y, width - 2 * r, r, color)
                circlePart(x + width - r, y + r, r, circleNone, circleQuarter, color)
                //middle
                r2d.quad(x, y + r, width, height - 2 * r, color)
            } else
                //middle
                r2d.quad(x, y, width, height - r, color)

            //bottom
            circlePart(x + width - r, y + height - r, r, circleQuarter, circleQuarter, color)
            r2d.quad(x + r, y + height - r, width - 2 * r, r, color)
            circlePart(x + r, y + height - r, r, circleHalf, circleQuarter, color)
        }
    }

    private fun _quadRoundedSide(x: Double, y: Double, width: Double, height: Double, color: Color, desiredR: Double, right: Boolean) {
        val r = getR(desiredR, width, height)
        if (r <= 0)
            r2d.quad(x, y, width, height, color)
        else {
            if (right) {
                circlePart(x + width - r, y + r, r, circleNone, circleQuarter, color)
                circlePart(x + width - r, y + height - r, r, circleQuarter, circleQuarter, color)
                r2d.quad(x, y, width - r, height, color)
                r2d.quad(x + width - r, y + r, r, height - r * 2, color)
            } else {
                circlePart(x + r, y + r, r, circle3Quarter, circleQuarter, color)
                circlePart(x + r, y + height - r, r, circleHalf, circleQuarter, color)
                r2d.quad(x + r, y, width - r, height, color)
                r2d.quad(x, y + r, r, height - r * 2, color)
            }
        }
    }

    private fun _circlePart(x: Double, y: Double, r: Double, startAngle: Double, angle: Double, color: Color) {
        val depth = circleDepth(r, angle)
        val cirPart = angle / depth
        val center = r2d.triangles.vec2(x, y).color(color).next()
        var prev = vecOnCircle(r2d, x, y, r, startAngle, color)
        repeat(depth + 1) {
            if (it == 0) return@repeat
            val next = vecOnCircle(r2d, x, y, r, startAngle + cirPart * it, color)
            r2d.triangles.quad(prev, center, next, next)
            prev = next
        }
    }

    private fun _circlePartOutline(x: Double, y: Double, r: Double, startAngle: Double, angle: Double, color: Color, outlineWidth: Double) {
        if (outlineWidth >= r)
            circlePart(x, y, r, startAngle, angle, color)
        else {
            val depth = circleDepth(r, angle)
            val cirPart = angle / depth
            var innerPrev = vecOnCircle(r2d, x, y, r - outlineWidth, startAngle, color)
            var outerPrev = vecOnCircle(r2d, x, y, r, startAngle, color)
            repeat(depth + 1) {
                if (it == 0) return@repeat
                val innerNext = vecOnCircle(r2d, x, y, r - outlineWidth, startAngle + cirPart * it, color)
                val outerNext = vecOnCircle(r2d, x, y, r, startAngle + cirPart * it, color)
                r2d.triangles.quad(innerNext, innerPrev, outerPrev, outerNext)
                innerPrev = innerNext
                outerPrev = outerNext
            }
        }

    }

    fun triangle(i1: Int, i2: Int, i3: Int, entity: Mesh) {
        val p = entity.indicesPointer + entity.indicesCount * 4
        MemoryUtil.memPutInt(p, i1)
        MemoryUtil.memPutInt(p + 4, i2)
        MemoryUtil.memPutInt(p + 8, i3)
        entity.indicesCount += 3
        entity.growIfNeeded()
    }

    fun circleDepth(r: Double, angle: Double) =
        (angle * r / circleQuarter).toInt().coerceAtLeast(1)

    private fun vecOnCircle(r2d: Renderer2D, x: Double, y: Double, r: Double, angle: Double, color: Color) =
        r2d.triangles.vec2(x + sin(angle) * r, y - cos(angle) * r).color(color).next()

    private fun getR(r: Double, w: Double, h: Double): Double {
        var temp = r
        if (r * 2 > h)
            temp = h / 2
        if (r * 2 > w)
            temp = w / 2
        return temp
    }
}
