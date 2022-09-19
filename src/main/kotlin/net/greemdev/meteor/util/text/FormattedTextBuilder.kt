/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package net.greemdev.meteor.util.text

import net.greemdev.meteor.util.*
import net.minecraft.text.*
import net.minecraft.util.Formatting
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

    fun getMutableText() = internal

    operator fun MutableList<ChatColor>.plus(color: ChatColor): MutableList<ChatColor> = apply { add(color) }
    operator fun ChatColor.plus(color: ChatColor): MutableList<ChatColor> = mutableListOf(this, color)

    fun addString(content: String): FormattedTextBuilder {
        internal = internal.append(content)
        return this
    }

    fun newline() = newline(1)
    fun newline(amount: Int): FormattedTextBuilder = addString("\n".repeat(amount.coerceAtLeast(1)))

    /**
     * @throws IllegalArgumentException thrown when the provided [colorHex] is invalid
     */
    @Throws(IllegalArgumentException::class)
    fun addString(content: String, colorHex: String) =
        addString(content, parseHexColor(colorHex))
    fun addString(content: String, color: java.awt.Color) =
        addString(content) { colored(color) }

    fun addString(content: String, color: ChatColor) =
        addString(content) { colored(color) }
    fun addString(content: String, colors: Collection<ChatColor>) =
        addString(content) { colored(colors) }

    fun addString(content: String, vararg colors: ChatColor) =
        addString(content) { colored(colors.toSet()) }

    fun addString(content: String, vararg formatting: Formatting) =
        addString(content) { formatted(*formatting) }
    fun addString(content: String, color: meteordevelopment.meteorclient.utils.render.color.Color) =
        addText(textOf(content)) { colored(color) }

    fun addString(content: String, builder: FormattedTextBuilder.() -> Unit): FormattedTextBuilder =
        addText(textOf(content), builder)
    fun addText(initial: MutableText, builder: FormattedTextBuilder.() -> Unit): FormattedTextBuilder {
        internal = internal.append(FormattedTextBuilder(initial).apply(builder).getMutableText())
        return this
    }
    fun addText(builder: FormattedTextBuilder.() -> Unit) =
        addText(textOf(), builder)
    fun addText(builder: Consumer<FormattedTextBuilder>) =
        addText { builder.accept(this) }
    fun addBuilder(builder: FormattedTextBuilder): FormattedTextBuilder {
        internal = internal.append(builder.getMutableText())
        return this
    }
    fun add(content: Any?, builder: Consumer<FormattedTextBuilder>) =
        addString(content.toString()) { builder.accept(this) }
    fun addString(content: String, builder: Consumer<FormattedTextBuilder>) =
        addString(content) { builder.accept(this) }
    fun onHovered(event: HoverEvent) = styled { withHoverEvent(event) }
    fun onClick(event: ClickEvent) = styled { withClickEvent(event) }
    fun font(fontId: Identifier) = styled { withFont(fontId) }
    fun colored(rgb: Int) = styled { withColor(rgb) }
    fun colored(colors: Collection<ChatColor>) = formatted(*colors.map(ChatColor::mc).toTypedArray())
    fun colored(color: java.awt.Color) = colored(color.rgb)
    fun colored(color: ChatColor) = formatted(color.mc)
    fun colored(color: meteordevelopment.meteorclient.utils.render.color.Color) = colored(color.packed)
    fun bold() = styled { withBold(!isBold) }
    fun italicized() = styled { withItalic(!isItalic) }
    fun underlined() = styled { withUnderline(!isUnderlined) }
    fun strikethrough() = styled { withStrikethrough(!isStrikethrough) }
    fun obfuscated() = styled { withObfuscated(!isObfuscated) }
}
