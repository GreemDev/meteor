/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util


fun String.toCamelCase(separator: String = "-") =
    split(separator)
        .mapIndexed { i, part ->
            if (i == 0)
                part.lowercase()
            else
                part.replaceFirstChar { it.uppercase() }
        }.joinToString("")


fun String.ensurePrefix(prefix: String, ignoreCase: Boolean = false) =
    if (startsWith(prefix, ignoreCase))
        this
    else "$prefix$this"

fun String.withoutPrefix(prefix: String, ignoreCase: Boolean = false) =
    if (startsWith(prefix, ignoreCase))
        drop(prefix.length)
    else this

fun String.ensureSuffix(suffix: String, ignoreCase: Boolean = false) =
    if (endsWith(suffix, ignoreCase))
        this
    else "$this$suffix"

fun String.withoutSuffix(suffix: String, ignoreCase: Boolean = false) =
    if (endsWith(suffix, ignoreCase))
        dropLast(suffix.length)
    else this

fun String.pluralize(quantity: Number, useES: Boolean = false, prefixQuantity: Boolean = true) =
    if (quantity != 1) buildString {
        if (prefixQuantity) append("$quantity ")
        append(this@pluralize)
        if (useES) append('e')
        append('s')
    } else {
        if (prefixQuantity) "$quantity $this"
        else this
    }

data class StringScope(val inner: StringBuilder) : CharSequence by inner, Comparable<StringBuilder> by inner,
    java.io.Serializable by inner {

    inline operator fun <reified T> T?.unaryPlus() {
        inner.append(toString())
    }

    operator fun Array<Any?>.unaryPlus() {
        forEach { inner.append(it.toString()) }
    }

    operator fun Collection<Any?>.unaryPlus() {
        forEach { inner.append(it.toString()) }
    }

    /**
     * Appends new line to the current [Any]? value's string representation.
     */
    fun Any?.newline() = "${toString()}\n"
    fun newline() = '\n'

    /**
     * Prepends new line to the string representation of the provided [value].
     */
    fun line(value: Any?) = "\n${value.toString()}"

    fun append(content: Any?): StringScope {
        inner.append(content.toString())
        return this
    }

    fun appendln(content: Any? = "%JUSTNEWLINE%"): StringScope {
        if (content == "%JUSTNEWLINE%") {
            inner.appendLine()
        } else {
            inner.appendLine(content.toString())
        }
        return this
    }

    fun currentString() = inner.toString()
}

inline fun string(initial: CharSequence = "", builderScope: StringScope.() -> Unit): String {
    return StringScope(StringBuilder(initial)).apply(builderScope).currentString()
}

inline fun buildString(initialValue: String, builderAction: StringBuilder.() -> Unit): String {
    return StringBuilder(initialValue).apply(builderAction).toString()
}
