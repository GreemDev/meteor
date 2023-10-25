/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.tabs.Tab
import meteordevelopment.meteorclient.gui.tabs.TabScreen
import meteordevelopment.meteorclient.gui.tabs.Tabs
import meteordevelopment.meteorclient.gui.widgets.WTopBar
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.*
import net.greemdev.meteor.util.meteor.renderOrder
import net.greemdev.meteor.util.minecraft
import org.lwjgl.glfw.GLFW.glfwSetCursorPos

class WRoundedTopBar : WTopBar(), RoundedWidget {
    override fun getButtonColor(pressed: Boolean, hovered: Boolean) = theme().backgroundColor(pressed, hovered)
    override fun getNameColor(): Color = theme().textColor()

    override fun init() {
        clear()
        val (tabs, iconTabs) = Tabs.get().renderOrder()

        tabs.forEach { add(WRoundedTopBarButton(it)) }

        if (tabs.isNotEmpty())
            add(theme.verticalSeparator(true)).expandWidgetY()

        iconTabs.forEach { add(WRoundedTopBarButton(it)) }
    }

    private inner class WRoundedTopBarButton(val tab: Tab) : WPressable() {
        private val state by invoking {
            var a = 0
            if (this == cells.first().widget())
                a = a or 1
            if (this == cells.last().widget())
                a = a or 2
            a
        }

        override fun onCalculateSize() {
            val pad = pad()

            width = if (tab.displayIcon.get())
                pad + theme.textHeight() + pad
            else
                pad + theme.textWidth(tab.name) + pad

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
            val color = getButtonColor(
                pressed || ((minecraft.currentScreen as? TabScreen)?.tab == tab),
                mouseOver
            )

            when (state) {
                1 -> renderer.roundRenderer2D.widgetQuadSide(this, color, theme().round(), false)
                2 -> renderer.roundRenderer2D.widgetQuadSide(this, color, theme().round(), true)
                3 -> renderer.roundRenderer2D.widgetQuad(this, color, theme().round())
                else -> renderer.quad(this, color)
            }

            if (tab.displayIcon.get())
                renderer.quad(
                    x + pad,
                    y + pad,
                    theme.textHeight(),
                    theme.textHeight(),
                    tab.icon,
                    nameColor
                )
            else
                renderer.text(tab.name, x + pad, y + pad, nameColor, false)
        }
    }
}
