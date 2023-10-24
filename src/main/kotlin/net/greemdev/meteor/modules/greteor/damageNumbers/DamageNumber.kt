/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.greteor.damageNumbers

import com.mojang.blaze3d.systems.RenderSystem
import net.greemdev.meteor.util.meteor.invoke
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.*
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f
import org.lwjgl.opengl.GL11

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

    fun render(matrices: MatrixStack, camera: Camera) {
        if (camera.pos.squaredDistanceTo(x, y, z) > DamageNumbers.distance().power(2)) return

        val scaleFactor = DamageNumbers.scaleFactor().toFloat()
        val tickDelta = minecraft.tickDelta

        val xx = lerp(tickDelta, prevX, x)
        val yy = lerp(tickDelta, prevY, y)
        val zz = lerp(tickDelta, prevZ, z)

        val (camX, camY, camZ) = camera.pos

        modifyTopCopy(matrices) {
            translate(xx - camX, yy - camY, zz - camZ)
            multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.yaw))
            multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.pitch))
            scale(scaleFactor, scaleFactor, scaleFactor)

            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            RenderSystem.enableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            val dmg = if (DamageNumbers.cumulative())
                entityState.lastDamageCumulative
            else damage

            DamageNumbers.drawNumber(this, dmg, 0f, 0f, 10f)

            RenderSystem.disableBlend()
        }
    }

}
