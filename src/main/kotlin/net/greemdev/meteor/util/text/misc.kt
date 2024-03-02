/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import net.greemdev.meteor.Initializer
import net.greemdev.meteor.util.charStr
import net.minecraft.text.*

fun textOf(content: String?) = textOf(content, null)
fun emptyText() = textOf(null, null)
fun textOf(content: String? = null, block: Initializer<MutableText>?): MutableText =
    content?.let {
        Text.literal(content)
            .apply { block?.invoke(this) }
    } ?: Text.empty()



operator fun Style?.invoke(): Style = this ?: Style()
fun Style(): Style = Style.EMPTY

fun Text.applyGradient(from: Int, to: Int): Text {
    val length = string.length
    val fromR = (from.shr(16) and 0xFF) / 255f
    val fromG = (from.shr(8) and 0xFF) / 255f
    val fromB = (from and 0xFF) / 255f
    val toR = (to.shr(16) and 0xFF) / 255f
    val toG = (to.shr(8) and 0xFF) / 255f
    val toB = (to and 0xFF) / 255f

    return buildText {
        asOrderedText().acceptPassive { index, style, codePoint ->
            val f = (index / (length - 1)).toFloat()
            addString(
                codePoint.charStr(),
                style().withColor(
                    ((fromR + f * (toR - fromR)) * 255).toInt().shl(16) or
                        ((fromG + f * (toG - fromG)) * 255).toInt().shl(8) or
                        ((fromB + f * (toB - fromB)) * 255).toInt()
                )
            )
        }
    }
}

fun OrderedText.acceptPassive(
    visitor: (index: Int, Style?, codePoint: Int) -> Unit
) = accept { index, style, codePoint -> visitor(index, style, codePoint); true }
