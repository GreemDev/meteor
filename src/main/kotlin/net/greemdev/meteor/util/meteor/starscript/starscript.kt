/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor.starscript

import meteordevelopment.starscript.Starscript
import net.greemdev.meteor.Pipe
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.math.hasDecimal
import net.greemdev.meteor.util.meteor.starscript.api.*
import net.greemdev.meteor.util.math.lerp
import net.minecraft.util.math.MathHelper
import kotlin.math.*

internal fun initGStarscript() {
    meteorStarscript.clientMods()
    meteorStarscript.math()
    meteorStarscript.utilities()
}

private fun Starscript.clientMods() {
    newObject("mods") {
        defineToString { modLoader.allMods.size.toString() }
        defineString("list") { modLoader.allMods.joinToString(", ") { it.metadata.name } }
        modLoader.allMods.forEach { mod ->
            newObject(mod.metadata.id.toCamelCase("-", "_")) {
                defineToString(mod.metadata::getName)
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

private fun Starscript.math() {
    singleNumberFunc("log10", ::log10)
    singleNumberFunc("sqrt", ::sqrt)
    singleNumberFunc("cbrt", ::cbrt)
    singleNumberFunc("tan", ::tan)
    singleNumberFunc("tanh", ::tanh)
    singleNumberFunc("atan", ::atan)
    singleNumberFunc("atanh", ::atanh)
    singleNumberFunc("sin", ::sin)
    singleNumberFunc("sinh", ::sinh)
    singleNumberFunc("cos", ::cos)
    singleNumberFunc("cosh", ::cosh)
    singleNumberFunc("wrapDegrees", MathHelper::wrapDegrees)

    numberFunc("max",   Constraint.exactCount(2)) { max(nextNumber(), nextNumber()) }
    numberFunc("min",   Constraint.exactCount(2)) { min(nextNumber(), nextNumber()) }
    numberFunc("clamp", Constraint.exactCount(3)) { nextNumber().coerceIn(nextNumber(), nextNumber()) }
    numberFunc("lerp",  Constraint.exactCount(3)) { nextNumber().lerp(nextNumber(), nextNumber()) }

    numberFunc("fma", Constraint.exactCount(3)) {
        Math.fma(nextNumber(), nextNumber(), nextNumber())
    }
    numberFunc("log", Constraint.within(1..2)) {
        when (argCount) {
            1 -> ln(nextNumber())
            else -> log(nextNumber(), nextNumber())
        }
    }
    numberFunc("lerpProgress", Constraint.exactCount(3)) {
        MathHelper.getLerpProgress(nextNumber(), nextNumber(), nextNumber())
    }
    numberFunc("avg", Constraint.atLeast(2)) {
        getVariadicArguments(ArgType.Number, "All arguments to $functionName need to be a number.").average()
        //TODO: test getVariadicArguments
    }
    numberFunc("xor", Constraint.exactCount(2)) {
        val first = nextNumber()
        val second = nextNumber()

        if (first.hasDecimal or second.hasDecimal)
            starscript.error("Only non-decimal values are accepted for %s.", functionName)

        first.toLong() xor second.toLong()
    }
}


private fun Starscript.utilities() {
    stringFunc("camelCase", Constraint.atLeast(1)) {
        when (argCount) {
            1 -> nextString().toCamelCase()
            2 -> nextString().toCamelCase(nextString())
            else -> nextString().toCamelCase(*getVariadicArguments(ArgType.Str))
        }
    }
}

private inline fun Starscript.singleNumberFunc(name: String, crossinline mathFunc: Pipe<Double>) =
    numberFunc(name, Constraint.exactCount(1)) { mathFunc(nextNumber()) }
