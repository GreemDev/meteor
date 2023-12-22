/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.greemdev.meteor.modules.flightHud

import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.AwtColor
import net.greemdev.meteor.MeteorColor
import net.greemdev.meteor.meteor
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.VertexConsumer
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector4f
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun line(
    startX: Number,
    startY: Number,
    endX: Number,
    endY: Number,
    startR: Float = 1f,
    startG: Float = 1f,
    startB: Float = 1f,
    startA: Float = 1f,
    endR: Float = 1f,
    endG: Float = 1f,
    endB: Float = 1f,
    endA: Float = 1f
) = HudLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), startR, startG, startB, startA, endR, endG, endB, endA)

class HudLine(
    var startX: Float,
    var startY: Float,
    var endX: Float,
    var endY: Float,
    var startR: Float,
    var startG: Float,
    var startB: Float,
    var startA: Float,
    var endR: Float,
    var endG: Float,
    var endB: Float,
    var endA: Float
) {
    fun colorStart(r: Float, g: Float, b: Float, a: Float = this.startA): HudLine {
        this.startR = r
        this.startG = g
        this.startB = b
        return alphaStart(a)
    }

    fun colorEnd(r: Float, g: Float, b: Float, a: Float = this.endA): HudLine {
        this.endR = r
        this.endG = g
        this.endB = b
        return alphaEnd(a)
    }

    fun colorStart(vec: Vector4f) = colorStart(vec.x, vec.y, vec.z, vec.w)
    fun colorStart(color: MeteorColor) = colorStart(color.toVector4f())
    fun colorStart(color: AwtColor) = colorStart(color.meteor())
    fun colorEnd(vec: Vector4f) = colorEnd(vec.x, vec.y, vec.z, vec.w)
    fun colorEnd(color: MeteorColor) = colorEnd(color.toVector4f())
    fun colorEnd(color: AwtColor) = colorEnd(color.meteor())

    fun color(r: Float, g: Float, b: Float, a: Float? = null) = colorStart(r, g, b, a ?: this.startA).colorEnd(r, g, b, a ?: this.endA)
    fun color(v: Vector4f) = colorStart(v).colorEnd(v)
    fun color(color: MeteorColor) = colorStart(color).colorEnd(color)
    fun color(color: AwtColor) = colorStart(color.meteor()).colorEnd(color.meteor())


    fun alpha(a: Float): HudLine {
        this.startA = a
        return this
    }

    fun alphaStart(a: Float): HudLine {
        this.startA = a
        return this
    }

    fun alphaEnd(a: Float): HudLine {
        this.endA = a
        return this
    }

    fun draw(ctx: DrawContext) {
        color(ctx.lineColor)
        var normalX = endX - startX
        var normalY = endY - startY

        val length = sqrt(normalX.pow(2) + normalY.pow(2)).also {
            if (abs(it) < 0.0001) return
        }

        normalX /= length
        normalY /= length

        ctx.bufferBuilder {
            vertex(ctx.positionMatrix, startX, startY, -90f).
            normal(ctx.normalMatrix, normalX, normalY, 0f).
            color(startR, startG, startB, startA)
        }
        ctx.bufferBuilder {
            vertex(ctx.positionMatrix, endX, endY, -90f).
            normal(ctx.normalMatrix, normalX, normalY, 0f).
            color(endR, endG, endB, endA)
        }
    }

    fun draw(bufferBuilder: BufferBuilder, positionMatrix: Matrix4f, normalMatrix: Matrix3f, lineColor: Color) =
        draw(DrawContext(bufferBuilder, positionMatrix, normalMatrix, lineColor))

    companion object {
        fun drawer(bufferBuilder: BufferBuilder, positionMatrix: Matrix4f, normalMatrix: Matrix3f, lineColor: Color) =
            DrawContext(bufferBuilder, positionMatrix, normalMatrix, lineColor)
    }

    class DrawContext(val bufferBuilder: BufferBuilder, val positionMatrix: Matrix4f, val normalMatrix: Matrix3f, val lineColor: Color) {
        operator fun invoke(
            startX: Float,
            startY: Float,
            endX: Float,
            endY: Float,
            startR: Float = 1f,
            startG: Float = 1f,
            startB: Float = 1f,
            startA: Float = 1f,
            endR: Float = 1f,
            endG: Float = 1f,
            endB: Float = 1f,
            endA: Float = 1f
        ) = this(line(startX, startY, endX, endY, startR, startG, startB, startA, endR, endG, endB, endA))

        operator fun invoke(hudLine: HudLine) = hudLine.draw(bufferBuilder, positionMatrix, normalMatrix, lineColor)
    }
}

operator fun BufferBuilder.invoke(vertex: BufferBuilder.() -> VertexConsumer) {
    vertex().next()
}
