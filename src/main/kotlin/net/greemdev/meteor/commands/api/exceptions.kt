/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.ImmutableStringReader
import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.*
import net.greemdev.meteor.invoking
import net.greemdev.meteor.util.className
import net.greemdev.meteor.util.text.FormattedText
import net.greemdev.meteor.util.text.buildText
import net.greemdev.meteor.util.text.textOf

object CommandExceptions : BuiltInExceptionProvider by CommandSyntaxException.BUILT_IN_EXCEPTIONS {

    infix fun simple(message: String) =
        lazy { SimpleCommandExceptionType(textOf(message)) }

    infix fun simple(message: Message) =
        lazy { SimpleCommandExceptionType(message) }

    infix fun dynamic(func: FormattedText.(Any) -> Unit) =
        invoking { DynamicCommandExceptionType { buildText { func(it) } } }

    infix fun dynamic2(func: FormattedText.(Any, Any) -> Unit) =
        invoking { Dynamic2CommandExceptionType { it1, it2 -> buildText { func(it1, it2) } } }

    infix fun dynamic3(func: FormattedText.(Any, Any, Any) -> Unit) =
        invoking { Dynamic3CommandExceptionType { it1, it2, it3 -> buildText { func(it1, it2, it3) } } }

    infix fun dynamic4(func: FormattedText.(Any, Any, Any, Any) -> Unit) =
        invoking { Dynamic4CommandExceptionType { it1, it2, it3, it4 -> buildText { func(it1, it2, it3, it4) } } }

    infix fun dynamicN(func: FormattedText.(Array<Any>) -> Unit) =
        invoking { DynamicNCommandExceptionType { buildText { func(it) } } }


    @JvmName("dynamicTyped")
    inline fun<reified T : Any> dynamic(crossinline func: FormattedText.(T) -> Unit) =
        dynamic { func(__castarg__(it)) }

    @JvmName("dynamic2Typed")
    inline fun<reified T1 : Any, reified T2 : Any> dynamic2(crossinline func: FormattedText.(T1, T2) -> Unit) =
        dynamic2 { it1, it2 ->
            func(__castarg__(it1), __castarg__(it2))
        }

    @JvmName("dynamic3Typed")
    inline fun<reified T1 : Any, reified T2 : Any, reified T3 : Any> dynamic3(crossinline func: FormattedText.(T1, T2, T3) -> Unit) =
        dynamic3 { it1, it2, it3 ->
            func(__castarg__(it1), __castarg__(it2), __castarg__(it3))
        }

    @JvmName("dynamic4Typed")
    inline fun<reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> dynamic4(crossinline func: FormattedText.(T1, T2, T3, T4) -> Unit) =
        dynamic4 { it1, it2, it3, it4 ->
            func(__castarg__(it1), __castarg__(it2), __castarg__(it3), __castarg__(it4))
        }

    @JvmName("dynamicNTyped")
    inline fun<reified T : Any> dynamicN(crossinline func: FormattedText.(Array<T>) -> Unit) =
        dynamicN {
            func(it.map<Any, T>(::__castarg__).toTypedArray())
        }
}

@Suppress("FunctionName", "SpellCheckingInspection") //i want this function to be undesirable to call
// it's only intended for the above functions, but NEEDS to be public due to those functions being inline, and they NEED to be an inline function due to typearg reification
inline fun<reified E> __castarg__(arg: Any) =
    if (arg::class.java.canonicalName == E::class.java.canonicalName)
        arg as E
    else error("Cannot create a typed command exception with argument of type ${className<E>()} with provided value of type ${arg::class.simpleName}")


fun CommandExceptionType.isKnown() =
    this is SimpleCommandExceptionType
        || this is DynamicCommandExceptionType
        || this is Dynamic2CommandExceptionType
        || this is Dynamic3CommandExceptionType
        || this is Dynamic4CommandExceptionType
        || this is DynamicNCommandExceptionType

/**
 * When receiver is [SimpleCommandExceptionType]:
 *
 * -> it is [require]d that [args] contains exactly 0 elements.
 *
 * When receiver is [DynamicCommandExceptionType]:
 *
 * -> it is [require]d that [args] contains exactly one element.
 *
 * When receiver is [Dynamic2CommandExceptionType]:
 *
 * -> it is [require]d that [args] contains exactly two elements.
 *
 * When receiver is [Dynamic3CommandExceptionType]:
 *
 * -> it is [require]d that [args] contains exactly three elements.
 *
 * When receiver is [Dynamic4CommandExceptionType]:
 *
 * -> it is [require]d that [args] contains exactly four elements.
 *
 * When receiver is [DynamicNCommandExceptionType]:
 *
 * -> it is [require]d that [args] contains at least one element, with as many extra as you need.
 *
 * It is [require]d that the receiver be one of the 6 types described above, otherwise your CommandExceptionType isn't thrown and instead an [IllegalArgumentException] from [require] is thrown,
 * and due to how Brigadier works, it won't be caught automatically as it isn't a [CommandSyntaxException] and will thus be bubbled up.
 */
fun CommandExceptionType.throwNew(vararg args: Any, readerCtx: ImmutableStringReader? = null): Nothing {
    require(isKnown()) { "Unknown CommandExceptionType '${this::class.qualifiedName}'. Use a different type or add a when branch for that type in throwNew." }
    val exception = when (this) {
        is SimpleCommandExceptionType -> {
            require(args.isEmpty()) { "SimpleCommandExceptionType takes no arguments." }

            readerCtx.new()
        }
        is DynamicCommandExceptionType -> {
            require(args.size == 1) { "DynamicCommandExceptionType requires one argument." }

            readerCtx.new(args.first())
        }
        is Dynamic2CommandExceptionType -> {
            require(args.size == 2) { "Dynamic2CommandExceptionType requires two arguments." }

            readerCtx.new(args.first(), args.last())
        }
        is Dynamic3CommandExceptionType -> {
            require(args.size == 3) { "Dynamic3CommandExceptionType requires three arguments." }

            readerCtx.new(args.first(), args.second(), args.last())
        }
        is Dynamic4CommandExceptionType -> {
            require(args.size == 4) { "Dynamic4CommandExceptionType requires four arguments." }

            readerCtx.new(args.first(), args.second(), args.third(), args.last())
        }
        is DynamicNCommandExceptionType -> {
            require(args.isNotEmpty()) { "DynamicNCommandExceptionType at least one argument." }

            readerCtx.new(*args)
        }
        else -> null
    }

    throw exception!!
}

private fun<T> Array<T>.second() = this[1]
private fun<T> Array<T>.third() = this[2]

context(SimpleCommandExceptionType)
private fun ImmutableStringReader?.new() = this?.let { createWithContext(it) } ?: create()

context(DynamicCommandExceptionType)
private fun ImmutableStringReader?.new(arg: Any) = this?.let { createWithContext(it, arg) } ?: create(arg)

context(Dynamic2CommandExceptionType)
private fun ImmutableStringReader?.new(arg1: Any, arg2: Any) = this?.let { createWithContext(it, arg1, arg2) } ?: create(arg1, arg2)

context(Dynamic3CommandExceptionType)
private fun ImmutableStringReader?.new(arg1: Any, arg2: Any, arg3: Any) = this?.let { createWithContext(it, arg1, arg2, arg3) } ?: create(arg1, arg2, arg3)

context(Dynamic4CommandExceptionType)
private fun ImmutableStringReader?.new(arg1: Any, arg2: Any, arg3: Any, arg4: Any) = this?.let { createWithContext(it, arg1, arg2, arg3, arg4) } ?: create(arg1, arg2, arg3, arg4)

context(DynamicNCommandExceptionType)
private fun ImmutableStringReader?.new(vararg args: Any) = this?.let { createWithContext(it, *args) } ?: create(args.first(), args.drop(1))
