/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Starscript
import meteordevelopment.starscript.utils.SFunction
import meteordevelopment.starscript.value.Value
import meteordevelopment.starscript.value.ValueMap
import net.greemdev.meteor.util.*
import java.util.function.Supplier

private fun Starscript.define(name: String, supplier: () -> Value) = set(name, Supplier(supplier))
private fun Starscript.setObject(name: String, builder: ValueMap.() -> Unit) = set(name, starscriptObject(builder))
private fun Starscript.defineFunction(name: String, function: (ss: Starscript, argCount: Int) -> Value) =
    set(name, SFunction(function))

private fun ValueMap.define(name: String, supplier: () -> Value) = set(name, Supplier(supplier))
private fun ValueMap.setObject(name: String, builder: ValueMap.() -> Unit) = set(name, starscriptObject(builder))
private fun ValueMap.defineFunction(name: String, function: (ss: Starscript, argCount: Int) -> Value) =
    set(name, SFunction(function))

fun starscriptObject(builder: ValueMap.() -> Unit) = ValueMap().apply(builder)

internal fun initGStarscript() {
    with(MeteorStarscript.ss) {
        setObject("mods", starscriptMods)
    }
}

private val starscriptMods: ValueMap.() -> Unit = {
    define("_toString") { Value.number(modLoader.allMods.size.toDouble()) }
    define("list") { Value.string(modLoader.allMods.joinToString(", ") { it.metadata.name }) }
    modLoader.allMods.forEach { mod ->
        setObject(mod.metadata.id.toCamelCase()) {
            define("_toString") { Value.string(mod.metadata.name) }
            define("authors") { Value.string(mod.metadata.authors.joinToString(", ") { it.name }) }
            define("version") { Value.string(mod.metadata.version.friendlyString) }
            define("description") { Value.string(mod.metadata.description) }
            defineFunction("getValue") { ss, argCount ->
                if (argCount != 1) ss.error("getValue() requires 1 argument, got $argCount.")
                val name = ss.popString("Argument to getValue() needs to be a string.")
                if (mod.metadata.containsCustomValue(name))
                    Value.string(mod.metadata.getCustomValue(name).asString)
                else Value.null_()
            }
        }
    }
}
