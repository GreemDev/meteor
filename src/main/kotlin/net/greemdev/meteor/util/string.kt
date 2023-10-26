/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Strings")

package net.greemdev.meteor.util

import net.greemdev.meteor.invoking
import net.greemdev.meteor.kotlin
import java.net.URI
import java.util.*
import java.util.function.Consumer
import kotlin.io.path.Path


fun String.toCamelCase(separator: String = "-") =
    toPascalCase(separator).replaceFirstChar { it.lowercase() }

fun String.toPascalCase(separator: String = "-") =
    split(separator).joinToString("") {
        it.replaceFirstChar { ch -> ch.uppercase() }
    }

fun String.asPath() = Path(this)
fun String.asURI() = URI(this)

fun String.asUuidOrNull() = net.greemdev.meteor.getOrNull { UUID.fromString(this) }

@JvmOverloads
fun String.ensurePrefix(prefix: String, ignoreCase: Boolean = false) =
    if (startsWith(prefix, ignoreCase))
        this
    else "$prefix$this"

fun String.lines() = split("\n").toList()
inline fun String.forEachLine(action: (String) -> Unit) = split("\n").forEach(action)

inline fun String.widestLine(getWidth: (String) -> Double) = lines().maxOf { getWidth(it) }


fun String.lineCount(): Int {
    return if (endsWith('\n'))
        1
    else
        count { it == '\n' } + 1 //0 newline literals makes one line, 1 newline makes 2 lines, etc
}

@JvmOverloads
fun String.ensureSuffix(suffix: String, ignoreCase: Boolean = false) =
    if (endsWith(suffix, ignoreCase))
        this
    else "$this$suffix"

@JvmOverloads
fun String.pluralize(quantity: Number, useES: Boolean = false, prefixQuantity: Boolean = true) = string {
    if (prefixQuantity)
        +"$quantity "

    +this@pluralize

    if (quantity != 1) {
        if (useES)
            +'e'
        +'s'
    }
}


data class StringScope(
    private val appendNullLiteral: Boolean,
    val inner: StringBuilder
) : CharSequence by inner, Comparable<StringBuilder> by inner, java.io.Serializable by inner {
    operator fun Any?.unaryPlus() {
        append(this)
    }

    operator fun Array<Any?>.unaryPlus() {
        forEach { append(it) }
    }

    operator fun Collection<Any?>.unaryPlus() {
        forEach { append(it) }
    }

    /**
     * Appends new line to the current [Any]? value's string representation.
     */
    fun Any?.newline() = "${toString()}${System.lineSeparator()}"
    fun newline(): String = System.lineSeparator()

    /**
     * Prepends new line to the string representation of the provided [value].
     */
    fun line(value: Any?) = "${newline()}${value.toString()}"

    fun lines(count: Number) = newline().repeat(count.toInt())

    fun append(content: Any?): StringScope {
        if ((content == null && appendNullLiteral) || content != null) {
            inner.append(content)
        }

        return this
    }

    fun appendln(content: Any? = newline()) =
        append(content?.toString()?.ensureSuffix(newline()))

    fun appendlns(vararg content: Any?): StringScope {
        content.forEach(::appendln)
        return this
    }

    val currentString by invoking(inner::toString)
}

inline fun string(initial: CharSequence = "", appendNullLiteral: Boolean = true, builderScope: StringScope.() -> Unit) =
    StringScope(appendNullLiteral, StringBuilder(initial)).apply(builderScope).currentString


@JvmName("buildStringKt")
inline fun buildString(initialValue: String, builderAction: StringBuilder.() -> Unit) =
    StringBuilder(initialValue).apply(builderAction).toString()

@JvmName("buildString")
fun `buildStringJava`(initialValue: String, builderAction: Consumer<StringBuilder>) =
    StringBuilder(initialValue).apply(builderAction.kotlin).toString()
