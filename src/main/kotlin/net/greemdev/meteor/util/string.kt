/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Strings")
@file:Suppress("NOTHING_TO_INLINE")

package net.greemdev.meteor.util

import net.greemdev.meteor.*
import java.net.URI
import java.util.*
import java.util.function.Consumer
import kotlin.io.path.Path


fun String.capitalize() = replaceFirstChar(Char::uppercaseChar)
fun String.decapitalize() = replaceFirstChar(Char::lowercaseChar)

fun String.forEachChar(action: Consumer<Char>) = forEach(action.kotlin)

fun String.toCamelCase(vararg separators: String = arrayOf("-"))
    = toPascalCase(*separators).decapitalize()

fun String.toPascalCase(vararg separators: String = arrayOf("-")) =
    split(*separators).joinToString(empty, transform = String::capitalize)

fun String.asPath() = Path(this)
fun String.asURI() = URI(this)

fun String.asUuidOrNull() =
    runCatching(UUID::fromString)
        .onFailureOf(IllegalArgumentException::class, Greteor.logger::catching)
        .getOrNull()



fun String.lines() = split("\n").toList()
inline fun String.forEachLine(action: ValueAction<String>) = split("\n").forEach(action)

inline fun String.widestLine(getWidth: Mapper<String, Double>) = lines().maxOf(getWidth)


fun String.lineCount(): Int {
    val count = count { it == '\n' }
    return if (endsWith('\n') && count == 1)
        1
    else
        count + 1 //0 newline literals makes one line, 1 literal makes 2 lines, etc
}


@JvmOverloads
fun String.ensurePrefix(prefix: String, ignoreCase: Boolean = false) =
    if (startsWith(prefix, ignoreCase))
        this
    else "$prefix$this"

@JvmOverloads
fun String.ensureSuffix(suffix: String, ignoreCase: Boolean = false) =
    if (endsWith(suffix, ignoreCase))
        this
    else "$this$suffix"

@JvmOverloads
fun String.pluralize(quantity: Number, plurality: Plurality = Plurality.Simple, prefixQuantity: Boolean = false) = string {
    if (prefixQuantity)
        +"$quantity "

    if (quantity == 1)
        +this@pluralize
    else
        +plurality.format(this@pluralize)
}

enum class Plurality(private val str: String, private val trimAmount: Int = 0) {
    Simple("s"),
    ES("es"),
    IES("ies", 1);

    fun format(word: String) =
        (if (trimAmount > 0) word.dropLast(trimAmount) else word) + this.str
}

private val emptyStr = String()
private const val singleSpaceStr = " "
private val singleSpaceChar = singleSpaceStr[0]

val String.Companion.empty
    get() = emptyStr

val String.Companion.singleSpace
    get() = singleSpaceStr

val Char.Companion.singleSpace
    get() = singleSpaceChar

@get:JvmName("empty")
val empty by invoking(::emptyStr)

inline fun String.minify() = replace(" ", empty).removeNewlines()

@JvmName("min") // String.minify() & minify(String) have the same JVM signature
inline fun minify(str: String) = str.minify()

inline fun String.removeNewlines() = replace("\n", empty)

@JvmName("withoutNewlines")
inline fun removeNewlines(str: String) = str.removeNewlines()

fun Char.string(): String = java.lang.String.valueOf(this)
fun Int.charStr() = toChar().string()

@Suppress("MemberVisibilityCanBePrivate") // API
data class StringScope(
    private val omitNulls: Boolean,
    private val inner: StringBuilder
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
    fun Any?.newline() = "${toString()}${this@StringScope.newline()}"
    fun newline(): String = System.lineSeparator()

    /**
     * Prepends new line to the string representation of the provided [value].
     */
    fun line(value: Any?) = "${newline()}${value.toString()}"

    fun lines(count: Number) = newline().repeat(count.toInt())

    fun append(content: Any?): StringScope {
        if ((content == null && !omitNulls) || content != null) {
            inner.append(content.toString())
        }

        return this
    }

    @JvmOverloads
    fun appendln(content: Any? = newline()) =
        append(content?.toString()?.ensureSuffix(newline()))

    fun appendlns(vararg content: Any? = arrayOf(newline())): StringScope {
        content.forEach(::appendln)
        return this
    }

    val currentString by invoking(inner::toString)
}

inline fun string(initial: CharSequence = empty, omitNulls: Boolean = false, builderScope: Initializer<StringScope>) =
    StringScope(omitNulls, StringBuilder(initial)).apply(builderScope).currentString


@JvmName("ktBuildString")
inline fun buildString(initialValue: String, builderAction: Initializer<StringBuilder>) =
    StringBuilder(initialValue).apply(builderAction).toString()

@JvmName("buildString")
fun `java-buildString`(initialValue: String, builderAction: Consumer<StringBuilder>) =
    StringBuilder(initialValue).apply(builderAction.kotlin).toString()
