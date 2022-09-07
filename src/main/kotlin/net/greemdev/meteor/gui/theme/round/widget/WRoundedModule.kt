/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.utils.AlignmentX
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.clamp
import net.greemdev.meteor.util.meteor.*
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT


class WRoundedModule(private val module: Module) : WPressable(), RoundedWidget {

    private var titleWidth: Double = 0.0

    private var animationProgress1: Double
    private var animationProgress2: Double

    init {
        tooltip = module.description

        if (module.isActive) {
            animationProgress1 = 1.0
            animationProgress2 = 1.0
        } else {
            animationProgress1 = 0.0
            animationProgress2 = 0.0
        }
    }

    override fun pad() = theme.scale(4.0)

    override fun onCalculateSize() {
        val pad = pad()
        if (titleWidth == 0.0)
            titleWidth = theme.textWidth(module.title)

        width = pad + titleWidth + pad
        height = pad + theme.textHeight() + pad
    }

    override fun onPressed(button: Int) {
        when(button) {
            GLFW_MOUSE_BUTTON_LEFT -> module.toggle()
            GLFW_MOUSE_BUTTON_RIGHT -> minecraft.setScreen(theme.moduleScreen(module))
        }
    }

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        val theme = theme()
        val pad = pad()

        animationProgress1 += delta * 4 * if (module.isActive || mouseOver) 1 else -1
        animationProgress1 = animationProgress1.clamp(0, 1)


        animationProgress2 += delta * 6 * if (module.isActive) 1 else -1
        animationProgress2 = animationProgress2.clamp(0, 1)

        if (animationProgress1 > 0.0)
            renderer.quad(x, y, width * animationProgress1, height, theme.moduleBackground())

        if (animationProgress2 > 0)
            renderer.quad(x, y + height * (1 - animationProgress2), theme.scale(2.0), height * animationProgress2, theme.accentColor())

        var x = x + pad
        val w = width - pad * 2

        if (theme.moduleAlignment() == AlignmentX.Center)
            x += w / 2 - titleWidth / 2
        else if (theme.moduleAlignment() == AlignmentX.Right)
            x += w - titleWidth

        renderer.text(module.title, x, y + pad, theme.textColor(), false)

    }

}
