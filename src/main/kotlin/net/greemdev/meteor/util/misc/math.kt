/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.utils.Utils
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.pow

fun Number.clamp(min: Number, max: Number): Double =
    Utils.clamp(this.toDouble(), min.toDouble(), max.toDouble())

infix fun MatrixStack.onTop(logic: MatrixStack.() -> Unit) {
    push()
    logic()
    pop()
}

fun modifyTopCopy(matrices: MatrixStack, logic: MatrixStack.() -> Unit) = matrices.onTop(logic)

fun Vec3d.deconstructPos() = Triple(x, y, z)

val Number.isZero
    get() = abs(toDouble()) == 0.0

fun Number.power(of: Number) = toDouble().pow(of.toDouble())
