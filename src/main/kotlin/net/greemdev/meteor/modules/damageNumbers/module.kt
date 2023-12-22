/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.damageNumbers

import com.mojang.blaze3d.systems.RenderSystem
import meteordevelopment.meteorclient.MeteorClient
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
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import org.lwjgl.opengl.GL11
import java.io.File
import kotlin.math.abs
import kotlin.math.round


private val particles = hashSetOf<DamageNumber>()

//Implementation based on ToroHealth
object DamageNumbers : GModule(
    "damage-numbers", "Floating, disappearing text when you damage enemies showing how much damage was done."
) {

    init {
        autoRegister = (MeteorClient.FOLDER / "indev" / "damageNumbers").exists()
    }

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
        visible(rainbowIndicators.inverse())
    }

    val healColor by sgC color {
        name("heal-color")
        description("The color of the numbers when an entity is &ahealed&r.")
        defaultValue(ChatColor.green)
        canRainbow(false)
        onChanged {
            if (it.rainbow) resetHeal()
        }
        visible(rainbowIndicators.inverse())
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

    fun getColor(isDamage: Boolean): Color = when {
        rainbowIndicators() -> RainbowColor.current()
        isDamage -> damageColor()
        else -> healColor()
    }


    private fun drawNumber(dmg: Float, drawContext: DrawContext) {
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
    private fun preTick(event: TickEvent.Pre) =
        tick()


    override fun onDeactivate() {
        EntityState.clear()
        particles.clear()
    }

    fun add(entityState: EntityState, lastDamage: Float) = if (isActive) particles.add(DamageNumber(entityState, lastDamage)) else false

    fun tick() {
        EntityState.tick()

        particles.forEach(DamageNumber::tick)
        particles.removeIf { it.age > DamageNumbers.maxAge() }
    }
    @JvmStatic
    fun render(context: WorldRenderContext) {
        if (DamageNumbers.isActive)
            particles.forEach { render(it, context.camera(), context.matrixStack(), context.tickDelta()) }
    }

    fun render(dn: DamageNumber, camera: Camera, matrices: MatrixStack, tickDelta: Float) {
        if (camera.pos.squaredDistanceTo(dn.x, dn.y, dn.z) > distance().power(2)) return

        modifyPushedCopy(matrices) {
            translate(
                tickDelta.lerp(dn.prevX, dn.x) - camera.pos.x,
                tickDelta.lerp(dn.prevY, dn.y) - camera.pos.y,
                tickDelta.lerp(dn.prevZ, dn.z) - camera.pos.z
            )

            multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.yaw))
            multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.pitch))
            scaleFactor.valueAsFloat.also { scale(-it, -it, it) }

            RenderSystem.setShader(GameRenderer::getPositionColorProgram)
            RenderSystem.enableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            //val vertexConsumer = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)

            drawNumber(dn.displayNumber, DrawContext(Tessellator.getInstance().buffer))

            RenderSystem.disableBlend()
        }

        RenderSystem.enableCull()
    }
}
