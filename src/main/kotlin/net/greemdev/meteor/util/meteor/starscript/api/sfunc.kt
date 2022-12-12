/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor.starscript.api

import meteordevelopment.starscript.Starscript
import meteordevelopment.starscript.utils.StarscriptError
import meteordevelopment.starscript.value.Value
import meteordevelopment.starscript.value.ValueMap
import net.greemdev.meteor.util.pluralize
import net.greemdev.meteor.util.string

fun <T> Starscript.popArg(argPos: Int, functionName: String, type: ArgType<T>, customError: String? = null) =
    type.popper(this, customError ?: "Argument $argPos of $functionName() needs to be a ${type.friendly}.")

sealed class ArgType<T>(
    val friendly: kotlin.String,
    val popper: Starscript.(kotlin.String) -> T
) {
    object Boolean : ArgType<kotlin.Boolean>(
        "boolean (true/false)",
        Starscript::popBool
    )

    object String : ArgType<kotlin.String>(
        "string",
        Starscript::popString
    )

    object Number : ArgType<Double>(
        "number",
        Starscript::popNumber
    )
}

class StarscriptFunctionContext(val name: String, val starscript: Starscript, val argCount: Int) {
    private var argPos = 1

    val functionName = "$name()"

    fun constrain(constraint: Constraint, customError: String? = null): StarscriptFunctionContext {
        if (!constraint.predicate(argCount))
            throw StarscriptError(customError ?: constraint.formatError(name, argCount))
        return this
    }

    fun<T> nextArg(type: ArgType<T>, customError: String? = null): T = starscript.popArg(argPos++, name, type, customError)
    fun nextBoolean(customError: String? = null) = nextArg(ArgType.Boolean, customError)
    fun nextString(customError: String? = null) = nextArg(ArgType.String, customError)
    fun nextNumber(customError: String? = null) = nextArg(ArgType.Number, customError)
}

class Constraint private constructor(private val data: Pair<Int, Any>, val predicate: (Int) -> Boolean) {
    init {
        if (data.first !in 0..3) error("Invalid Constraint type.")
    }

    companion object {
        fun exactCount(number: Number) = Constraint(0 to number) { number == it }
        fun atLeast(minimum: Number) = Constraint(1 to minimum) { it >= minimum.toInt() }
        fun atMost(maximum: Number) = Constraint(2 to maximum) { it <= maximum.toInt() }
        fun within(range: IntRange) = Constraint(3 to range) { it in range }
    }

    fun formatError(functionName: String, argCount: Int) = string {
        val (type, comparerTo) = data
        +"$functionName() requires "
        when (type) {
            0 -> +"argument".pluralize(comparerTo as Int)
            1 -> +"at least ${"argument".pluralize(comparerTo as Int)}"
            2 -> +"at most ${"argument".pluralize(comparerTo as Int)}"
            3 -> {
                comparerTo as IntRange
                append(comparerTo.first)
                +'-'
                append(comparerTo.last)
                +" argument".pluralize(
                    comparerTo.sum().takeUnless { it == 1 && comparerTo.first == 0 } ?: 2, //account for the fact that 0-1 should still be considered plural
                    prefixQuantity = false
                )
            }
        }
        +", got $argCount."
    }
}

fun Starscript.func(name: String, logic: ConstrainedStarscriptFunction<Value>): Starscript {
    defineFunction(name) { ss, argCount ->
        logic(Constraint, StarscriptFunctionContext(name, ss, argCount))
    }
    return this
}

fun ValueMap.func(name: String, logic: ConstrainedStarscriptFunction<Value>): ValueMap {
    defineFunction(name) { ss, argCount ->
        logic(Constraint, StarscriptFunctionContext(name, ss, argCount))
    }
    return this
}

fun Starscript.func(name: String, constraint: Constraint, logic: StarscriptFunction<Value>): Starscript {
    defineFunction(name) { ss, argCount ->
        StarscriptFunctionContext(name, ss, argCount).constrain(constraint).logic()
    }
    return this
}

fun ValueMap.func(name: String, constraint: Constraint, logic: StarscriptFunction<Value>): ValueMap {
    defineFunction(name) { ss, argCount ->
        StarscriptFunctionContext(name, ss, argCount).constrain(constraint).logic()
    }
    return this
}


fun Starscript.booleanFunc(name: String, logic: ConstrainedStarscriptFunction<Boolean?>) =
    func(name) { BooleanValue(logic(Constraint, this)) }

fun ValueMap.booleanFunc(name: String, logic: ConstrainedStarscriptFunction<Boolean?>) =
    func(name) { BooleanValue(logic(Constraint, this)) }

fun Starscript.booleanFunc(name: String, constraint: Constraint, logic: StarscriptFunction<Boolean?>) =
    func(name, constraint) { BooleanValue(logic()) }

fun ValueMap.booleanFunc(name: String, constraint: Constraint, logic: StarscriptFunction<Boolean?>) =
    func(name, constraint) { BooleanValue(logic()) }


fun Starscript.numberFunc(name: String, logic: ConstrainedStarscriptFunction<Number?>) =
    func(name) { NumberValue(logic(Constraint, this)) }

fun ValueMap.numberFunc(name: String, logic: ConstrainedStarscriptFunction<Number?>) =
    func(name) { NumberValue(logic(Constraint, this)) }

fun Starscript.numberFunc(name: String, constraint: Constraint, logic: StarscriptFunction<Number?>) =
    func(name, constraint) { NumberValue(logic()) }

fun ValueMap.numberFunc(name: String, constraint: Constraint, logic: StarscriptFunction<Number?>) =
    func(name, constraint) { NumberValue(logic()) }


fun Starscript.stringFunc(name: String, logic: ConstrainedStarscriptFunction<String?>) =
    func(name) { StringValue(logic(Constraint, this)) }

fun ValueMap.stringFunc(name: String, logic: ConstrainedStarscriptFunction<String?>) =
    func(name) { StringValue(logic(Constraint, this)) }

fun Starscript.stringFunc(name: String, constraint: Constraint, logic: StarscriptFunction<String?>) =
    func(name, constraint) { StringValue(logic()) }

fun ValueMap.stringFunc(name: String, constraint: Constraint, logic: StarscriptFunction<String?>) =
    func(name, constraint) { StringValue(logic()) }


fun Starscript.objectFunc(name: String, logic: ConstrainedStarscriptFunction<ValueMap?>) =
    func(name) { ObjectValue(logic(Constraint, this)) }

fun ValueMap.objectFunc(name: String, logic: ConstrainedStarscriptFunction<ValueMap?>) =
    func(name) { ObjectValue(logic(Constraint, this)) }

fun Starscript.objectFunc(name: String, constraint: Constraint, logic: StarscriptFunction<ValueMap?>) =
    func(name, constraint) { ObjectValue(logic()) }

fun ValueMap.objectFunc(name: String, constraint: Constraint, logic: StarscriptFunction<ValueMap?>) =
    func(name, constraint) { ObjectValue(logic()) }


fun Starscript.mapFunc(name: String, logic: ConstrainedStarscriptFunction<Map<String, Value>?>) =
    func(name) { MapValue(logic(Constraint, this)) }

fun ValueMap.mapFunc(name: String, logic: ConstrainedStarscriptFunction<Map<String, Value>?>) =
    func(name) { MapValue(logic(Constraint, this)) }

fun Starscript.mapFunc(name: String, constraint: Constraint, logic: StarscriptFunction<Map<String, Value>?>) =
    func(name, constraint) { MapValue(logic()) }

fun ValueMap.mapFunc(name: String, constraint: Constraint, logic: StarscriptFunction<Map<String, Value>?>) =
    func(name, constraint) { MapValue(logic()) }


typealias ConstrainedStarscriptFunction<T> = context(Constraint.Companion) StarscriptFunctionContext.() -> T
typealias StarscriptFunction<T> = StarscriptFunctionContext.() -> T
