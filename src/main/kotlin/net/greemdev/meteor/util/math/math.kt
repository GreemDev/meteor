/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.math

import net.greemdev.meteor.Initializer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import org.jetbrains.annotations.Range
import kotlin.math.pow

val minecraftRandom: Random by lazy { Random.create() }

fun Number.clamp(min: Number, max: Number): Double =
    MathHelper.clamp(toDouble(), min.toDouble(), max.toDouble())

infix fun MatrixStack.onTop(logic: Initializer<MatrixStack>) = modifyPushedCopy(this, logic)

fun MatrixStack.scale(scalar: Float) = scale(scalar, scalar, scalar)

fun modifyPushedCopy(matrices: MatrixStack, logic: Initializer<MatrixStack>) {
    matrices.push()
    matrices.logic()
    matrices.pop()
}

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

val Number.isZero
    get() = toFloat().let { it == 0f || it == -0f }

fun Number.power(of: Number) = toDouble().pow(of.toDouble())

fun Vec3d.edit(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) = Vec3d(this.x + x, this.y + y, this.z + z)
fun Vec3d.change(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) = Vec3d(
    if (x == 0.0) this.x else x,
    if (y == 0.0) this.y else y,
    if (z == 0.0) this.z else z
)

/**
 * Receiver is the `delta` (first argument of [MathHelper.lerp]).
 */
fun Number.lerp(start: Number, end: Number) = MathHelper.lerp(this.toDouble(), start.toDouble(), end.toDouble())

fun Random.nextGaussian(mean: Float, deviation: Float) = MathHelper.nextGaussian(this, mean, deviation)

fun Float.precision(decimalPlaces: @Range(from = 1, to = 10) Int) =
    precision(decimalPlaces.coerceIn(1, 10), this).toFloat()

fun Double.precision(decimalPlaces: @Range(from = 1, to = 10) Int) =
    precision(decimalPlaces.coerceIn(1, 10), this).toDouble()

fun precision(decimalPlaces: Int, number: Number): String {
    val parts = number.toString().split('.')
    val decimal = parts.elementAtOrNull(1)?.take(decimalPlaces) ?: "0".repeat(decimalPlaces)
    return "${parts.first()}.$decimal"
}
