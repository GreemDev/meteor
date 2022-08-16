/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.*

fun text(content: String? = null, block: (MutableText.() -> Unit)? = null): Text = if (content == null)
    Text.empty()
else if (block == null)
    Text.of(content)
else
    Text.literal(content).apply(block)

fun textBuilder(initial: Text = Text.empty()): FormattedTextBuilder = FormattedTextBuilder(Text.empty().copy())
data class FormattedTextBuilder(private val internal: MutableText = Text.empty()) {

    fun withReset(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, RESET, formatting, resetAtEnd = resetAtEnd)
    fun withBlack(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, BLACK, formatting, resetAtEnd = resetAtEnd)
    fun withDarkBlue(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, DARK_BLUE, formatting, resetAtEnd = resetAtEnd)
    fun withDarkGreen(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, DARK_GREEN, formatting, resetAtEnd = resetAtEnd)
    fun withDarkAqua(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, DARK_AQUA, formatting, resetAtEnd = resetAtEnd)
    fun withDarkRed(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, DARK_RED, formatting, resetAtEnd = resetAtEnd)
    fun withDarkPurple(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, DARK_PURPLE, formatting, resetAtEnd = resetAtEnd)
    fun withDarkGrey(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, DARK_GRAY, formatting, resetAtEnd = resetAtEnd)
    fun withGold(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, GOLD, formatting, resetAtEnd = resetAtEnd)
    fun withGrey(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, GRAY, formatting, resetAtEnd = resetAtEnd)
    fun withBlue(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, BLUE, formatting, resetAtEnd = resetAtEnd)
    fun withGreen(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, GREEN, formatting, resetAtEnd = resetAtEnd)
    fun withAqua(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, AQUA, formatting, resetAtEnd = resetAtEnd)
    fun withRed(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, RED, formatting, resetAtEnd = resetAtEnd)
    fun withLightPurple(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, LIGHT_PURPLE, formatting, resetAtEnd = resetAtEnd)
    fun withYellow(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, YELLOW, formatting, resetAtEnd = resetAtEnd)
    fun withWhite(text: String, vararg formatting: Formatting, resetAtEnd: Boolean = false) = append(text, WHITE, formatting, resetAtEnd = resetAtEnd)

    fun append(content: String, prefixing: Formatting, formatting: Array<out Formatting>, resetAtEnd: Boolean = false): FormattedTextBuilder {
        internal.append(text(content) { formatted(prefixing, *formatting) })
        if (resetAtEnd)
            internal.append(Text.empty().formatted(RESET))

        return this
    }

    fun build(): Text = internal
}
