/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.damageNumbers

import com.mojang.blaze3d.systems.RenderSystem
import net.greemdev.meteor.invoke
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import org.joml.AxisAngle4d
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

class DamageNumber(val entityState: EntityState, val damage: Float) {
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var prevX = 0.0
    var prevY = 0.0
    var prevZ = 0.0

    var age = 0

    var ax = 0.0f
    var ay = -0.01f
    var az = 0.0f

    var vx = 0.0f
    var vy = 0.0f
    var vz = 0.0f

    init {
        val entityLoc = entityState.entity.pos.add(0.0, (entityState.entity.height / 2).toDouble(), 0.0)
        val cameraLoc = minecraft.gameRenderer.camera.pos
        val offsetBy = entityState.entity.width.toDouble()
        val offset = cameraLoc.subtract(entityLoc).normalize() * offsetBy
        val pos = entityLoc.add(offset)

        vx = minecraftRandom.nextGaussian(0f, 0.04f)
        vy = minecraftRandom.nextGaussian(0.10f, 0.05f)
        vz = minecraftRandom.nextGaussian(0f, 0.04f)

        x = pos.x
        y = pos.y
        z = pos.z

        prevX = x
        prevY = y
        prevZ = z
    }

    fun tick() {
        prevX = x
        prevY = y
        prevZ = z
        age++
        x += vx
        y += vy
        z += vz
        vx += ax
        vy += ay
        vz += az
    }
}

object Axis {

    val XN = Vec3f(-1.0F, 0.0F, 0.0F)
    val XP = Vec3f(1.0F, 0.0F, 0.0F)
    val YN = Vec3f(0.0F, -1.0F, 0.0F)
    val YP = Vec3f(0.0F, 1.0F, 0.0F)
    val ZN = Vec3f(0.0F, 0.0F, -1.0F)
    val ZP = Vec3f(0.0F, 0.0F, 1.0F)

    @Suppress("LocalVariableName")
    class Vec3f(val x: Float, val y: Float, val z: Float) {
        infix fun degreesQuaternion(rotationAngle: Float) = quaternion(rotationAngle, true)

        infix fun radialQuaternion(rotationAngle: Float) = quaternion(rotationAngle, false)

        fun quaternion(rotationAngle_: Float, degrees: Boolean): Quaternionf {
            val rotationAngle = if (degrees) rotationAngle_ * 0.017453292F else rotationAngle_

            return sin(rotationAngle / 2.0f).let { f ->
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
