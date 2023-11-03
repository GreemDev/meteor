/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("FunctionName")
// see comment in Http.kt for interop design scheme

package net.greemdev.meteor.util.text

import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.greemdev.meteor.parseHexColor
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import org.jetbrains.annotations.Range
import java.util.UUID
import java.util.function.Consumer

class FormattedTextBuilder(private var internal: MutableText) {
    fun text(): Text = internal
    fun mutableText() = internal

    operator fun MutableList<ChatColor>.plus(color: ChatColor): MutableList<ChatColor> = apply { add(color) }
    operator fun ChatColor.plus(color: ChatColor): MutableList<ChatColor> = mutableListOf(this, color)

    fun addString(content: String?): FormattedTextBuilder {
        internal = internal.append(content ?: return this)
        return this
    }

    @JvmOverloads
    fun newline(amount: Int = 1) =
        addString("\n".repeat(amount.coerceAtLeast(1)))

    /**
     * @throws IllegalArgumentException thrown when the provided [colorHex] is invalid
     */
    @Throws(IllegalArgumentException::class)
    fun addString(content: String?, colorHex: String) =
        addString(content, parseHexColor(colorHex))
    fun addString(content: String?, color: AwtColor) =
        addString(content) { colored(color) }

    fun addString(content: String?, color: ChatColor) =
        addString(content) { colored(color) }
    fun addString(content: String?, colors: Collection<ChatColor>) =
        addString(content) { colored(colors) }

    fun addString(content: String?, vararg colors: ChatColor) =
        addString(content) { colored(colors.toSet()) }

    fun addString(content: String?, vararg formatting: Formatting) =
        addString(content) { formatted(*formatting) }
    fun addString(content: String?, color: MeteorColor) =
        addString(content) { colored(color) }


    /**
     * When [content] is null, this doesn't do anything, allowing the use of nullability to choose whether to add [content] to the text builder.
     */
    @JvmName("ktAddText")
    fun addString(content: String?, builder: Initializer<FormattedTextBuilder>): FormattedTextBuilder {
        return addText(textOf(content ?: return this), builder)
    }

    @JvmName("ktAddText")
    fun addText(initial: Text, builder: Initializer<FormattedTextBuilder>? = null): FormattedTextBuilder {
        internal = internal.append(
            FormattedTextBuilder(initial.copy())
                .apply { builder?.invoke(this) }
                .text()
        )
        return this
    }

    @JvmName("ktAddText")
    fun addText(builder: Initializer<FormattedTextBuilder>) =
        addText(emptyText(), builder)

    @JvmName("addText")
    @JvmOverloads
    fun `java-addText`(initial: Text = emptyText(), builder: Consumer<FormattedTextBuilder>) =
        addText(initial, builder.kotlin)

    @JvmName("addText")
    fun `java-addText`(initial: Text) = addText(initial)
    fun addBuilder(builder: FormattedTextBuilder): FormattedTextBuilder {
        internal = internal.append(builder.text())
        return this
    }
    @JvmName("add")
    fun `java-add`(content: Any?, builder: Consumer<FormattedTextBuilder>) =
        addString(content.toString(), builder.kotlin)
    @JvmName("addString")
    fun `java-addString`(content: String, builder: Consumer<FormattedTextBuilder>) =
        addString(content, builder.kotlin)
    fun onHovered(event: HoverEvent) = styled { withHoverEvent(event) }
    fun onClick(event: ClickEvent) = styled { withClickEvent(event) }

    fun clicked(action: ClickAction, value: String) = onClick(net.greemdev.meteor.util.text.clicked(action, value))
    fun<T> hovered(action: HoverAction<T>, value: T) = onHovered(net.greemdev.meteor.util.text.hovered(action, value))

    fun hoveredText(text: Text) =
        hovered(actions.showText, text)
    @JvmName("ktHoveredText")
    fun hoveredText(initial: Text = emptyText(), block: FormattedTextBuilder.() -> Unit) =
        hoveredText(buildText(initial, block))

    @JvmName("hoveredText")
    @JvmOverloads
    fun `java-hoveredText`(initial: Text? = null, block: Consumer<FormattedTextBuilder>) =
        hoveredText(FormattedText.build(initial ?: emptyText(), block))

    fun hoveredItem(itemStack: ItemStack) =
        hovered(actions.showItem, HoverEvent.ItemStackContent(itemStack))

    fun hoveredEntity(entityType: EntityType<*>, uuid: UUID, name: Text?) =
        hovered(actions.showEntity, HoverEvent.EntityContent(entityType, uuid, name))

    fun styled(styler: VisitorOn<Style>): FormattedTextBuilder {
        internal = internal.styled(styler)
        return this
    }

    fun formatted(vararg formatting: Formatting): FormattedTextBuilder {
        internal = internal.formatted(*formatting)
        return this
    }

    fun colored(colors: Collection<ChatColor>) = formatted(*colors.map(ChatColor::mc).toTypedArray())
    fun colored(color: AwtColor) = colored(color.rgb)
    fun colored(color: ChatColor) = formatted(color.mc)
    fun colored(color: MeteorColor) = colored(color.packed)
    fun font(fontId: Identifier) = styled { withFont(fontId) }
    fun colored(rgb: Int) = styled { withColor(rgb) }
    fun bold() = styled { withBold(!isBold) }
    fun italicized() = styled { withItalic(!isItalic) }
    fun underlined() = styled { withUnderline(!isUnderlined) }
    fun strikethrough() = styled { withStrikethrough(!isStrikethrough) }
    fun obfuscated() = styled { withObfuscated(!isObfuscated) }
}
