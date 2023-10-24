/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.world

import meteordevelopment.meteorclient.events.render.Render3DEvent
import meteordevelopment.meteorclient.renderer.ShapeMode
import meteordevelopment.meteorclient.utils.misc.Keybind
import meteordevelopment.meteorclient.utils.misc.input.KeyAction
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.MeteorColor
import net.greemdev.meteor.event.GameInputEvent
import net.greemdev.meteor.invoking
import net.greemdev.meteor.util.baritone
import net.greemdev.meteor.util.meteor.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT

object Excavator : GModule.World("excavator", "Excavate a selection area via Baritone.") {

    private val sgR = settings group "Rendering"

    val selectionKey by sg keybind {
        name("selection-key")
        description("Key to draw the selection.")
        defaultValue(Keybind.fromButton(GLFW_MOUSE_BUTTON_RIGHT))
    }

    val logSelection by sg bool {
        name("log-selection")
        description("Logs the selection coordinates to the chat.")
        defaultValue(true)
    }

    val shapeMode by sgR.enum<ShapeMode> {
        name("shape-mode")
        description("How the shapes are rendered.")
        defaultValue(ShapeMode.Both)
    }

    val sideColor by sgR color {
        name("side-color")
        description("The side color.")
        defaultValue(MeteorColor(255, 255, 255, 50))
    }

    val lineColor by sgR color {
        name("line-color")
        description("The line color.")
        defaultValue(MeteorColor(255, 255, 255, 255))
    }

    private var status = Status.SelStart

    override fun onActivate() {
        status = Status.SelStart
    }

    override fun onDeactivate() {
        if (status == Status.SelEnd)
            baritone.commandManager.execute("sel clear")
        else
            baritone.selectionManager.removeSelection(baritone.selectionManager.lastSelection)

        if (baritone.builderProcess.isActive)
            baritone.commandManager.execute("stop")
    }

    override fun onGameInput(event: GameInputEvent) {
        if (event.action() != KeyAction.Press || selectionKey.get() !in event || mc.currentScreen != null)
            return

        selectCorners()
    }

    private fun selectCorners() {
        val lookingAt = (mc.crosshairTarget as? BlockHitResult)?.blockPos ?: run {
            warning("You're not looking at a block.")
            return
        }

        if (status == Status.SelStart) {
            status.runCommand(lookingAt)
            status.log(lookingAt)

            status = Status.SelEnd

        } else if (status == Status.SelEnd) {
            status.runCommand(lookingAt)
            status.log(lookingAt)

            baritone.commandManager.execute("sel cleararea")

            status = Status.InProgress
        }
    }

    @EventHandler
    private fun onRender3D(event: Render3DEvent) {
        status.onRender(event)
    }


    enum class Status(private val corner: Int? = null) {
        SelStart(1),
        SelEnd(2),
        InProgress;

        fun runCommand(blockPos: BlockPos) {
            if (hasCorner)
                baritone.commandManager.execute("sel $corner ${blockPos.x} ${blockPos.y} ${blockPos.z}")
        }

        private val hasCorner by invoking { this != InProgress }

        fun log(lookingAt: BlockPos) {
            if (hasCorner && logSelection())
                info("${name.drop(3)} corner set: (${lookingAt.x}, ${lookingAt.y}, ${lookingAt.z})")
        }

        fun onRender(event: Render3DEvent) {
            if (hasCorner) {
                if (mc.crosshairTarget !is BlockHitResult) return
                event.renderer.box((mc.crosshairTarget as BlockHitResult).blockPos, sideColor(), lineColor(), shapeMode(), 0)
            } else if (!hasCorner && !baritone.builderProcess.isActive)
                toggle()
        }
    }
}
