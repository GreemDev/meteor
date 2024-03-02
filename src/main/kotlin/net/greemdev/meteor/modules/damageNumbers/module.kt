/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.damageNumbers

import com.mojang.blaze3d.systems.RenderSystem
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.settings.ColorSetting
import meteordevelopment.meteorclient.settings.EnumSetting
import meteordevelopment.meteorclient.utils.render.color.*
import meteordevelopment.orbit.EventHandler
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.greemdev.meteor.*
import net.greemdev.meteor.type.DamageOperatorType
import net.greemdev.meteor.util.math.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.DrawContext
import net.greemdev.meteor.util.text.ChatColor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import org.lwjgl.opengl.GL11
import kotlin.math.abs
import kotlin.math.round


private val particles = hashSetOf<DamageNumber>()

//Implementation based on ToroHealth
object DamageNumbers : GModule.Render(
    "damage-numbers", "Floating, disappearing text when you damage enemies showing how much damage was done."
) {
    private val sgC = settings group "Colors"

    val ignoreSelf by sg bool {
        name("ignore-self")
        description("Don't render damage numbers for damage you take.")
        defaultValue(true)
    }

    val operatorPrefix: EnumSetting<DamageOperatorType> by sg enum {
        name("operator-prefix")
        description("The type of prefixing you want on the indicators.")
        defaultValue(DamageOperatorType.None)
        onChanged {
            if (!it.supportsRainbow and rainbowIndicators())
                rainbowIndicators.set(false)
        }
    }

    val rainbowIndicators by sg bool {
        name("rainbow-numbers")
        description("With operators enabled, you have the option to make both types of damage indicator &zrainbow&r.")
        defaultValue(false)
        visible { operatorPrefix().supportsRainbow }
    }

    val showDecimal by sg bool {
        name("display-decimal")
        description("Display the decimal point of the damage number.")
        defaultValue(false)
    }

    val precision by sg int {
        name("decimal-precision")
        description("How many digits after the decimal point to display.")
        range(1, 10)
        defaultValue(2)
    }

    val cumulative by sg bool {
        name("cumulative-damage")
        description("When a damage number is rendered, show the total amount of damage done to the target, instead of the damage of the individual hit.")
        defaultValue(false)
    }

    val shadowedText by sg bool {
        name("text-shadow")
        description("Whether or not damage numbers are rendered with a shadow.")
        defaultValue(false)
    }

    val distance by sg int {
        name("distance-from-camera")
        description("The max distance from the camera before a particle stops rendering.")
        defaultValue(60)
        range(1, 240)
    }

    val damageColor: ColorSetting by sgC color {
        name("damage-color")
        description("The color of the numbers when an entity is &4damaged&r.")
        defaultValue(ChatColor.darkRed.meteor.brighter())
        canRainbow(false)
        onChanged {
            if (it.rainbow) resetDmg()
        }
        visible { !rainbowIndicators() }
    }

    val healColor by sgC color {
        name("heal-color")
        description("The color of the numbers when an entity is &ahealed&r.")
        defaultValue(ChatColor.green.meteor)
        canRainbow(false)
        onChanged {
            if (it.rainbow) resetHeal()
        }
        visible { !rainbowIndicators() }
    }

    private fun resetDmg() { damageColor.reset() }
    private fun resetHeal() { healColor.reset() }

    val scaleFactor by sg double {
        name("scaling")
        description("The scaling factor of the damage number.")
        defaultValue(-0.025)
        range(-0.050, -0.010)
    }

    val maxAge by sg int {
        name("number-lifetime")
        description("The amount of ticks that a damage number remains rendered.")
        defaultValue(50)
        range(30, 120)
    }

    fun getColor(isDamage: Boolean): Color =
        if (rainbowIndicators()) RainbowColor.current()
        else if (isDamage) damageColor()
        else healColor()


    fun drawNumber(dmg: Float, drawContext: DrawContext) {
        val x = 0
        val y = 0
        val width = 10f

        val number = abs(dmg).takeUnless(Number::isZero) ?: return
        val numStr = (
            if (showDecimal())
                number.precision(precision())
            else
                round(number).toInt()
            ).toString()

        val formattedNumber = operatorPrefix().formatNumber(if (dmg > 0) '-' else '+', numStr)

        val sw = minecraft.textRenderer.getWidth(formattedNumber)
        val color = getColor(dmg > 0)

        drawContext.drawText(
            minecraft.textRenderer,
            formattedNumber,
            (x + (width / 2) - sw).toInt(),
            y + 5,
            color.packed,
            shadowedText()
        )
    }

    @EventHandler
    private fun postTick(event: TickEvent.Post) {
        EntityState.tick()
        tick()
    }

    fun add(entityState: EntityState, lastDamage: Float) = if (isActive) particles.add(DamageNumber(entityState, lastDamage)) else false

    fun tick() {
        particles.forEach(DamageNumber::tick)
        particles.removeIf { it.age > DamageNumbers.maxAge() }
    }
    @JvmStatic
    fun render(wrctx: WorldRenderContext) =
        particles.forEach { render(it, wrctx) }

    fun render(dn: DamageNumber, wrctx: WorldRenderContext) {
        if (wrctx.camera().pos.squaredDistanceTo(dn.x, dn.y, dn.z) > distance().power(2)) return

        val tickDelta = minecraft.tickDelta

        val (camX, camY, camZ) = wrctx.camera().pos

        modifyPushedCopy(wrctx.matrixStack()) {
            translate(
                tickDelta.lerp(dn.prevX, dn.x) - camX,
                tickDelta.lerp(dn.prevY, dn.y) - camY,
                tickDelta.lerp(dn.prevZ, dn.z) - camZ
            )

            multiply(Axis.YP.degreesQuaternion(-wrctx.camera().yaw))
            multiply(Axis.XP.degreesQuaternion(wrctx.camera().pitch))
            scaleFactor.asFloat.also { scale(-it, -it, it) }

            RenderSystem.setShader(GameRenderer::getPositionColorProgram)
            RenderSystem.enableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            // WorldRenderContext#consumers() is guaranteed to be present at the point in WorldRenderer.render where
            // this entire function is called (WorldRenderEvents.AFTER_ENTITIES), therefore !! should never cause issues

            drawNumber(dn.getDisplayDamageNumber(), DrawContext(Tessellator.getInstance().buffer))

            RenderSystem.disableBlend()
        }

        RenderSystem.enableCull()
    }
}
