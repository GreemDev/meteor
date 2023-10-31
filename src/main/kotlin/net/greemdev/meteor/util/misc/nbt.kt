/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("NbtUtil")

package net.greemdev.meteor.util.misc

import net.greemdev.meteor.*
import net.minecraft.nbt.*
import net.minecraft.text.Text
import java.io.File
import java.util.function.Consumer

fun File.write(tag: NbtCompound) =
    runCatching { NbtIo.write(tag, this) }.exceptionOrNull()

fun File.readNbt(): NbtCompound = NbtIo.read(this) ?: error("File at $absolutePath doesn't exist.")

fun File.readNbtOrNull() = getOrNull(this::readNbt)

object Nbt {
    infix fun compound(builder: Initializer<NbtCompound>) = NbtCompound().apply(builder)
    infix fun list(builder: Initializer<NbtList>) = NbtList().apply(builder)

    @JvmStatic
    fun list(elements: Collection<Any>) = listElements(elements.map(Any::toNBT))
    @JvmStatic
    fun list(vararg elements: Any) = list(elements.toList())
    @JvmStatic
    infix fun listElements(elements: Collection<NbtElement>) = list { elements.forEach { add(it) } }
    @JvmStatic
    @JvmName("newCompound")
    fun `java-compound`(builder: Consumer<NbtCompound>) = compound { builder.accept(this) }
    @JvmStatic
    @JvmName("newList")
    fun `java-list`(builder: Consumer<NbtList>) = list { builder.accept(this) }
}

fun Any.toNBT(): NbtElement = when (this) {
    is String -> NbtString.of(this)
    is Int -> NbtInt.of(this)
    is Byte -> NbtByte.of(this)
    is Boolean -> NbtByte.of(this)
    is Double -> NbtDouble.of(this)
    is Float -> NbtFloat.of(this)
    is Long -> NbtLong.of(this)
    else -> error("Unknown or unsupported object type ${this::class.java.simpleName}. Valid NBT non-collection values are: [String, Int, Byte, Boolean, Double, Float, Long]")
}

enum class NbtDataType(val byte: kotlin.Byte) {
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

    val int = byte.toInt()
}

fun NbtCompound.getList(name: String, type: NbtDataType): NbtList =
    getList(name, type.int)
fun NbtCompound.collectList(name: String, type: NbtDataType) =
    getList(name, type).collectAsStrings()

fun NbtElement.asPrettyText(): Text = NbtHelper.toPrettyPrintedText(this)

inline fun<reified T> Array<T>.toNBT(): NbtElement = when (T::class) {
    Byte::class -> NbtByteArray(castFast<Array<Byte>>().toList())
    Long::class -> NbtLongArray(castFast<Array<Long>>().toList())
    Int::class -> NbtIntArray(castFast<Array<Int>>().toList())
    else -> error("Unknown or unsupported array type Supported are Byte, Long, and Int.")
}

fun Collection<*>.toNBT() = Nbt listElements mapNotNull { it?.toNBT() }

fun NbtList.collectAsStrings() = mapNotNull(NbtElement::asString)

