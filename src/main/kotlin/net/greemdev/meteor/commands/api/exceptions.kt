/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.ImmutableStringReader
import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.*
import net.greemdev.meteor.invoking

object CommandExceptions {

    val builtIn: BuiltInExceptionProvider = CommandSyntaxException.BUILT_IN_EXCEPTIONS

    infix fun simple(message: Message) =
        lazy { SimpleCommandExceptionType(message) }

    infix fun dynamic(func: (Any) -> Message) =
        invoking { DynamicCommandExceptionType(func) }

    infix fun dynamic2(func: (Any, Any) -> Message) =
        invoking { Dynamic2CommandExceptionType(func) }

    infix fun dynamic3(func: (Any, Any, Any) -> Message) =
        invoking { Dynamic3CommandExceptionType(func) }

    infix fun dynamic4(func: (Any, Any, Any, Any) -> Message) =
        invoking { Dynamic4CommandExceptionType(func) }

    infix fun dynamicN(func: (Array<Any>) -> Message) =
        invoking { DynamicNCommandExceptionType(func) }


    @JvmName("dynamicTyped")
    inline fun<reified T : Any> dynamic(crossinline func: (T) -> Message) =
        dynamic { func(it.cast()) }

    @JvmName("dynamic2Typed")
    inline fun<reified T1 : Any, reified T2 : Any> dynamic2(crossinline func: (T1, T2) -> Message) =
        dynamic2 { it1, it2 ->
            func(it1.cast(), it2.cast())
        }

    @JvmName("dynamic3Typed")
    inline fun<reified T1 : Any, reified T2 : Any, reified T3 : Any> dynamic3(crossinline func: (T1, T2, T3) -> Message) =
        dynamic3 { it1, it2, it3 ->
            func(it1.cast(), it2.cast(), it3.cast())
        }

    @JvmName("dynamic4Typed")
    inline fun<reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> dynamic4(crossinline func: (T1, T2, T3, T4) -> Message) =
        dynamic4 { it1, it2, it3, it4 ->
            func(it1.cast(), it2.cast(), it3.cast(), it4.cast())
        }

    @JvmName("dynamicNTyped")
    inline fun<reified T : Any> dynamicN(crossinline func: (Array<T>) -> Message) =
        dynamicN {
            func(it.map<Any, T>(Any::cast).toTypedArray())
        }
}

inline fun<reified E> Any.cast() =
    if (this::class.java.canonicalName == E::class.java.canonicalName)
        this as E
    else error("Cannot create a typed command exception with argument of type ${E::class.simpleName} with provided value of type ${this::class.simpleName}")


fun CommandExceptionType.isKnown() =
    this is SimpleCommandExceptionType ||
        this is DynamicCommandExceptionType ||
        this is Dynamic2CommandExceptionType ||
        this is Dynamic3CommandExceptionType ||
        this is Dynamic4CommandExceptionType ||
        this is DynamicNCommandExceptionType

/**
 * When receiver is [SimpleCommandExceptionType]:
 *
 * -> [args] doesn't *need* to be empty, but it's unused.
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
fun CommandExceptionType.throwNew(vararg args: Any, readerContext: ImmutableStringReader? = null): Nothing {
    require(isKnown()) { "Unknown CommandExceptionType." }
    val exception = when (this) {
        is SimpleCommandExceptionType -> readerContext.new()
        is DynamicCommandExceptionType -> {
            require(args.size == 1) { "DynamicCommandExceptionType requires one argument." }

            readerContext.new(args.first())
        }
        is Dynamic2CommandExceptionType -> {
            require(args.size == 2) { "Dynamic2CommandExceptionType requires two arguments." }

            readerContext.new(args.first(), args.last())
        }
        is Dynamic3CommandExceptionType -> {
            require(args.size == 3) { "Dynamic3CommandExceptionType requires three arguments." }

            val arg1 = args.first()
            val arg2 = args[1]
            val arg3 = args.last()

            readerContext.new(arg1, arg2, arg3)
        }
        is Dynamic4CommandExceptionType -> {
            require(args.size == 4) { "Dynamic4CommandExceptionType requires four arguments." }

            val arg1 = args.first()
            val arg2 = args[1]
            val arg3 = args[2]
            val arg4 = args.last()
            readerContext.new(arg1, arg2, arg3, arg4)
        }
        is DynamicNCommandExceptionType -> {
            require(args.isNotEmpty()) { "DynamicNCommandExceptionType at least one argument." }

            readerContext.new(*args)
        }
        else -> null
    }

    throw exception!!
}

context(SimpleCommandExceptionType)
private fun ImmutableStringReader?.new() =
    if (this != null)
        createWithContext(this)
    else create()


context(DynamicCommandExceptionType)
private fun ImmutableStringReader?.new(arg: Any) =
    if (this != null)
        createWithContext(this, arg)
    else create(arg)

context(Dynamic2CommandExceptionType)
private fun ImmutableStringReader?.new(arg1: Any, arg2: Any) =
    if (this != null)
        createWithContext(this, arg1, arg2)
    else create(arg1, arg2)

context(Dynamic3CommandExceptionType)
private fun ImmutableStringReader?.new(arg1: Any, arg2: Any, arg3: Any) =
    if (this != null)
        createWithContext(this, arg1, arg2, arg3)
    else create(arg1, arg2, arg3)

context(Dynamic4CommandExceptionType)
private fun ImmutableStringReader?.new(arg1: Any, arg2: Any, arg3: Any, arg4: Any) =
    if (this != null)
        createWithContext(this, arg1, arg2, arg3, arg4)
    else create(arg1, arg2, arg3, arg4)

context(DynamicNCommandExceptionType)
private fun ImmutableStringReader?.new(vararg args: Any) =
    if (this != null)
        createWithContext(this, *args)
    else create(args.first(), args.drop(1))
