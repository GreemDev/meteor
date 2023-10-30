/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

/*package net.greemdev.meteor.modules.damageNumbers

import net.greemdev.meteor.*
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.currentWorld
import net.greemdev.meteor.util.misc.player
import net.minecraft.entity.LivingEntity

data class EntityState(val entity: LivingEntity) {
    var health = 0f
        private set
    var lastDamage = 0f
        private set
    var lastDamageCumulative = 0f
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
        lastDamage = 0f
        lastDamageCumulative = 0f
    }

    private fun tickTimer() {
        if (lastDamageDelay > 0)
            lastDamageDelay--
    }

    private fun onHealthChange() {
        lastDamage = lastHealth - health
        lastDamageCumulative += lastDamage

        lastDamageDelay = healthIndicatorDelay * 2
        lastHealth = health
        if (DamageNumbers.ignoreSelf() && minecraft.player?.uuid == entity.uuid)
            return

        DamageNumbers.add(DamageNumber(this, lastDamage))
    }

    // Manager of EntityState instances
    companion object : HashMap<Int, EntityState>() {
        const val healthIndicatorDelay = 10f
        private var ticked = 0

        fun clean() = entries.removeIf { (id, state) ->
            val entity = minecraft.currentWorld().getEntityById(id)
            if (entity !is LivingEntity)
                true
            else if (!minecraft.currentWorld().chunkManager.isChunkLoaded(entity.blockPos.x, entity.blockPos.z))
                true
            else if (DamageNumbers.ignoreSelf() && minecraft.player().uuid == state.entity.uuid)
                true
            else !entity.isAlive
        }
        @JvmStatic
        fun track(entity: LivingEntity) {
            if (DamageNumbers.ignoreSelf() && minecraft.player().uuid == entity.uuid) return

            computeIfAbsent(entity.id) {
                EntityState(entity)
            }
        }
        fun tick() {
            forEach { _, state -> state.tick() }
            if (ticked >= 200) {
                clean()
                ticked = 0
            } else ticked++
        }
    }
}
*/
