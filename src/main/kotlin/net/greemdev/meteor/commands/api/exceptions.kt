/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.ImmutableStringReader
import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.*
import net.greemdev.meteor.util.invoking
import kotlin.reflect.KClass

fun simpleCommandException(message: Message) = lazy { SimpleCommandExceptionType(message) }
fun dynamicCommandException(func: (Any) -> Message) = invoking { DynamicCommandExceptionType(func) }
fun dynamic2CommandException(func: (Any, Any) -> Message) = invoking { Dynamic2CommandExceptionType(func) }
fun dynamic3CommandException(func: (Any, Any, Any) -> Message) = invoking { Dynamic3CommandExceptionType(func) }
fun dynamic4CommandException(func: (Any, Any, Any, Any) -> Message) = invoking { Dynamic4CommandExceptionType(func) }
fun dynamicNCommandException(func: (List<Any>) -> Message) = invoking { DynamicNCommandExceptionType { func(it.toList()) } }


@Suppress("UNCHECKED_CAST")
fun<E : Any> valueOfOrError(value: Any, expectedType: KClass<E>): E {
    if (value::class.qualifiedName == expectedType.qualifiedName)
        return value as E
    else
        error("Cannot create a typed command exception of type ${expectedType.simpleName} with provided value of type ${value::class.simpleName}")
}

inline fun<reified T : Any> dynamicTypedCommandException(crossinline func: (T) -> Message) = dynamicCommandException {
    func(valueOfOrError(it, T::class))
}
inline fun<reified T1 : Any, reified T2 : Any> dynamic2TypedCommandException(crossinline func: (T1, T2) -> Message) = dynamic2CommandException { it1, it2 ->
    func(valueOfOrError(it1, T1::class), valueOfOrError(it2, T2::class))
}
inline fun<reified T1 : Any, reified T2 : Any, reified T3 : Any> dynamic3TypedCommandException(crossinline func: (T1, T2, T3) -> Message) = dynamic3CommandException { it1, it2, it3 ->
    func(valueOfOrError(it1, T1::class), valueOfOrError(it2, T2::class), valueOfOrError(it3, T3::class))
}
inline fun<reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> dynamic4TypedCommandException(crossinline func: (T1, T2, T3, T4) -> Message) = dynamic4CommandException { it1, it2, it3, it4 ->
    func(valueOfOrError(it1, T1::class), valueOfOrError(it2, T2::class), valueOfOrError(it3, T3::class), valueOfOrError(it4, T4::class))
}
inline fun<reified T : Any> dynamicNTypedCommandException(crossinline func: (List<T>) -> Message) = dynamicNCommandException { base ->
    func(base.map { valueOfOrError(it, T::class) })
}

fun SimpleCommandExceptionType.throwNew(readerContext: ImmutableStringReader? = null): Nothing =
    throw if (readerContext != null)
        createWithContext(readerContext)
    else create()

fun DynamicCommandExceptionType.throwNew(arg: Any, readerContext: ImmutableStringReader? = null): Nothing =
    throw if (readerContext != null)
        createWithContext(readerContext, arg)
    else create(arg)

fun Dynamic2CommandExceptionType.throwNew(arg: Any, arg2: Any, readerContext: ImmutableStringReader? = null): Nothing =
    throw if (readerContext != null)
        createWithContext(readerContext, arg, arg2)
    else create(arg, arg2)

fun Dynamic3CommandExceptionType.throwNew(arg: Any, arg2: Any, arg3: Any, readerContext: ImmutableStringReader? = null): Nothing =
    throw if (readerContext != null)
        createWithContext(readerContext, arg, arg2, arg3)
    else create(arg, arg2, arg3)

fun Dynamic4CommandExceptionType.throwNew(arg: Any, arg2: Any, arg3: Any, arg4: Any, readerContext: ImmutableStringReader? = null): Nothing =
    throw if (readerContext != null)
        createWithContext(readerContext, arg, arg2, arg3, arg4)
    else create(arg, arg2, arg3, arg4)

fun DynamicNCommandExceptionType.throwNew(vararg args: Any, readerContext: ImmutableStringReader? = null): Nothing =
    throw if (readerContext != null)
        createWithContext(readerContext, args)
    else create(args)

val errors: BuiltInExceptionProvider = CommandSyntaxException.BUILT_IN_EXCEPTIONS
