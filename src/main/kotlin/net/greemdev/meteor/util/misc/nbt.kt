/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("NbtUtil")
@file:Suppress("FunctionName")

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.utils.misc.ByteCountDataOutput
import net.greemdev.meteor.*
import net.greemdev.meteor.util.string
import net.minecraft.nbt.*
import net.minecraft.text.Text
import net.minecraft.util.crash.CrashException
import java.io.DataInput
import java.io.DataOutput
import java.io.File
import java.io.IOException
import java.util.function.Consumer
import kotlin.random.Random

fun File.write(tag: NbtCompound) =
    runCatching { NbtIo.write(tag, this) }.exceptionOrNull()

fun DataOutput.write(tag: NbtCompound) =
    runCatching { NbtIo.write(tag, this) }.exceptionOrNull()

fun UnlimitedNbtTagSizeTracker() = NbtTagSizeTracker(Long.MAX_VALUE)

@JvmName("readCompound")
fun DataInput.readNbtCompound(nbtTagSizeTracker: NbtTagSizeTracker = UnlimitedNbtTagSizeTracker()): Result<NbtCompound> =
    runCatching { NbtIo.read(this, nbtTagSizeTracker) }

@JvmName("read")
fun File.readNbt(): NbtCompound =
    runCatching {
        NbtIo.read(this)
    }.onFailureOf(CrashException::class) { throw it.cause!! }
        .getOrNull() ?: error("File at $absolutePath doesn't exist.")

@JvmName("readOrNull")
fun File.readNbtOrNull() = getOrNull(this::readNbt)

object Nbt {
    infix fun compound(builder: Initializer<NbtCompound>) = NbtCompound().apply(builder)
    infix fun list(builder: Initializer<NbtList>) = NbtList().apply(builder)

    @JvmStatic
    fun list(elements: Collection<Any>) = listElements(elements.map(Any::toNBT))
    @JvmStatic
    fun list(vararg elements: Any) = list(elements.toList())
    @JvmStatic
    infix fun listElements(elements: Collection<NbtElement>) = list { elements.forEach(::add) }
    @JvmStatic
    @JvmName("newCompound")
    fun `java-compound`(builder: Consumer<NbtCompound>) = compound(builder.kotlin)
    @JvmStatic
    @JvmName("newList")
    fun `java-list`(builder: Consumer<NbtList>) = list(builder.kotlin)
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

fun Any?.toNBTOrNull() = this?.toNBT()

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
    Byte::class -> NbtByteArray(cast<Array<Byte>>().toList())
    Long::class -> NbtLongArray(cast<Array<Long>>().toList())
    Int::class -> NbtIntArray(cast<Array<Int>>().toList())
    else -> error("Unknown or unsupported array type Supported are Byte, Long, and Int.")
}

fun Collection<*>.toNBT() = Nbt listElements mapNotNull(Any?::toNBTOrNull)

fun NbtList.collectAsStrings() = mapNotNull(NbtElement::asString)

@Throws(IOException::class)
fun NbtCompound.countBytes(): Int {
    return ByteCountDataOutput().also(::write).count
}

fun NbtCompound.ifTagPresent(name: String, action: Consumer<NbtCompound>) {
    if (contains(name, NbtDataType.Compound.int))
        action(getCompound(name))
}

fun NbtCompound.ifElementPresent(name: String, dataType: NbtDataType, action: Consumer<NbtElement>) {
    if (contains(name, dataType.int))
        action(get(name)!!)
}


fun Random.createRandomNbtIntArray(elements: Int) =
    string {
        +"[I;"
        repeat(elements) {
            +"${nextInt()}"

            if (it < elements - 1)
                +','
        }
        +']'
    }

fun Random.createRandomNbtLongArray(elements: Int) =
    string {
        +"[L;"
        repeat(elements) {
            +"${nextLong()}"

            if (it < elements - 1)
                +','
        }
        +']'
    }

fun Random.createRandomNbtByteArray(elements: Int) =
    string {
        +"[B;"
        +nextBytes(elements).joinToString(",")
        +']'
    }
