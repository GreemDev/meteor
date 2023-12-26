/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.damageNumbers

import net.greemdev.meteor.*
import net.greemdev.meteor.util.meteor.Meteor
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

        if (lastDamageDelay > 0)
            lastDamageDelay--

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

    private fun onHealthChange() {
        lastDamage = lastHealth - health
        lastDamageCumulative += lastDamage

        lastDamageDelay = HealthIndicatorDelay * 2
        lastHealth = health

        if (DamageNumbers.ignoreSelf() && entity.uuid == minecraft.player?.uuid)
            return

        DamageNumbers.add(this, lastDamage)
    }

    // Manager of EntityState instances
    companion object : HashMap<Int, EntityState>() {
        private val cleaner = ticker {
            tickLimit(200)
            action {
                val cleanedEntries = entries.retainMatching { (id, state) ->
                    val entity = minecraft.currentWorld().getEntityById(id)
                        ?: return@retainMatching false

                    entity is LivingEntity &&
                        minecraft.currentWorld().chunkManager.isChunkLoaded(entity.blockPos.x, entity.blockPos.z) and
                        !(DamageNumbers.ignoreSelf() && minecraft.player().uuid == state.entity.uuid) and
                        entity.isAlive

                    //TODO: ensure this new cache cleaner works properly
                }

                if (cleanedEntries > 0)
                    Greteor.debug("cleared $cleanedEntries EntityStates")
            }
        }

        @JvmStatic
        fun track(entity: LivingEntity) {
            if (!DamageNumbers.isActive) return

            if (DamageNumbers.ignoreSelf() && minecraft.player().uuid == entity.uuid) return

            computeIfAbsent(entity.id) {
                EntityState(entity)
            }
        }
        fun tick() {
            if (Meteor.isModuleActive(DamageNumbers::class.java)) {
                cleaner.tick()
                values.forEach(EntityState::tick)
            } else {
                if (isNotEmpty())
                    clear()
            }
        }
    }
}

private const val HealthIndicatorDelay = 10f
