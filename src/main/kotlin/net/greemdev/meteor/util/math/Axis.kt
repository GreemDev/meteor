/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.math

import org.joml.Quaternionf
import kotlin.math.cos
import kotlin.math.sin

@Suppress("unused") //api
object Axis {

    //P = positive, N = negative; XN = negative X, etc
    val XN = Vec3f(-1.0F, 0.0F, 0.0F)
    val XP = Vec3f(1.0F, 0.0F, 0.0F)
    val YN = Vec3f(0.0F, -1.0F, 0.0F)
    val YP = Vec3f(0.0F, 1.0F, 0.0F)
    val ZN = Vec3f(0.0F, 0.0F, -1.0F)
    val ZP = Vec3f(0.0F, 0.0F, 1.0F)

    class Vec3f(val x: Float, val y: Float, val z: Float) {
        infix fun degreesQuaternion(rotationAngle: Float) = quaternion(rotationAngle, true)

        infix fun radialQuaternion(rotationAngle: Float) = quaternion(rotationAngle, false)

        private fun quaternion(fRotationAngle: Float, isDegrees: Boolean): Quaternionf {
            val rotationAngle = if (isDegrees) fRotationAngle * 0.017453292F else fRotationAngle

            return sin(rotationAngle / 2.0f)
                .let { f ->
                    Quaternionf(
                        x * f,
                        y * f,
                        z * f,
                        cos(rotationAngle / 2.0f)
                    )
                }
        }
    }
}
