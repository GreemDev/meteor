/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Starscript
import meteordevelopment.starscript.utils.SFunction
import meteordevelopment.starscript.value.Value
import meteordevelopment.starscript.value.ValueMap
import java.util.function.Supplier

private fun Starscript.define(name: String, supplier: () -> Value) = set(name, Supplier(supplier))
private fun Starscript.defineFunction(name: String, function: (Starscript, Int) -> Value) = set(name, SFunction(function))

private fun ValueMap.define(name: String, supplier: () -> Value) = set(name, Supplier(supplier))
private fun ValueMap.defineFunction(name: String, function: (Starscript, Int) -> Value) = set(name, SFunction(function))

internal fun initGStarscript() {
    with(MeteorStarscript.ss) {
        val mods = ValueMap()
        modLoader.allMods.forEach { mod ->
            mods.set(mod.metadata.id.replace("-", "_"), ValueMap()
                .define("_toString") { Value.string(mod.metadata.name) }
                .define("author") { Value.string(mod.metadata.authors.joinToString(", ") { it.name }) }
                .define("version") { Value.string(mod.metadata.version.friendlyString) }
                .define("description") { Value.string(mod.metadata.description) }
                .defineFunction("value") { ss, argCount ->
                    if (argCount != 1) ss.error("value() requires 1 argument, got $argCount.")
                    val name = ss.popString("Argument to value() needs to be a string.")
                    if (mod.metadata.containsCustomValue(name))
                        Value.string(mod.metadata.getCustomValue(name).asString)
                    else Value.null_()
                }
            )
        }
        set("mods", mods
            .define("_toString") { Value.number(modLoader.allMods.size.toDouble()) }
            .define("list") { Value.string(modLoader.allMods.joinToString(", ") { it.metadata.name }) }
        )
    }
}
