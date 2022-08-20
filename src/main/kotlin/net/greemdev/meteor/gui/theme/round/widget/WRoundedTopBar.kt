/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.tabs.Tab
import meteordevelopment.meteorclient.gui.widgets.WTopBar
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.gui.tabs.TabScreen
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.tabs.Tabs
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.gui.theme.round.util.*
import net.greemdev.meteor.util.invoke
import net.greemdev.meteor.util.minecraft
import org.lwjgl.glfw.GLFW.glfwSetCursorPos


class WRoundedTopBar : WTopBar(), RoundedWidget {
    override fun getButtonColor(pressed: Boolean, hovered: Boolean) = theme().backgroundColor.get(pressed, hovered)
    override fun getNameColor(): Color = theme().textColor()

    override fun init() {
        Tabs.get().forEach { add(WTopBarButton(it)) }
    }

    private fun buttonState(button: WTopBarButton): Int {
        var a = 0
        if (button == cells.first().widget())
            a = a or 1
        if (button == cells.last().widget())
            a = a or 2
        return a
    }
    private inner class WTopBarButton(val tab: Tab) : WPressable() {
        override fun onCalculateSize() {
            val pad = pad()

            width = pad + theme.textWidth(tab.name) + pad
            height = pad + theme.textHeight() + pad
        }

        override fun onPressed(button: Int) {
            val screen = minecraft.currentScreen

            if (screen !is TabScreen || screen.tab != tab) {
                tab.openScreen(theme)
                glfwSetCursorPos(minecraft.window.handle, minecraft.mouse.x, minecraft.mouse.y)
            }
        }

        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            val pad = pad()
            val color = getButtonColor(pressed || (minecraft.currentScreen is TabScreen && (minecraft.currentScreen as TabScreen).tab == tab), mouseOver)

            when (buttonState(this)) {
                1 -> renderer.quadRoundedSide(this, color, theme().round(), false)
                2 -> renderer.quadRoundedSide(this, color, theme().round(), true)
                3 -> renderer.quadRounded(this, color, theme().round())
                else -> renderer.quad(this, color)
            }
            renderer.text(tab.name, x + pad, y + pad, nameColor, false)
        }
    }
}
