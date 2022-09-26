/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.render

import com.mojang.blaze3d.systems.RenderSystem
import net.greemdev.meteor.modules.DamageNumbers
import net.greemdev.meteor.util.meteor.Meteor
import net.greemdev.meteor.util.meteor.invoke
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.*
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f
import org.lwjgl.opengl.GL11
import java.util.concurrent.ThreadLocalRandom

class DamageParticle(val entityState: EntityState, val damage: Int) {

    private val rand = ThreadLocalRandom.current()

    var x = 0.0
    var y = 0.0
    var z = 0.0
    var prevX = 0.0
    var prevY = 0.0
    var prevZ = 0.0

    var age = 0

    var ax = 0.0
    var ay = -0.01
    var az = 0.0

    var vx = 0.0
    var vy = 0.0
    var vz = 0.0

    init {
        val entityLoc = entityState.entity.pos.add(0.0, (entityState.entity.height / 2).toDouble(), 0.0)
        val cameraLoc = minecraft.gameRenderer.camera.pos
        val offsetBy = entityState.entity.width.toDouble()
        val offset = cameraLoc.subtract(entityLoc).normalize() * offsetBy
        val pos = entityLoc.add(offset)

        vx = rand.nextGaussian() * 0.04
        vy = 0.10 + (rand.nextGaussian() * 0.05)
        vz = rand.nextGaussian() * 0.04

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
        val dn = Meteor.module<DamageNumbers>()
        if (camera.pos.squaredDistanceTo(x, y, z) > dn.distance() * dn.distance()) return

        val scaleFactor = dn.scaleFactor().toFloat()
        val tickDelta = minecraft.tickDelta

        val xx = MathHelper.lerp(tickDelta.toDouble(), prevX, x)
        val yy = MathHelper.lerp(tickDelta.toDouble(), prevY, y)
        val zz = MathHelper.lerp(tickDelta.toDouble(), prevZ, z)

        val (camX, camY, camZ) = camera.pos.deconstructPos()

        modifyTopCopy(matrices) {
            translate(xx - camX, yy - camY, zz - camZ)
            multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.yaw))
            multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.pitch))
            scale(scaleFactor, scaleFactor, scaleFactor)

            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            RenderSystem.enableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            val dmg = if (Meteor.module<DamageNumbers>().cumulative())
                entityState.lastDamageCumulative
            else damage

            dn.drawNumber(this, dmg, 0f, 0f, 10f)

            RenderSystem.disableBlend()
        }
    }

}
