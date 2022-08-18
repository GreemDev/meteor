/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.util

import meteordevelopment.meteorclient.renderer.Renderer2D
import meteordevelopment.meteorclient.utils.render.color.Color
import kotlin.math.*

const val circleNone = 0.0
const val circleQuarter = PI / 2
const val circleHalf = circleQuarter * 2
const val circle3Quarter = circleQuarter * 3

/*
 * Credit to meteor-rejects.
 */
object RoundedRenderer2D {

    fun quadRoundedOutline(r2d: Renderer2D, x: Double, y: Double, width: Double, height: Double, color: Color, desiredR: Double, s: Double) {
        val r = getR(desiredR, width, height)
        if (r <= 0) {
            r2d.quad(x, y, width, s, color)
            r2d.quad(x, y + height - s, width, s, color)
            r2d.quad(x, y + s, s, height - s * 2, color)
            r2d.quad(x + width - s, y + s, s, height - s * 2, color)
        } else {
            //top
            circlePartOutline(r2d, x + r, y + r, r, circle3Quarter, circleQuarter, color, s)
            r2d.quad(x + r, y, width - r * 2, s, color)
            circlePartOutline(r2d, x + width - r, y + r, r, circleNone, circleQuarter, color, s)
            //middle
            r2d.quad(x, y + r, s, height - r * 2, color)
            r2d.quad(x + width - s, y + r, s, height - r * 2, color)
            //bottom
            circlePartOutline(r2d, x + width - r, y + height - r, r, circleQuarter, circleQuarter, color, s)
            r2d.quad(x + r, y + height - s, width - r * 2, s, color)
            circlePartOutline(r2d, x + r, y + height - r, r, circleHalf, circleQuarter, color, s)
        }
    }

    fun quadRounded(r2d: Renderer2D, x: Double, y: Double, width: Double, height: Double, color: Color, desiredR: Double, roundTop: Boolean) {
        val r = getR(desiredR, width, height)
        if (r <= 0)
            r2d.quad(x, y, width, height, color)
        else {
            if (roundTop) {
                //top
                circlePart(r2d, x + r, y + r, r, circle3Quarter, circleQuarter, color)
                r2d.quad(x + r, y, width - 2 * r, r, color)
                circlePart(r2d, x + width - r, y + r, r, circleNone, circleQuarter, color)
                //middle
                r2d.quad(x, y + r, width, height - 2 * r, color)
            } else
                //middle
                r2d.quad(x, y, width, height - r, color)

            //bottom
            circlePart(r2d, x + width - r, y + height - r, r, circleQuarter, circleQuarter, color)
            r2d.quad(x + r, y + height - r, width - 2 * r, r, color)
            circlePart(r2d, x + r, y + height - r, r, circleHalf, circleQuarter, color)
        }
    }

    fun quadRoundedSide(r2d: Renderer2D, x: Double, y: Double, width: Double, height: Double, color: Color, desiredR: Double, right: Boolean) {
        val r = getR(desiredR, width, height)
        if (r <= 0)
            r2d.quad(x, y, width, height, color)
        else {
            if (right) {
                circlePart(r2d, x + width - r, y + r, r, circleNone, circleQuarter, color)
                circlePart(r2d, x + width - r, y + height - r, r, circleQuarter, circleQuarter, color)
                r2d.quad(x, y, width - r, height, color)
                r2d.quad(x + width - r, y + r, r, height - r * 2, color)
            } else {
                circlePart(r2d, x + r, y + r, r, circle3Quarter, circleQuarter, color)
                circlePart(r2d, x + r, y + height - r, r, circleHalf, circleQuarter, color)
                r2d.quad(x + r, y, width - r, height, color)
                r2d.quad(x, y + r, r, height - r * 2, color)
            }
        }
    }

    fun circlePart(r2d: Renderer2D, x: Double, y: Double, r: Double, startAngle: Double, angle: Double, color: Color) {
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

    fun circlePartOutline(r2d: Renderer2D, x: Double, y: Double, r: Double, startAngle: Double, angle: Double, color: Color, outlineWidth: Double) {
        if (outlineWidth >= r)
            circlePart(r2d, x, y, r, startAngle, angle, color)
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

    fun circleDepth(r: Double, angle: Double) =
        max(1, (angle * r / circleQuarter).toInt())

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
