/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.utils.Utils
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import kotlin.math.abs
import kotlin.math.pow

val minecraftRandom: Random by lazy { Random.create() }

fun Number.clamp(min: Number, max: Number): Double =
    Utils.clamp(toDouble(), min.toDouble(), max.toDouble())

infix fun MatrixStack.onTop(logic: MatrixStack.() -> Unit) {
    push()
    logic()
    pop()
}

fun modifyTopCopy(matrices: MatrixStack, logic: MatrixStack.() -> Unit) = matrices.onTop(logic)

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

val Number.isZero
    get() = abs(toDouble()) == 0.0

fun Number.power(of: Number) = toDouble().pow(of.toDouble())

fun lerp(delta: Number, start: Number, end: Number) = MathHelper.lerp(delta.toDouble(), start.toDouble(), end.toDouble())

fun Random.nextGaussian(mean: Float, deviation: Float) = MathHelper.nextGaussian(this, mean, deviation)
