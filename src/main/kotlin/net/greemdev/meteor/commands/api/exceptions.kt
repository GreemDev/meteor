/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.ImmutableStringReader
import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.*

fun simpleCommandException(message: Message) = lazy {
    SimpleCommandExceptionType(message)
}

fun dynamicCommandException(func: (Any) -> Message) = lazy {
    DynamicCommandExceptionType(func)
}

fun dynamic2CommandException(func: (Any, Any) -> Message) = lazy {
    Dynamic2CommandExceptionType(func)
}

fun dynamic3CommandException(func: (Any, Any, Any) -> Message) = lazy {
    Dynamic3CommandExceptionType(func)
}

fun dynamic4CommandException(func: (Any, Any, Any, Any) -> Message) = lazy {
    Dynamic4CommandExceptionType(func)
}

fun dynamicNCommandException(func: (List<Any>) -> Message) = lazy {
    DynamicNCommandExceptionType { func(it.toList()) }
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
