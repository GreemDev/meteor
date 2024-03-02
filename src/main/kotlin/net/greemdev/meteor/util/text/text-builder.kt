/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate") // API
// see comment in http.kt for interop design scheme

package net.greemdev.meteor.util.text

import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.greemdev.meteor.parseHexColor
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import java.io.File
import java.nio.file.Path
import java.util.UUID
import java.util.function.Consumer
import kotlin.io.path.absolutePathString

@DslMarker
annotation class TextDsl

fun buildText(initial: Text = emptyText(), block: FormattedText.() -> Unit) =
    FormattedText(initial.copy(), block.java)


@TextDsl
open class FormattedText(internal: MutableText = emptyText()) : TextProxy(internal) {

    constructor(internal: MutableText = emptyText(), builder: Consumer<FormattedText>) : this(internal) {
        builder.accept(this)
    }

    constructor(internal: String, builder: Consumer<FormattedText>) : this(textOf(internal), builder)

    constructor(builder: Consumer<FormattedText>) : this(emptyText(), builder)

    fun copyText() = FormattedText(copy())


    fun addString(content: String?): FormattedText {
        internal = internal.append(content ?: return this)
        return this
    }

    /**
     * @throws IllegalArgumentException thrown when the provided [colorHex] is invalid
     */
    @Throws(IllegalArgumentException::class)
    fun addString(content: String?, colorHex: String) = addString(content, parseHexColor(colorHex))
    fun addString(content: String?, color: AwtColor): FormattedText {
        return addText(create(content ?: return this).colored(color))
    }

    fun addString(content: String?, color: MeteorColor): FormattedText {
        return addText(withColor(content ?: return this, color))
    }

    fun addString(content: String?, color: ChatColor): FormattedText {
        return addText(withColor(content ?: return this, color))
    }
    fun addString(content: String?, colors: Collection<ChatColor>): FormattedText {
        return addText(create(content ?: return this).colored(colors))
    }

    fun addString(content: String?, vararg colors: ChatColor): FormattedText {
        return addText(create(content ?: return this).colored(colors.toSet()))
    }

    fun addString(content: String?, vararg formatting: Formatting): FormattedText {
        return addText(create(content ?: return this).formatted(*formatting))
    }

    fun addString(content: String?, style: Style): FormattedText {
        return addText(textOf(content ?: return this).setStyle(style))
    }

    fun addGradientString(content: String?, gradient: Pair<MeteorColor, MeteorColor>): FormattedText {
        return addText(create(content ?: return this).gradient(gradient))
    }

    fun addGradientString(content: String?, firstColor: Int, secondColor: Int): FormattedText {
        return addText(create(content ?: return this).gradient(firstColor, secondColor))
    }

    @JvmOverloads
    fun addUrlHyperlink(
        content: String,
        url: String,
        hoverText: Text = textOf(url)
    ) = addText(urlHyperlink(content, url, hoverText))

    @JvmOverloads
    fun addFileHyperlink(
        content: String,
        url: String,
        hoverText: Text = textOf(url)
    ) = addText(fileHyperlink(content, url, hoverText))

    @JvmOverloads
    fun addFileHyperlink(
        content: String,
        file: File,
        hoverText: Text = textOf(file.path)
    ) = addFileHyperlink(content, file.path, hoverText)

    @JvmOverloads
    fun addFileHyperlink(
        content: String,
        path: Path,
        hoverText: Text = textOf(path.absolutePathString())
    ) = addFileHyperlink(content, path.absolutePathString(), hoverText)

    @JvmOverloads
    fun addCommandHyperlink(
        content: String,
        command: String,
        hoverText: Text = textOf(command)
    ) = addText(commandHyperlink(content, command, hoverText))

    @JvmOverloads
    fun addHyperlink(
        content: String,
        url: String,
        clickAction: ClickEvent.Action = actions.openURL,
        hoverText: Text = textOf(url)
    ) = addText(hyperlink(content, url, clickAction, hoverText))


    /**
     * When [content] is null, this doesn't do anything, allowing the use of nullability to choose whether to add [content] to the text builder.
     */
    @JvmName("ktAddText")
    fun addString(content: String?, builder: Initializer<FormattedText>): FormattedText {
        return addText(textOf(content ?: return this), builder)
    }

    @JvmName("ktAddText")
    fun addText(initial: Text, builder: Initializer<FormattedText>? = null): FormattedText {
        internal = internal.append(
            FormattedText(initial.copy())
                .apply { builder?.invoke(this) }
        )
        return this
    }

    @JvmName("ktAddText")
    fun addText(builder: Initializer<FormattedText>) =
        addText(emptyText(), builder)

    fun onHovered(event: HoverEvent) = styled { withHoverEvent(event) }
    fun onClick(event: ClickEvent) = styled { withClickEvent(event) }

    fun clicked(action: ClickAction, value: String) = onClick(net.greemdev.meteor.util.text.clicked(action, value))
    fun<T> hovered(action: HoverAction<T>, value: T) = onHovered(net.greemdev.meteor.util.text.hovered(action, value))

    fun hoveredText(text: String) =
        hoveredText(textOf(text))
    fun hoveredText(text: Text) =
        hovered(actions.showText, text)
    @JvmName("ktHoveredText")
    fun hoveredText(initial: Text = emptyText(), block: FormattedText.() -> Unit) =
        hoveredText(buildText(initial, block))

    fun hoveredItem(itemStack: ItemStack) =
        hovered(actions.showItem, HoverEvent.ItemStackContent(itemStack))

    fun hoveredEntity(entityType: EntityType<*>, uuid: UUID, name: Text? = entityType.name) =
        hovered(actions.showEntity, HoverEvent.EntityContent(entityType, uuid, name))


    fun gradient(firstColor: Int, secondColor: Int): FormattedText {
        internal = internal.applyGradient(firstColor, secondColor).copy()
        return this
    }

    fun gradient(firstColor: MeteorColor, secondColor: MeteorColor) = gradient(firstColor.packed, secondColor.packed)

    fun gradient(colors: Pair<MeteorColor, MeteorColor>) = gradient(colors.first, colors.second)

    fun formatted(vararg formatting: Formatting): FormattedText {
        internal = internal.formatted(*formatting)
        return this
    }

    fun colored(colors: Collection<ChatColor>) = formatted(*colors.map(ChatColor::mc).toTypedArray())
    fun colored(color: ChatColor) = formatted(color.mc)

    fun styled(styler: PipeOn<Style>): FormattedText {
        internal = internal.styled(styler)
        return this
    }

    fun colored(rgb: Int) = styled { withColor(rgb) }
    fun colored(color: AwtColor) = colored(color.rgb)
    fun colored(color: MeteorColor) = colored(color.packed)
    fun font(fontId: Identifier) = styled { withFont(fontId) }
    fun bold() = styled { withBold(!isBold) }
    fun italicized() = styled { withItalic(!isItalic) }
    fun underlined() = styled { withUnderline(!isUnderlined) }
    fun strikethrough() = styled { withStrikethrough(!isStrikethrough) }
    fun obfuscated() = styled { withObfuscated(!isObfuscated) }

    // ChatColor accumulator

    operator fun ChatColor.plus(color: ChatColor): List<ChatColor> = listOf(this, color)

    // Java Interop

    @JvmName("addText")
    @JvmOverloads
    fun `java-addText`(initial: Text = emptyText(), builder: Consumer<FormattedText>) =
        addText(initial, builder.kotlin)

    @JvmName("addText")
    fun `java-addText`(initial: Text) = addText(initial)

    @JvmName("add")
    fun `java-add`(content: Any?, builder: Consumer<FormattedText>) =
        addString(content.toString(), builder.kotlin)
    @JvmName("addString")
    fun `java-addString`(content: String, builder: Consumer<FormattedText>) =
        addString(content, builder.kotlin)

    @JvmName("hoveredText")
    @JvmOverloads
    fun `java-hoveredText`(initial: Text? = null, block: Consumer<FormattedText>) =
        hoveredText(create(initial ?: emptyText(), block))

    companion object {
        @JvmStatic
        fun styled(text: String, style: Style) = FormattedText().addString(text, style)

        @JvmStatic
        fun withColor(text: Any? = null, color: MeteorColor) = create(text).colored(color)

        @JvmStatic
        fun withColor(color: MeteorColor) = FormattedText().colored(color)

        @JvmStatic
        fun withColor(text: Any? = null, color: ChatColor) = create(text).colored(color)

        @JvmStatic
        fun withColor(color: ChatColor) = FormattedText().colored(color)

        @JvmStatic
        fun gradient(text: Text, firstColor: MeteorColor, secondColor: MeteorColor) =
            create(text).gradient(firstColor, secondColor)

        @JvmStatic
        fun gradient(text: String, firstColor: MeteorColor, secondColor: MeteorColor) =
            create(text).gradient(firstColor, secondColor)

        @JvmStatic
        fun create(initial: Text, builder: Consumer<FormattedText>) = buildText(initial, builder.kotlin)

        @JvmStatic
        fun create(initial: String, builder: Consumer<FormattedText>) =
            FormattedText(initial, builder)

        @JvmStatic
        @JvmOverloads
        fun hyperlink(
            content: String,
            url: String,
            clickAction: ClickEvent.Action = actions.openURL,
            hoverText: Text = textOf(url)
        ) = FormattedText(content) {
            it.colored(MeteorColor.HYPERLINK_BLUE).underlined()
            it.clicked(clickAction, url)
            it.hoveredText(hoverText)
        }

        @JvmStatic
        @JvmOverloads
        fun fileHyperlink(content: String,
                          filePath: String,
                          hoverText: Text = textOf(filePath)
        ) = hyperlink(content, filePath, actions.openFile, hoverText)

        @JvmStatic
        @JvmOverloads
        fun urlHyperlink(content: String,
                         url: String,
                         hoverText: Text = textOf(url)
        ) = hyperlink(content, url, actions.openURL, hoverText)

        @JvmStatic
        @JvmOverloads
        fun commandHyperlink(content: String,
                             command: String,
                             hoverText: Text = textOf(command)
        ) = hyperlink(content, command, actions.runCommand, hoverText)


        @JvmStatic
        fun create(initial: Any?) = FormattedText(
            when (initial) {
                is MutableText -> initial
                is TextContent -> MutableText.of(initial)
                is Text -> initial.copy()
                else -> textOf(initial?.toString())
            }
        )
    }
}

abstract class TextProxy(protected var internal: MutableText) : Text {
    override fun copy(): MutableText = internal.copy()

    override fun asOrderedText(): OrderedText = internal.asOrderedText()

    override fun getStyle(): Style = internal.style

    override fun getContent(): TextContent = internal.content

    override fun getSiblings(): List<Text> = internal.siblings
}
