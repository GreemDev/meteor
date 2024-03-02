/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor.starscript.api

import meteordevelopment.starscript.Starscript
import meteordevelopment.starscript.utils.StarscriptError
import meteordevelopment.starscript.value.Value
import meteordevelopment.starscript.value.ValueMap
import net.greemdev.meteor.Getter
import net.greemdev.meteor.Predicate
import net.greemdev.meteor.util.pluralize

sealed class ArgType<T>(
    val friendly: String,
    val popper: Starscript.(String) -> T
) {
    data object Bool : ArgType<Boolean>(
        "boolean (true/false)",
        Starscript::popBool
    )

    data object Str : ArgType<String>(
        "string",
        Starscript::popString
    )

    data object Number : ArgType<Double>(
        "number",
        Starscript::popNumber
    )
}

class StarscriptFunctionContext(val name: String, val starscript: Starscript, val argCount: Int) {
    var argPos = 1
        private set

    val functionName = "$name()"

    fun constrain(constraint: Constraint, customError: String? = null): StarscriptFunctionContext {
        if (!constraint(argCount))
            throw StarscriptError(customError ?: constraint.getError(this))
        return this
    }

    private fun <T> Starscript.popArg(type: ArgType<T>, customError: String? = null) =
        type.popper(this, customError ?: "Argument ${argPos++} of $functionName needs to be a ${type.friendly}.")

    fun<T> nextArg(type: ArgType<T>, customError: String? = null): T = starscript.popArg(type, customError)
    fun nextBoolean(customError: String? = null) = nextArg(ArgType.Bool, customError)
    fun nextString(customError: String? = null) = nextArg(ArgType.Str, customError)
    fun nextNumber(customError: String? = null) = nextArg(ArgType.Number, customError)

    /**
     * Consumes the rest of the arguments provided as the specified [type]
     */
    inline fun<reified T> getVariadicArguments(type: ArgType<T>, customError: String? = null) =
        Array(argCount - (argPos - 1)) { nextArg(type, customError) }
}

sealed class Constraint(
    private val test: Predicate<Int>,
    private val friendlyError: Getter<String>
) : Predicate<Int> by test {
    class ExactCount(count: Int) : Constraint(
        { it == count },
        { "argument".pluralize(count, prefixQuantity = true) }
    )
    class AtLeast(minimum: Int) : Constraint(
        { it >= minimum },
        { "at least ${"argument".pluralize(minimum, prefixQuantity = true)}" }
    )
    class AtMost(maximum: Int) : Constraint(
        { it <= maximum },
        { "at most ${"argument".pluralize(maximum, prefixQuantity = true)}" }
    )
    class Within(range: IntRange) : Constraint(range::contains, { "$range arguments" })

    companion object {
        fun exactCount(number: Number) = ExactCount(number.toInt())
        fun atLeast(minimum: Number) = AtLeast(minimum.toInt())
        fun atMost(maximum: Number) = AtMost(maximum.toInt())
        fun within(range: IntRange) = Within(range)
    }

    fun getError(ctx: StarscriptFunctionContext) =
        "${ctx.functionName} requires ${friendlyError()}, got ${ctx.argCount}."
}

fun Starscript.func(name: String, logic: UnconstrainedStarscriptFunction<Value>): Starscript {
    defineFunction(name) { ss, argCount ->
        logic(Constraint, StarscriptFunctionContext(name, ss, argCount))
    }
    return this
}

fun ValueMap.func(name: String, logic: UnconstrainedStarscriptFunction<Value>): ValueMap {
    defineFunction(name) { ss, argCount ->
        logic(Constraint, StarscriptFunctionContext(name, ss, argCount))
    }
    return this
}

fun Starscript.func(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Value>): Starscript {
    defineFunction(name) { ss, argCount ->
        logic(StarscriptFunctionContext(name, ss, argCount).constrain(constraint))
    }
    return this
}

fun ValueMap.func(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Value>): ValueMap {
    defineFunction(name) { ss, argCount ->
        logic(StarscriptFunctionContext(name, ss, argCount).constrain(constraint))
    }
    return this
}


fun Starscript.booleanFunc(name: String, logic: UnconstrainedStarscriptFunction<Boolean?>) =
    func(name) { BooleanValue(logic(Constraint, this)) }

fun ValueMap.booleanFunc(name: String, logic: UnconstrainedStarscriptFunction<Boolean?>) =
    func(name) { BooleanValue(logic(Constraint, this)) }

fun Starscript.booleanFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Boolean?>) =
    func(name, constraint) { BooleanValue(logic()) }

fun ValueMap.booleanFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Boolean?>) =
    func(name, constraint) { BooleanValue(logic()) }


fun Starscript.numberFunc(name: String, logic: UnconstrainedStarscriptFunction<Number?>) =
    func(name) { NumberValue(logic(Constraint, this)) }

fun ValueMap.numberFunc(name: String, logic: UnconstrainedStarscriptFunction<Number?>) =
    func(name) { NumberValue(logic(Constraint, this)) }

fun Starscript.numberFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Number?>) =
    func(name, constraint) { NumberValue(logic()) }

fun ValueMap.numberFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Number?>) =
    func(name, constraint) { NumberValue(logic()) }


fun Starscript.stringFunc(name: String, logic: UnconstrainedStarscriptFunction<String?>) =
    func(name) { StringValue(logic(Constraint, this)) }

fun ValueMap.stringFunc(name: String, logic: UnconstrainedStarscriptFunction<String?>) =
    func(name) { StringValue(logic(Constraint, this)) }

fun Starscript.stringFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<String?>) =
    func(name, constraint) { StringValue(logic()) }

fun ValueMap.stringFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<String?>) =
    func(name, constraint) { StringValue(logic()) }


fun Starscript.objectFunc(name: String, logic: UnconstrainedStarscriptFunction<ValueMap?>) =
    func(name) { ObjectValue(logic(Constraint, this)) }

fun ValueMap.objectFunc(name: String, logic: UnconstrainedStarscriptFunction<ValueMap?>) =
    func(name) { ObjectValue(logic(Constraint, this)) }

fun Starscript.objectFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<ValueMap?>) =
    func(name, constraint) { ObjectValue(logic()) }

fun ValueMap.objectFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<ValueMap?>) =
    func(name, constraint) { ObjectValue(logic()) }


fun Starscript.mapFunc(name: String, logic: UnconstrainedStarscriptFunction<Map<String, Value>?>) =
    func(name) { MapValue(logic(Constraint, this)) }

fun ValueMap.mapFunc(name: String, logic: UnconstrainedStarscriptFunction<Map<String, Value>?>) =
    func(name) { MapValue(logic(Constraint, this)) }

fun Starscript.mapFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Map<String, Value>?>) =
    func(name, constraint) { MapValue(logic()) }

fun ValueMap.mapFunc(name: String, constraint: Constraint, logic: ConstrainedStarscriptFunction<Map<String, Value>?>) =
    func(name, constraint) { MapValue(logic()) }


typealias UnconstrainedStarscriptFunction<T> = context(Constraint.Companion) StarscriptFunctionContext.() -> T
typealias ConstrainedStarscriptFunction<T> = StarscriptFunctionContext.() -> T
