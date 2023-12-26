/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("SameParameterValue")

package net.greemdev.meteor.modules.flightHud

import com.mojang.blaze3d.systems.RenderSystem
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.renderer.GL
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.*
import net.greemdev.meteor.util.math.*
import net.greemdev.meteor.util.meteor.bool
import net.greemdev.meteor.util.meteor.color
import net.greemdev.meteor.util.meteor.double
import net.greemdev.meteor.util.misc.currentWorld
import net.greemdev.meteor.util.misc.isAirAt
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.entity.player.PlayerEntity
import org.joml.Vector4f
import kotlin.math.*

import meteordevelopment.meteorclient.utils.java.*
import net.greemdev.meteor.util.misc.setRenderSystemShaderColor

private const val GRAVITY = -0.0784f

// Implementation originally by DeltaTimo, however the original mod is 1.19 only.
// There's a fork, however, updated for 1.20, by recolic.
// This module is based on recolic's fork: https://github.com/recolic/elytra-flight-hud/blob/recolic/1.20upgrade/src/main/java/eu/deltatimo/minecraft/elytrahud/ElytraFlightHud.java

@Suppress("RedundantSuppression") //fucking intellij
object ElytraFlightHud : GModule.Render("elytra-flight-HUD", "Shows relevant flight information on-screen while flying with Elytra.") {

    init {
        autoRegister = (MeteorClient.FOLDER / "indev" / "elytraHud").exists()
    }

    val lineColor by sg color {
        name("lines-color")
        description("The color of the display.")
        defaultValue(SettingColor(0, 255, 0))
    }

    val textColor by sg color {
        name("text-color")
        description("The color of the text in the display.")
        defaultValue(SettingColor(0, 255, 0))
    }

    val textShadow by sg bool {
        name("text-shadow")
        description("Render text with standard Minecraft text shadow.")
        defaultValue(false)
    }

    val lineWidth by sg double {
        name("line-width")
        description("The width of all rendered lines.")
        defaultValue(2.0)
        range(1.5, 4.0)
    }

    fun render(drawContext: DrawContext) {
        if (!isActive) return
        val player = mc.player?.takeIf(PlayerEntity::isFallFlying) ?: return

        // basic variables

        val aspect = mc.window.width.toDouble() / mc.window.height.toDouble()
        val fovDeg = mc.options.fov.value.toDouble()
        val fov = degreesToRadians(fovDeg)
        val horFov = atan(tan(fov / 2.0) * aspect) * 2
        val horFovDeg = radiansToDegrees(horFov)

        val screenWidth = mc.window.scaledWidth
        val screenHeight = mc.window.scaledHeight
        val screenLesser = min(screenWidth, screenHeight)
        val screenCenterX = screenWidth / 2
        val screenCenterY = screenHeight / 2

        val pixelsPerDegree = (screenHeight / fovDeg).toFloat()
        val pixelsPerHorizontalDegree = (screenWidth / horFovDeg).toFloat()

        val cameraPitchDegree = mc.gameRenderer.camera.pitch.toDouble()


        val horizonWidth = screenWidth / 8f
        val horizonVerticalBlipLength = screenHeight / 160f
        val centerHeight = screenCenterY + (pixelsPerDegree * -cameraPitchDegree).toFloat()

        val velocity = player.velocity.rotateY(degreesToRadians(mc.gameRenderer.camera.yaw)).rotateX(degreesToRadians(mc.gameRenderer.camera.pitch))

        val upwardSpeed = velocity.y
        val forwardSpeed = velocity.z
        val sidewaysSpeed = -velocity.x
        val upwardSpeedAngle = radiansToDegrees(atan2(upwardSpeed, forwardSpeed))
        val rightSpeedAngle = radiansToDegrees(atan2(sidewaysSpeed, forwardSpeed))

        val flightVectorX = screenCenterX + (pixelsPerHorizontalDegree * rightSpeedAngle).toFloat()
        val flightVectorY = screenCenterY - (pixelsPerDegree * upwardSpeedAngle).toFloat()

        val world = mc.currentWorld()
        val playerBlockPos = player.blockPos

        var radarHeight = 0


        forLoop int {
            startAt = playerBlockPos.y - 1
            increment = -1
            condition = { it >= world.bottomY && world.isAirAt(playerBlockPos.x, it, playerBlockPos.z) }
            body = { radarHeight++ }
        }

        /*Java.loop(
            playerBlockPos.y - 1,
            -1,
            { it >= world.bottomY && world.isAirAt(playerBlockPos.x, it, playerBlockPos.z) },
            { radarHeight++ }
        )*/

        val preShaderColor = RenderSystem.getShaderColor().let { (r, g, b, a) -> Vector4f(r, g, b, a) }
        val preShader = RenderSystem.getShader()
        val preLineWidth = RenderSystem.getShaderLineWidth()

        val lineColor = lineColor().also {
            it.toVector4f().also(::setRenderSystemShaderColor)
        }

        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        RenderSystem.enableBlend()

        val (positionMatrix, normalMatrix) = drawContext.matrices.peekPositionAndNormalMatrix()

        GL.depthMask(false)
        GL.disableCull()


        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        val tessellator = Tessellator.getInstance()

        RenderSystem.lineWidth(lineWidth.valueAsFloat)
        tessellator.buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)

        val draw = HudLine.drawer(tessellator.buffer, positionMatrix, normalMatrix)

        // draw lines on the horizon UPWARDS. (-degrees in minecraft)
        loop(
            -90,
            10,
            Lambdas.aboveZero()
        ) {
            // Out of upper screen bound. next!
            val diffDeg = it - cameraPitchDegree // -90 - -30 = -90 + 30 = -60
            val diffPixels = (pixelsPerDegree * diffDeg).toFloat()
            val height = screenCenterY + diffPixels
            if (isOutOfBounds(height, screenHeight)) return@loop

            draw(
                line(
                    screenCenterX - horizonWidth / 2f,
                    height,
                    screenCenterX - horizonWidth / 2f + horizonWidth / 3,
                    height
                ).color(lineColor)
            )
            draw(
                line(
                    screenCenterX - horizonWidth / 2f,
                    height,
                    screenCenterX - horizonWidth / 2f,
                    height + horizonVerticalBlipLength
                ).color(lineColor)
            )
            draw(
                line(
                    screenCenterX - horizonWidth / 2f + 2 * horizonWidth / 3f,
                    height,
                    screenCenterX - horizonWidth / 2f + horizonWidth,
                    height
                ).color(lineColor)
            )
            draw(
                line(
                    screenCenterX - horizonWidth / 2f + horizonWidth,
                    height,
                    screenCenterX - horizonWidth / 2f + horizonWidth,
                    height + horizonVerticalBlipLength
                ).color(lineColor)
            )
        }

        // draw lines on the horizon UPWARDS. (-degrees in minecraft)
        loop(
            90,
            -10,
            Lambdas.aboveZero()
        ) {
            // Out of upper screen bound. next!
            val diffDeg = it - cameraPitchDegree // -90 - -30 = -90 + 30 = -60
            val diffPixels = (pixelsPerDegree * diffDeg).toFloat()
            val height = screenCenterY + diffPixels
            if (isOutOfBounds(height, screenHeight)) return@loop

            // left horizontal lines
            val leftX = screenCenterX - horizonWidth / 2f
            val partWidth = horizonWidth / 3f / 3f
            draw(line(leftX, height, leftX + partWidth - partWidth / 3, height).color(lineColor))
            draw(line(leftX + partWidth, height, leftX + 2 * partWidth - partWidth / 3, height).color(lineColor))
            draw(line(leftX + 2 * partWidth, height, leftX + 3 * partWidth, height).color(lineColor))

            // right horizontal lines
            val rightX = screenCenterX + horizonWidth / 2f
            draw(line(rightX, height, rightX - partWidth + partWidth / 3, height).color(lineColor))
            draw(
                line(
                    rightX - partWidth,
                    height,
                    rightX - 2 * partWidth + partWidth / 3,
                    height
                ).color(lineColor)
            )
            draw(line(rightX - 2 * partWidth, height, rightX - 3 * partWidth, height).color(lineColor))

            // vertical ends
            draw(
                line(
                    screenCenterX - horizonWidth / 2f,
                    height,
                    screenCenterX - horizonWidth / 2f,
                    height - horizonVerticalBlipLength
                ).color(lineColor)
            )
            draw(
                line(
                    screenCenterX - horizonWidth / 2f + horizonWidth,
                    height,
                    screenCenterX - horizonWidth / 2f + horizonWidth,
                    height - horizonVerticalBlipLength
                ).color(lineColor)
            )
        }

        if (!isOutOfBounds(centerHeight, screenHeight)) {
            draw(
                line(
                    screenCenterX - horizonWidth / 2f,
                    centerHeight,
                    screenCenterX - horizonWidth / 2f + horizonWidth / 3,
                    centerHeight
                ).color(lineColor)
            )
            draw(
                line(
                    screenCenterX - horizonWidth / 2f + 2 * horizonWidth / 3f,
                    centerHeight,
                    screenCenterX + horizonWidth / 2f,
                    centerHeight
                ).color(lineColor)
            )
        }

        val flightVectorSize = screenLesser / 70f
        val flightVectorRadius = flightVectorSize / 2

        circleLines(flightVectorX, flightVectorY, flightVectorRadius, 10)
            .forEach(draw::invoke)

        val elytraRollDeg = if (forwardSpeed < 0.0001f)
            0f
        else
            ((sidewaysSpeed / forwardSpeed).coerceIn(-2.0, 2.0) * -45f).toFloat()
        val elytraRoll = degreesToRadians(elytraRollDeg)

        draw(
            line(
                flightVectorX + sin(elytraRoll) * flightVectorRadius,
                flightVectorY - cos(elytraRoll) * flightVectorRadius,
                flightVectorX + sin(elytraRoll) * (flightVectorRadius + flightVectorSize * 0.4f),
                flightVectorY - cos(elytraRoll) * (flightVectorRadius + flightVectorSize * 0.4f)
            ).color(lineColor)
        )

        draw(
            line(
                flightVectorX + sin(elytraRoll + 0.5 * Math.PI).toFloat() * flightVectorRadius,
                flightVectorY - cos(elytraRoll + 0.5 * Math.PI).toFloat() * flightVectorRadius,
                flightVectorX + sin(elytraRoll + 0.5 * Math.PI).toFloat() * (flightVectorRadius + flightVectorSize * 0.5f),
                flightVectorY - cos(elytraRoll + 0.5 * Math.PI).toFloat() * (flightVectorRadius + flightVectorSize * 0.5f)
            ).color(lineColor)
        )

        draw(
            line(
                flightVectorX + sin(elytraRoll - 0.5 * Math.PI).toFloat() * flightVectorRadius,
                flightVectorY - cos(elytraRoll - 0.5 * Math.PI).toFloat() * flightVectorRadius,
                flightVectorX + sin(elytraRoll - 0.5 * Math.PI).toFloat() * (flightVectorRadius + flightVectorSize * 0.5f),
                flightVectorY - cos(elytraRoll - 0.5 * Math.PI).toFloat() * (flightVectorRadius + flightVectorSize * 0.5f)
            ).color(lineColor)
        )

        val flightVectorAngle = atan2(screenCenterY - flightVectorY, screenCenterX - flightVectorX)
        draw(
            line(
                screenCenterX + 0.66f * (flightVectorX - screenCenterX),
                screenCenterY + 0.66f * (flightVectorY - screenCenterY),
                flightVectorX + (flightVectorSize / 2f) * cos(flightVectorAngle),
                flightVectorY + (flightVectorSize / 2f) * sin(flightVectorAngle)
            ).color(lineColor)
        )

        // Compass

        val heading = mc.gameRenderer.camera.yaw
        val compassWidth = horizonWidth * 0.85f

        val headingFives = round((heading * 2) / 10) / 2

        val compassBlipHeight = screenHeight / 200f

        loop(
            headingFives - 3f * 0.5f,
            0.5f,
            { it <= heading / 10f + 3 * 0.5f }
        ) {
            if (it < heading / 10f - 3 * 0.5f) return@loop
            val headingOffset = heading - (it * 10f)
            val headingX = screenCenterX - (headingOffset / 15f) * compassWidth / 2f
            draw(
                line(
                    headingX,
                    screenCenterY + screenHeight / 4f + mc.textRenderer.fontHeight * 1.05f,
                    headingX,
                    screenCenterY + screenHeight / 4f + mc.textRenderer.fontHeight * 1.05f + compassBlipHeight
                ).color(lineColor)
            )
        }

        val compassTriangleY = screenCenterY + screenHeight / 4f + mc.textRenderer.fontHeight * 1.05f + compassBlipHeight * 0.95f
        @Suppress("UnnecessaryVariable") /* makes math readability better */
        val compassTriangleHeight = compassBlipHeight
        val compassTriangleWidth = compassBlipHeight * 2f

        draw(line(screenCenterX.toFloat(), compassTriangleY, screenCenterX + compassTriangleWidth / 2f, compassTriangleY + compassTriangleHeight))
        draw(line(screenCenterX + compassTriangleWidth / 2f, compassTriangleY + compassTriangleHeight, screenCenterX - compassTriangleWidth / 2f, compassTriangleY + compassTriangleHeight))
        draw(line(screenCenterX - compassTriangleWidth / 2f, compassTriangleY + compassTriangleHeight, screenCenterX.toFloat(), compassTriangleY))

        tessellator.draw() // this calls BufferBuilder#end

        RenderSystem.lineWidth(1f)

        val playerVelocityVector = player.velocity.apply(y = -GRAVITY.toDouble())
        val airSpeed = round(playerVelocityVector.length() * 100f)
        if (lineColor.alpha() > 0.66) {
            loop(-90, 10, { it < 90 }) {
                if (it == 0) return@loop
                // Out of upper screen bound. next!

                val diffDeg = it - cameraPitchDegree // -90 - -30 = -90 + 30 = -60
                val diffPixels = (pixelsPerDegree * diffDeg).toFloat()

                val height = screenCenterY + diffPixels - (mc.textRenderer.fontHeight * (if (it > 0) 0.9f else 0.15f))
                if (isOutOfBounds(height, screenHeight)) return@loop
                val text = abs(it).toString()
                drawContext.drawText(
                    mc.textRenderer,
                    text,
                    (screenCenterX + horizonWidth / 2).toInt(),
                    height.toInt(),
                    textColor().packed,
                    textShadow()
                )
                drawContext.drawText(
                    mc.textRenderer,
                    text,
                    (screenCenterX - horizonWidth / 2f - mc.textRenderer.getWidth(text) - screenWidth / 500f).toInt(),
                    height.toInt(),
                    textColor().packed,
                    textShadow()
                )
            }

            drawContext.drawText(
                mc.textRenderer,
                floor(player.pos.y).toInt().toString(),
                (screenCenterX + horizonWidth * 0.8f).toInt(),
                screenCenterY,
                textColor().packed,
                textShadow()
            )

            val fallDistanceColor = when {
                player.fallDistance > player.safeFallDistance * 2 -> MeteorColor.RED
                player.fallDistance > player.safeFallDistance -> MeteorColor.ORANGE.darker()
                player.fallDistance > player.safeFallDistance * 0.75 -> MeteorColor.YELLOW
                else -> MeteorColor.GREEN
            }

            drawContext.drawText(
                mc.textRenderer,
                round(playerVelocityVector.y.toFloat() * 10f).toString(),
                (screenCenterX + horizonWidth * 0.8f).toInt(),
                (screenCenterY.toFloat() + mc.textRenderer.fontHeight * 1.5f).toInt(),
                fallDistanceColor.packed,
                textShadow()
            )
            drawContext.drawText(
                mc.textRenderer,
                radarHeight.toString() + "R",
                (screenCenterX + horizonWidth * 0.75f).toInt(),
                screenCenterY + screenHeight / 8,
                textColor().packed,
                textShadow()
            )

            val airSpeedColor = horizontalCollisionDamage(player).let {
                when {
                    it > 5 -> MeteorColor(136, 255, 0)
                    it > 0 -> MeteorColor(68, 255, 0)
                    else -> MeteorColor.GREEN
                }
            }

            drawContext.drawText(mc.textRenderer, airSpeed.toString(), (screenCenterX - horizonWidth * 0.75f - mc.textRenderer.getWidth(airSpeed.toString())).toInt(), screenCenterY, airSpeedColor.packed, textShadow())

            loop(
                headingFives - 3f * 0.5f,
                0.5f,
                { it <= heading / 10f + 3 * 0.5f }
            ) {
                // Skip first blip if it's outside.
                if (it < heading / 10f - 3 * 0.5f) return@loop
                if (it == floor(it)) {
                    var headingBlip360 = if (it < 0) 36 - realMod(it, 36f) else it % 36
                    if (headingBlip360 == 36f)
                        headingBlip360 = 0f

                    val headingText = (if (floor(headingBlip360) < 10) "0" else "") + floor(headingBlip360).toInt()
                    val headingOffset = heading - it * 10f
                    val headingX = screenCenterX - headingOffset / 15f * compassWidth / 2f
                    drawContext.drawText(
                        mc.textRenderer,
                        headingText,
                        (headingX - mc.textRenderer.getWidth(headingText) / 2f).toInt(),
                        screenCenterY + screenHeight / 4,
                        textColor().packed,
                        textShadow()
                    );
                }
            }

            setRenderSystemShaderColor(preShaderColor)
            RenderSystem.lineWidth(preLineWidth)
            RenderSystem.setShader(Lambdas.constant(preShader))

            GL.depthMask(true)
            GL.enableCull()
        }
    }
}




private fun realMod(a: Float, b: Float) =
    (a % b).let {
        it.takeUnless { it < 0 } ?: (it + b)
        /*if (it < 0)
            it + b
        else it*/
    }


private fun isOutOfBounds(height: Float, screenHeight: Int) =
    height > screenHeight * 0.85f || height < screenHeight * 0.15f


private const val DOUBLE_PI = Math.PI * 2
private fun circleLines(x: Float, y: Float, radius: Float, parts: Int): Collection<HudLine> =
    buildList {
        val doublePiDivParts = DOUBLE_PI / parts

        forLoop double {
            startAt = 0.0
            increment = doublePiDivParts
            condition = { it < DOUBLE_PI }
            body = {
                add(
                    line(
                        x + cos(it) * radius,
                        y + sin(it) * radius,
                        x + cos(it + doublePiDivParts) * radius,
                        y + sin(it + doublePiDivParts) * radius
                    )
                )
            }
        }
    }

private fun horizontalCollisionDamage(player: PlayerEntity) =
    player.velocity.multiply(0.99, 0.98, 0.99).horizontalLength() * 10.0 - player.safeFallDistance
