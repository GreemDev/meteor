/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("FunctionName") //factory functions

package net.greemdev.meteor.util.meteor.starscript.api

import meteordevelopment.starscript.Starscript
import meteordevelopment.starscript.value.Value
import meteordevelopment.starscript.value.ValueMap
import meteordevelopment.starscript.utils.SFunction
import java.util.function.Supplier

fun Starscript.define(name: String, supplier: () -> Value) = set(name, Supplier(supplier))
fun Starscript.defineBoolean(name: String, supplier: () -> Boolean?) = set(name, supplier wrapBy ::BooleanValue)
fun Starscript.defineNumber(name: String, supplier: () -> Number?) = set(name, supplier wrapBy ::NumberValue)
fun Starscript.defineString(name: String, supplier: () -> String?) = set(name, supplier wrapBy ::StringValue)
fun Starscript.defineObject(name: String, supplier: () -> ValueMap?) = set(name, supplier wrapBy ::ObjectValue)
fun Starscript.defineMap(name: String, supplier: () -> Map<String, Value>?) = set(name, supplier wrapBy ::MapValue)
fun Starscript.defineFunction(name: String, function: (ss: Starscript, argCount: Int) -> Value) = set(name, SFunction(function))
fun Starscript.newObject(name: String, builder: ValueMap.() -> Unit) = set(name, buildValueMap(builder))
fun Starscript.raw(value: () -> String) = defineString("_toString", value)

fun ValueMap.define(name: String, supplier: () -> Value) = set(name, Supplier(supplier))
fun ValueMap.defineBoolean(name: String, supplier: () -> Boolean?) = set(name, supplier wrapBy ::BooleanValue)
fun ValueMap.defineNumber(name: String, supplier: () -> Number?) = set(name, supplier wrapBy ::NumberValue)
fun ValueMap.defineString(name: String, supplier: () -> String?) = set(name, supplier wrapBy ::StringValue)
fun ValueMap.defineObject(name: String, supplier: () -> ValueMap?) = set(name, supplier wrapBy ::ObjectValue)
fun ValueMap.defineMap(name: String, supplier: () -> Map<String, Value>?) = set(name, supplier wrapBy ::MapValue)
fun ValueMap.defineFunction(name: String, function: (ss: Starscript, argCount: Int) -> Value) = set(name, SFunction(function))
fun ValueMap.newObject(name: String, builder: ValueMap.() -> Unit) = set(name, buildValueMap(builder))
fun ValueMap.raw(value: () -> String) = defineString("_toString", value)

fun buildValueMap(builder: ValueMap.() -> Unit) = ValueMap().apply(builder)

fun NullValue(): Value = Value.null_()
fun BooleanValue(value: Boolean?): Value { return Value.bool(value ?: return NullValue()) }
fun NumberValue(value: Number?): Value { return Value.number(value?.toDouble() ?: return NullValue()) }
fun StringValue(value: String?): Value { return Value.string(value ?: return NullValue()) }
fun FunctionValue(value: SFunction?): Value { return Value.function(value ?: return NullValue()) }
fun ObjectValue(value: ValueMap?): Value { return Value.map(value ?: return NullValue()) }
fun MapValue(value: Map<String, Value>?): Value =
    if (value != null)
        Value.map(buildValueMap { value.forEach(::set) })
    else NullValue()

private infix fun<T> (() -> T).wrapBy(converter: (T) -> Value) = Supplier {
    converter(this())
}
