/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.input

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.utils.CharFilter
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.gui.theme.round.widget.WRoundedLabel
import net.greemdev.meteor.util.misc.clamp
import net.greemdev.meteor.invoke

class WRoundedTextBox(text: String, placeholder: String?, filter: CharFilter, renderer: Class<out Renderer>?)
    : WTextBox(text, placeholder, filter, renderer), RoundedWidget {

    private var cursorVisible = false
    private var cursorTimer = 0.0
    private var animProgress = 0.0

    override fun onCursorChanged() {
        cursorVisible = true
        cursorTimer = 0.0
    }

    override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
        if (cursorTimer >= 1.0) {
            cursorVisible = !cursorVisible
            cursorTimer = 0.0
        } else {
            cursorTimer += delta * 1.1
        }

        renderer.roundedBackground(this, pressed = false, mouseOver = false)

        val theme = roundedTheme()
        val pad = pad()
        val overflowW = overflowWidthForRender

        renderer.scissorStart(x + pad, y + pad, width - pad * 2, height - pad * 2)

        // Text content
        if (text.isNotEmpty())
            this.renderer.render(renderer, x + pad - overflowW, y + pad, text, theme.textColor())
        else if (placeholder != null)
            this.renderer.render(renderer, x + pad - overflowW, y + pad, placeholder, theme.placeholderColor())

        // Text highlighting
        if (focused and (cursor != selectionStart || cursor != selectionEnd)) {
            val selStart = x + pad + getTextWidth(selectionStart) - overflowW
            val selEnd = x + pad + getTextWidth(selectionEnd) - overflowW

            renderer.quad(selStart, y + pad, selEnd - selStart, theme.textHeight(), theme.textHighlightColor())
        }

        // Cursor
        animProgress += delta * 10 * (if (focused and cursorVisible) 1 else -1)
        animProgress = animProgress.clamp(0, 1)

        if ((focused and cursorVisible) || animProgress > 0.0) {
            renderer.setAlpha(animProgress)
            renderer.quad(x + pad + getTextWidth(cursor) - overflowW, y + pad, theme.scalar(), theme.textHeight(), theme.textColor())
            renderer.setAlpha(1.0)
        }

        renderer.scissorEnd()
    }

    override fun createCompletionsRootWidget(): WVerticalList = object : WVerticalList(), RoundedWidget {
        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            val theme = roundedTheme()
            val s = theme.scale(2.0)
            val c = theme.outlineColor()

            renderer.roundedBackground(this, pressed = false, mouseOver = false)

            val col = theme.backgroundColor()
            val preA = col.a
            col.a /= 2
            col.validate()

            renderer.quad(this, col)
            col.a = preA

            renderer.quad(x, y + height - s, width, s, c)
            renderer.quad(x, y, s, height - s, c)
            renderer.quad(x + width - s, y, s, height - s, c)
        }
    }

    override fun <T> createCompletionsValueWidth(
        completion: String,
        selected: Boolean
    ): T where T : WWidget, T : ICompletionItem {
        @Suppress("UNCHECKED_CAST")
        return CompletionItem(completion, false, selected) as T
    }

    private class CompletionItem(text: String, title: Boolean, private var selected: Boolean) : WRoundedLabel(text, title), ICompletionItem {
        private val selectedColor = Color(255, 255, 255, 15)

        override fun onRender(renderer: GuiRenderer, mouseX: Double, mouseY: Double, delta: Double) {
            super.onRender(renderer, mouseX, mouseY, delta)
            if (selected)
                renderer.quad(this, selectedColor)
        }

        override fun isSelected() = selected
        override fun setSelected(selected: Boolean) {
            this.selected = selected
        }
        override fun getCompletion() = text
    }
}
