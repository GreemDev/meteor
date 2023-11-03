/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import net.greemdev.meteor.Initializer
import net.greemdev.meteor.MeteorColor
import net.greemdev.meteor.kotlin
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextContent
import java.util.function.Consumer

fun textOf(content: String?) = textOf(content, null)
fun emptyText() = textOf(null, null)
fun textOf(content: String? = null, block: Initializer<MutableText>?): MutableText {
    return Text.literal(content ?: return Text.empty())
        .apply { block?.invoke(this) }
}


inline fun buildText(initial: Text = emptyText(), block: FormattedTextBuilder.() -> Unit): Text {
    return FormattedTextBuilder(initial.copy()).apply(block).text()
}


object FormattedText {
    @JvmStatic
    fun colored(text: String, color: MeteorColor) = build(text) { it.colored(color) }
    @JvmStatic
    fun build(initial: Text, builder: Consumer<FormattedTextBuilder>) = builder(initial, builder).text()
    @JvmStatic
    fun build(builder: Consumer<FormattedTextBuilder>) = build(emptyText(), builder)
    @JvmStatic
    fun build(initial: String, builder: Consumer<FormattedTextBuilder>) = build(textOf(initial), builder)
    @JvmStatic
    fun builder() = FormattedTextBuilder(emptyText())
    @JvmStatic
    fun builder(initial: Any) = FormattedTextBuilder(
        when (initial) {
            is MutableText -> initial
            is TextContent -> MutableText.of(initial)
            is Text -> initial.copy()
            else -> textOf(initial.toString())
        }
    )

    private fun builder(initial: Text?, builder: Consumer<FormattedTextBuilder>) =
        FormattedTextBuilder(initial?.copy() ?: emptyText()).apply(builder.kotlin)
}
