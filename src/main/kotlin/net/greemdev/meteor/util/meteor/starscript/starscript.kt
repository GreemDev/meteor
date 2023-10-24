/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor.starscript

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Starscript
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.starscript.api.*
import net.greemdev.meteor.util.misc.clamp
import net.minecraft.util.math.MathHelper
import kotlin.math.*



internal fun initGStarscript() {
    with(MeteorStarscript.ss) {
        clientMods()
        utilities()
    }
}

private fun Starscript.clientMods() {
    newObject("mods") {
        raw { modLoader.allMods.size.toString() }
        defineString("list") { modLoader.allMods.joinToString(", ") { it.metadata.name } }
        modLoader.allMods.forEach { mod ->
            newObject(mod.metadata.id.toCamelCase()) {
                raw(mod.metadata::getName)
                defineString("authors") { mod.metadata.authors.joinToString(", ") { it.name } }
                defineString("version", mod.metadata.version::getFriendlyString)
                defineString("description", mod.metadata::getDescription)
                stringFunc("getValue", Constraint.exactCount(1)) {
                    mod.metadata.getCustomValue(nextString())?.asString
                }
            }
        }
    }
}

private fun Starscript.utilities() {
    stringFunc("camelCase", Constraint.within(1..2)) {
        when (argCount) {
            1 -> nextString().toCamelCase()
            else -> nextString().toCamelCase(nextString())
        }
    }
    numberFunc("fma", Constraint.exactCount(3)) {
        Math.fma(nextNumber(), nextNumber(), nextNumber())
    }
    numberFunc("log", Constraint.within(1..2)) {
        when (argCount) {
            1 -> ln(nextNumber())
            else -> log(nextNumber(), nextNumber())
        }
    }
    numberFunc("log10", Constraint.exactCount(1)) {
        log10(nextNumber())
    }
    numberFunc("sqrt", Constraint.exactCount(1)) {
        sqrt(nextNumber())
    }
    numberFunc("cbrt", Constraint.exactCount(1)) {
        Math.cbrt(nextNumber())
    }
    numberFunc("max", Constraint.exactCount(2)) {
        max(nextNumber(), nextNumber())
    }
    numberFunc("min", Constraint.exactCount(2)) {
        min(nextNumber(), nextNumber())
    }
    numberFunc("clamp", Constraint.exactCount(3)) {
        nextNumber().clamp(nextNumber(), nextNumber())
    }
    numberFunc("avg", Constraint.atLeast(2)) {
        var result = 0.0
        for (i in 1..argCount) {
            result += nextNumber("All arguments to $functionName need to be a number.")
        }
        result / argCount
    }
    numberFunc("wrapDegrees", Constraint.exactCount(1)) {
        MathHelper.wrapDegrees(nextNumber())
    }
    numberFunc("lerp", Constraint.exactCount(3)) {
        MathHelper.lerp(nextNumber(), nextNumber(), nextNumber())
    }
    numberFunc("lerpProgress", Constraint.exactCount(3)) {
        MathHelper.getLerpProgress(nextNumber(), nextNumber(), nextNumber())
    }
}
