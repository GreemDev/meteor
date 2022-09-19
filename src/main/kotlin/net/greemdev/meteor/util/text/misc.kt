/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.function.Consumer

fun textOf(content: String?) = textOf(content, null)
fun textOf() = textOf(null, null)
fun textOf(content: String? = null, block: (MutableText.() -> Unit)?): MutableText = if (content == null)
    Text.empty()
else
    Text.literal(content).apply { block?.invoke(this) }

fun buildText(initial: MutableText = textOf(), block: context(ChatColor.Companion) FormattedTextBuilder.() -> Unit): Text = FormattedTextBuilder(initial).apply {
    block(ChatColor, this@apply)
}.getMutableText()

object FormattedText {
    @JvmStatic
    fun build(builder: Consumer<FormattedTextBuilder>): MutableText = build(null, builder)
    @JvmStatic
    fun build(initial: MutableText?, builder: Consumer<FormattedTextBuilder>): MutableText = builder(initial, builder).getMutableText()
    @JvmStatic
    fun build(initial: String, builder: Consumer<FormattedTextBuilder>): MutableText = builder(textOf(initial), builder).getMutableText()
    @JvmStatic
    fun builder(): FormattedTextBuilder = builder(null) {}
    @JvmStatic
    fun builder(initial: Any): FormattedTextBuilder = builder(textOf(initial.toString())) {}
    @JvmStatic
    fun builder(initial: MutableText?, builder: Consumer<FormattedTextBuilder>): FormattedTextBuilder = FormattedTextBuilder(initial ?: textOf()).apply(builder::accept)
}
