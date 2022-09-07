/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package net.greemdev.meteor.util.text

import net.greemdev.meteor.util.*
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.*
import net.minecraft.util.Identifier
import java.util.function.Consumer

class FormattedTextBuilder(private var internal: MutableText) {
    fun styled(styler: Style.() -> Style): FormattedTextBuilder {
        internal = internal.styled(styler)
        return this
    }

    fun formatted(vararg formatting: Formatting): FormattedTextBuilder {
        internal = internal.formatted(*formatting)
        return this
    }

    fun mutableText() = internal

    /*fun append(content: String, prefixing: Formatting, formatting: Array<out Formatting>, resetAtEnd: Boolean = false): FormattedTextBuilder {
        internal = internal.append(textOf(content) { formatted(prefixing, *formatting) })
        if (resetAtEnd)
            internal = internal.append(textOf().formatted(RESET))

        return this
    }*/

    fun append(content: String): FormattedTextBuilder {
        internal = internal.append(content)
        return this
    }

    /**
     * @throws IllegalArgumentException thrown when the provided [colorHex] is invalid
     */
    @Throws(IllegalArgumentException::class)
    fun append(content: String, colorHex: String) = append(content, parseHexColor(colorHex))
    fun append(content: String, color: java.awt.Color) = appendColored(textOf(content), color.rgb)
    fun append(content: String, vararg formatting: Formatting) = append(content) {
        formatted(*formatting)
    }
    fun append(content: String, color: meteordevelopment.meteorclient.utils.render.color.Color) = appendColored(textOf(content), color.packed)

    private fun appendColored(content: MutableText, rgb: Int) = append(content) {
        styled { withColor(rgb) }
    }

    fun append(content: String, builder: FormattedTextBuilder.() -> Unit): FormattedTextBuilder = append(textOf(content), builder)
    fun append(initial: MutableText, builder: FormattedTextBuilder.() -> Unit): FormattedTextBuilder {
        internal = internal.append(FormattedTextBuilder(initial).apply(builder).mutableText())
        return this
    }
    fun append(builder: FormattedTextBuilder.() -> Unit): FormattedTextBuilder = append(builder = builder)
    fun append(builder: Consumer<FormattedTextBuilder>) = append { builder.accept(this) }
    fun append(content: String, builder: Consumer<FormattedTextBuilder>) = append(content) { builder.accept(this) }

    fun onHovered(event: HoverEvent) = styled { withHoverEvent(event) }
    fun onClick(event: ClickEvent) = styled { withClickEvent(event) }
    fun font(fontId: Identifier) = styled { withFont(fontId) }
    fun colored(rgb: Int) = styled { withColor(rgb) }
    fun colored(color: java.awt.Color) = colored(color.rgb)
    fun colored(color: meteordevelopment.meteorclient.utils.render.color.Color) = colored(color.packed)
    fun bold() = styled { withBold(!isBold) }
    fun italicized() = styled { withItalic(!isItalic) }
    fun underlined() = styled { withUnderline(!isUnderlined) }
    fun strikethrough() = styled { withStrikethrough(!isStrikethrough) }
    fun obfuscated() = styled { withObfuscated(!isObfuscated) }
}
