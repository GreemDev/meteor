/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import net.greemdev.meteor.ColoredInitializer
import net.greemdev.meteor.Initializer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.function.Consumer

fun textOf(content: String?) = textOf(content, null)
fun emptyText() = textOf(null, null)
fun textOf(content: String? = null, block: Initializer<MutableText>?): MutableText = if (content == null)
    Text.empty()
else
    Text.literal(content).apply { block?.invoke(this) }

fun buildText(initial: MutableText = emptyText(), block: ColoredInitializer<FormattedTextBuilder>): Text = FormattedTextBuilder(initial).apply {
    block(ChatColor, this@apply)
}.text()

object FormattedText {
    @JvmStatic
    fun build(builder: Consumer<FormattedTextBuilder>): MutableText = build(null, builder)
    @JvmStatic
    fun build(initial: MutableText?, builder: Consumer<FormattedTextBuilder>): MutableText = builder(initial, builder).text()
    @JvmStatic
    fun build(initial: String, builder: Consumer<FormattedTextBuilder>): MutableText = builder(textOf(initial), builder).text()
    @JvmStatic
    fun builder(): FormattedTextBuilder = builder(null) {}
    @JvmStatic
    fun builder(initial: Any): FormattedTextBuilder = builder(textOf(initial.toString())) {}
    @JvmStatic
    fun builder(initial: MutableText?, builder: Consumer<FormattedTextBuilder>): FormattedTextBuilder = FormattedTextBuilder(initial ?: emptyText()).apply(builder::accept)
}
