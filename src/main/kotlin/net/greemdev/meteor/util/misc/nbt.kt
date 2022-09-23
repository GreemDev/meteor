/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package net.greemdev.meteor.util.misc

import net.minecraft.nbt.*
import java.util.function.Consumer

object Nbt {
    infix fun compound(builder: NbtCompound.() -> Unit) = NbtCompound().apply(builder)
    infix fun list(builder: NbtList.() -> Unit) = NbtList().apply(builder)

    @JvmStatic
    infix fun list(elements: Collection<NbtElement>) = list { elements.forEach { add(it) } }
    @JvmStatic
    fun newCompound(builder: Consumer<NbtCompound>) = compound { builder.accept(this) }
    @JvmStatic
    fun newList(builder: Consumer<NbtList>) = list { builder.accept(this) }
}

fun Any.toNBT(): NbtElement = when (this) {
    is String -> NbtString.of(this)
    is Int -> NbtInt.of(this)
    is Byte -> NbtByte.of(this)
    is Boolean -> NbtByte.of(this)
    is Double -> NbtDouble.of(this)
    is Float -> NbtFloat.of(this)
    is Long -> NbtLong.of(this)
    else -> error("Unknown or unsupported object type.")
}

enum class NbtDataType(type: kotlin.Byte) {
    Byte(NbtElement.BYTE_TYPE),
    Short(NbtElement.SHORT_TYPE),
    Integer(NbtElement.INT_TYPE),
    Long(NbtElement.LONG_TYPE),
    Float(NbtElement.FLOAT_TYPE),
    Double(NbtElement.DOUBLE_TYPE),
    String(NbtElement.STRING_TYPE),
    Compound(NbtElement.COMPOUND_TYPE),
    List(NbtElement.LIST_TYPE),
    IntegerArray(NbtElement.INT_ARRAY_TYPE),
    LongArray(NbtElement.LONG_ARRAY_TYPE),
    ByteArray(NbtElement.BYTE_ARRAY_TYPE),
    End(NbtElement.END_TYPE);


    val int = type.toInt()
}

fun NbtCompound.getList(name: String, type: NbtDataType): NbtList = getList(name, type.int)

@Suppress("UNCHECKED_CAST")
inline fun<reified T> Array<T>.toNBT(): NbtElement = when (T::class) {
    Byte::class -> NbtByteArray((this as Array<Byte>).toList())
    Long::class -> NbtLongArray((this as Array<Long>).toList())
    Int::class -> NbtIntArray((this as Array<Int>).toList())
    else -> error("Unknown or unsupported array type Supported are Byte, Long, and Int.")
}

fun Collection<*>.toNBT() = Nbt list mapNotNull { it?.toNBT() }

