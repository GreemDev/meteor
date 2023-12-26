/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Math")
package net.greemdev.meteor.util.math

import net.greemdev.meteor.Initializer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.util.math.random.Random
import org.jetbrains.annotations.Range
import org.joml.*
import java.lang.Math
import kotlin.math.pow

@get:JvmName("rand")
val minecraftRandom by lazy<Random>(Random::create)

infix fun MatrixStack.onTop(logic: Initializer<MatrixStack>) = modifyPushedCopy(this, logic)

fun MatrixStack.scale(scalar: Float) = scale(scalar, scalar, scalar)

fun modifyPushedCopy(matrices: MatrixStack, logic: Initializer<MatrixStack>) {
    matrices.push()
    matrices.logic()
    matrices.pop()
}

fun MatrixStack.Entry.getPositionAndNormalMatrix(): Pair<Matrix4f, Matrix3f> = positionMatrix to normalMatrix
fun MatrixStack.peekPositionAndNormalMatrix() = peek().getPositionAndNormalMatrix()

fun radiansToDegrees(angrad: Double) = Math.toDegrees(angrad)
fun degreesToRadians(angdeg: Double) = Math.toRadians(angdeg)

fun radiansToDegrees(angrad: Float) = radiansToDegrees(angrad.toDouble()).toFloat()
fun degreesToRadians(angdeg: Float) = degreesToRadians(angdeg.toDouble()).toFloat()

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

val Number.isZero
    get() = toFloat().let { it == 0f || it == -0f }

fun Number.power(of: Number) = toDouble().pow(of.toDouble())

@get:JvmName("hasDecimal")
val Number.hasDecimal: Boolean
    get() = when {
        this is Float || this is Double -> toString().contains('.')
        else -> false
    }

/**
 * Receiver is the `delta` (first argument of [MathHelper.lerp]).
 */
fun Number.lerp(start: Number, end: Number) = MathHelper.lerp(toDouble(), start.toDouble(), end.toDouble())

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

// Vectors

fun Vec3d.apply(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) =
    Vec3d(this.x + x, this.y + y, this.z + z)
fun Vec3d.edit(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) = Vec3d(
    if (x == 0.0) this.x else x,
    if (y == 0.0) this.y else y,
    if (z == 0.0) this.z else z
)

fun Vec3i.apply(x: Int = 0, y: Int = 0, z: Int = 0) =
    Vec3i(this.x + x, this.y + y, this.z + z)
fun Vec3i.edit(x: Int = 0, y: Int = 0, z: Int = 0) = Vec3i(
    if (x == 0) this.x else x,
    if (y == 0) this.y else y,
    if (z == 0) this.z else z
)

fun Vector3d.apply(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) =
    Vector3d(this.x + x, this.y + y, this.z + z)
fun Vector3d.edit(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) = Vector3d(
    if (x == 0.0) this.x else x,
    if (y == 0.0) this.y else y,
    if (z == 0.0) this.z else z
)

fun Vector3i.apply(x: Int = 0, y: Int = 0, z: Int = 0) =
    Vector3i(this.x + x, this.y + y, this.z + z)
fun Vector3i.edit(x: Int = 0, y: Int = 0, z: Int = 0) = Vector3i(
    if (x == 0) this.x else x,
    if (y == 0) this.y else y,
    if (z == 0) this.z else z
)

fun Vector4d.apply(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 0.0) =
    Vector4d(this.x + x, this.y + y, this.z + z, this.w + w)
fun Vector4d.edit(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 0.0) = Vector4d(
    if (x == 0.0) this.x else x,
    if (y == 0.0) this.y else y,
    if (z == 0.0) this.z else z,
    if (w == 0.0) this.w else w
)

fun Vector4i.apply(x: Int = 0, y: Int = 0, z: Int = 0, w: Int = 0) =
    Vector4i(this.x + x, this.y + y, this.z + z, this.w + w)
fun Vector4i.edit(x: Int = 0, y: Int = 0, z: Int = 0, w: Int = 0) = Vector4i(
    if (x == 0) this.x else x,
    if (y == 0) this.y else y,
    if (z == 0) this.z else z,
    if (w == 0) this.w else w
)

fun Vector4f.apply(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f) =
    Vector4f(this.x + x, this.y + y, this.z + z, this.w + w)
fun Vector4f.edit(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f) = Vector4f(
    if (x == 0f) this.x else x,
    if (y == 0f) this.y else y,
    if (z == 0f) this.z else z,
    if (w == 0f) this.w else w
)
