/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.render

import net.greemdev.meteor.modules.DamageNumbers
import net.greemdev.meteor.util.meteor.Meteor
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.currentWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper

data class EntityState(val entity: LivingEntity) {
    companion object : HashMap<Int, EntityState>() {
        const val healthIndicatorDelay = 10f
        private var ticked = 0

        @Suppress("DEPRECATION")
        fun removeExpired() = entries.removeIf {
            val entity = minecraft.currentWorld().getEntityById(it.key)
            if (entity !is LivingEntity)
                true
            else if (!minecraft.currentWorld().isChunkLoaded(entity.blockPos))
                true
            else !entity.isAlive
        }
        fun getOrTrack(entity: LivingEntity) = computeIfAbsent(entity.id) { EntityState(entity) }
        fun tick() {
            forEach { _, state -> state.tick() }
            if (ticked >= 200) {
                removeExpired()
                ticked = 0
            } else ticked++
        }
    }

    var health = 0f
        private set
    var lastDamage = 0
        private set
    var lastDamageCumulative = 0
        private set
    var lastHealth = 0f
        private set
    var lastDamageDelay = 0f
        private set

    fun tick() {
        health = entity.health.coerceAtMost(entity.maxHealth)
        tickTimer()

        if (lastHealth < 0.1)
            reset()
        else if (lastHealth != health)
            onHealthChange()
        else if (lastDamageDelay == 0f)
            reset()
    }

    private fun reset() {
        lastHealth = health
        lastDamage = 0
        lastDamageCumulative = 0
    }

    private fun tickTimer() {
        if (lastDamageDelay > 0)
            lastDamageDelay--
    }

    private fun onHealthChange() {
        lastDamage = MathHelper.ceil(lastHealth) - MathHelper.ceil(health)
        lastDamageCumulative += lastDamage

        lastDamageDelay = healthIndicatorDelay * 2
        lastHealth = health
        if (Meteor.module<DamageNumbers>().isActive)
            DamageNumbers.particles.add(DamageParticle(this, lastDamage))
    }
}
