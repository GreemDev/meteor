/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.damageNumbers

import net.greemdev.meteor.invoke
import net.greemdev.meteor.util.math.change
import net.greemdev.meteor.util.math.minecraftRandom
import net.greemdev.meteor.util.math.nextGaussian
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.*

class DamageNumber(val entityState: EntityState, val damage: Float) {
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var prevX = 0.0
    var prevY = 0.0
    var prevZ = 0.0

    var age = 0

    //var ax = 0.0f
    var ay = -0.01f
    //var az = 0.0f

    var vx = 0.0f
    var vy = 0.0f
    var vz = 0.0f

    init {
        val entityLoc = entityState.entity.pos.change(y = (entityState.entity.height / 2).toDouble())
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

    fun getDisplayDamageNumber() = if (DamageNumbers.cumulative()) entityState.lastDamageCumulative else damage

    fun tick() {
        prevX = x
        prevY = y
        prevZ = z
        age++
        x += vx
        y += vy
        z += vz
        //vx += ax
        vy += ay
        //vz += az
    }
}
