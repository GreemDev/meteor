/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package net.greemdev.meteor.util.text

import net.greemdev.meteor.Initializer
import net.greemdev.meteor.Visitor
import net.greemdev.meteor.util.*
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.greemdev.meteor.parseHexColor
import java.util.function.Consumer

class FormattedTextBuilder(private var internal: MutableText) {
    fun styled(styler: Visitor<Style>): FormattedTextBuilder {
        internal = internal.styled(styler)
        return this
    }

    fun formatted(vararg formatting: Formatting): FormattedTextBuilder {
        internal = internal.formatted(*formatting)
        return this
    }

    fun text() = internal

    operator fun MutableList<ChatColor>.plus(color: ChatColor): MutableList<ChatColor> = apply { add(color) }
    operator fun ChatColor.plus(color: ChatColor): MutableList<ChatColor> = mutableListOf(this, color)

    fun addString(content: String): FormattedTextBuilder {
        internal = internal.append(content)
        return this
    }

    @JvmOverloads
    fun newline(amount: Int = 1): FormattedTextBuilder = addString("\n".repeat(amount.coerceAtLeast(1)))

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

    fun addString(content: String, builder: Initializer<FormattedTextBuilder>): FormattedTextBuilder =
        addText(textOf(content), builder)
    @JvmOverloads
    fun addText(initial: MutableText, builder: Initializer<FormattedTextBuilder>? = null): FormattedTextBuilder {
        internal = internal.append(FormattedTextBuilder(initial).apply { builder?.invoke(this) }.text())
        return this
    }

    fun addText(builder: Initializer<FormattedTextBuilder>) =
        addText(emptyText(), builder)
    fun addText(builder: Consumer<FormattedTextBuilder>) =
        addText { builder.accept(this) }
    fun addBuilder(builder: FormattedTextBuilder): FormattedTextBuilder {
        internal = internal.append(builder.text())
        return this
    }
    fun add(content: Any?, builder: Consumer<FormattedTextBuilder>) =
        addString(content.toString()) { builder.accept(this) }
    fun addString(content: String, builder: Consumer<FormattedTextBuilder>) =
        addString(content) { builder.accept(this) }
    fun onHovered(event: HoverEvent) = styled { it.withHoverEvent(event) }
    fun onClick(event: ClickEvent) = styled { it.withClickEvent(event) }

    fun clicked(action: ClickAction, value: String) = onClick(net.greemdev.meteor.util.text.clicked(action, value))
    fun<T> hovered(action: HoverAction<T>, value: T) = onHovered(net.greemdev.meteor.util.text.hovered(action, value))
    fun font(fontId: Identifier) = styled { it.withFont(fontId) }
    fun colored(rgb: Int) = styled { it.withColor(rgb) }
    fun colored(colors: Collection<ChatColor>) = formatted(*colors.map(ChatColor::mc).toTypedArray())
    fun colored(color: java.awt.Color) = colored(color.rgb)
    fun colored(color: ChatColor) = formatted(color.mc)
    fun colored(color: meteordevelopment.meteorclient.utils.render.color.Color) = colored(color.packed)
    fun bold() = styled { it.withBold(!it.isBold) }
    fun italicized() = styled { it.withItalic(!it.isItalic) }
    fun underlined() = styled { it.withUnderline(!it.isUnderlined) }
    fun strikethrough() = styled { it.withStrikethrough(!it.isStrikethrough) }
    fun obfuscated() = styled { it.withObfuscated(!it.isObfuscated) }
}
