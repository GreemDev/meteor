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
import net.greemdev.meteor.*

const val STARSCRIPT_TOSTRING = "_toString"

fun Starscript.define(name: String, supplier: Getter<Value>): ValueMap = set(name, supplier)
fun Starscript.defineBoolean(name: String, supplier: Getter<Boolean?>): ValueMap = set(name, supplier wrapBy ::BooleanValue)
fun Starscript.defineNumber(name: String, supplier: Getter<Number?>): ValueMap = set(name, supplier wrapBy ::NumberValue)
fun Starscript.defineString(name: String, supplier: Getter<String?>): ValueMap = set(name, supplier wrapBy ::StringValue)
fun Starscript.defineObject(name: String, supplier: Getter<ValueMap?>): ValueMap = set(name, supplier wrapBy ::ObjectValue)
fun Starscript.defineMap(name: String, supplier: Getter<Map<String, Value>?>): ValueMap = set(name, supplier wrapBy ::MapValue)
fun Starscript.defineFunction(name: String, function: (ss: Starscript, argCount: Int) -> Value): ValueMap = set(name, SFunction(function))
fun Starscript.newObject(name: String, builder: Initializer<ValueMap>): ValueMap = set(name, ValueMap(builder))
fun Starscript.defineToString(value: Getter<String>): ValueMap = defineString(STARSCRIPT_TOSTRING, value)

fun ValueMap.define(name: String, supplier: Getter<Value>): ValueMap = set(name, supplier)
fun ValueMap.defineBoolean(name: String, supplier: Getter<Boolean?>): ValueMap = set(name, supplier wrapBy ::BooleanValue)
fun ValueMap.defineNumber(name: String, supplier: Getter<Number?>): ValueMap = set(name, supplier wrapBy ::NumberValue)
fun ValueMap.defineString(name: String, supplier: Getter<String?>): ValueMap = set(name, supplier wrapBy ::StringValue)
fun ValueMap.defineObject(name: String, supplier: Getter<ValueMap?>): ValueMap = set(name, supplier wrapBy ::ObjectValue)
fun ValueMap.defineMap(name: String, supplier: Getter<Map<String, Value>?>): ValueMap = set(name, supplier wrapBy ::MapValue)
fun ValueMap.defineFunction(name: String, function: (ss: Starscript, argCount: Int) -> Value): ValueMap = set(name, SFunction(function))
fun ValueMap.newObject(name: String, builder: Initializer<ValueMap>): ValueMap = set(name, ValueMap(builder))
fun ValueMap.defineToString(value: Getter<String>): ValueMap = defineString(STARSCRIPT_TOSTRING, value)

fun ValueMap(builder: Initializer<ValueMap>) = ValueMap().apply(builder)

fun NullValue(): Value = Value.null_()
fun BooleanValue(value: Boolean?): Value = value?.let(Value::bool) ?: NullValue()
fun NumberValue(value: Number?): Value = value?.toDouble()?.let(Value::number) ?: NullValue()
fun StringValue(value: String?): Value = value?.let(Value::string) ?: NullValue()
fun FunctionValue(value: SFunction): Value = Value.function(value)
fun ObjectValue(value: ValueMap?): Value = value?.let(Value::map) ?: NullValue()
fun MapValue(value: Map<String, Value>?): Value =
    value?.let {
        Value.map(ValueMap { value.forEach(::set) })
    } ?: NullValue()

private infix fun<T> Getter<T>.wrapBy(
    converter: Mapper<T, Value>
): Getter<Value> =
    { converter(this()) }
