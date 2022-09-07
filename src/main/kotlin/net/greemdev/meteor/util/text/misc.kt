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


fun buildText(initial: MutableText = textOf(), block: FormattedTextBuilder.() -> Unit): Text = textBuilder(initial).apply(block).mutableText()
fun textBuilder(initial: MutableText = textOf()): FormattedTextBuilder = FormattedTextBuilder(initial)

object FormattedText {
    @JvmStatic
    fun build(builder: Consumer<FormattedTextBuilder>): MutableText = build(null, builder)
    @JvmStatic
    fun build(initial: MutableText?, builder: Consumer<FormattedTextBuilder>): MutableText = builder(initial, builder).mutableText()
    @JvmStatic
    fun build(initial: String, builder: Consumer<FormattedTextBuilder>): MutableText = builder(textOf(initial), builder).mutableText()
    @JvmStatic
    fun builder(): FormattedTextBuilder = builder(null) {}
    @JvmStatic
    fun builder(initial: MutableText?, builder: Consumer<FormattedTextBuilder>): FormattedTextBuilder = textBuilder(initial ?: textOf()).apply { builder.accept(this) }
}
