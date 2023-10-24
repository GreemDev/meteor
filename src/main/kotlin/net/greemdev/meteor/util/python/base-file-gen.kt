/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.python

import com.mojang.text2speech.Narrator
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.Utils
import meteordevelopment.meteorclient.utils.entity.*
import meteordevelopment.meteorclient.utils.files.StreamUtils
import meteordevelopment.meteorclient.utils.misc.*
import meteordevelopment.meteorclient.utils.misc.input.*
import meteordevelopment.meteorclient.utils.misc.text.TextUtils
import meteordevelopment.meteorclient.utils.network.*
import meteordevelopment.meteorclient.utils.notebot.NotebotUtils
import meteordevelopment.meteorclient.utils.notebot.decoder.*
import meteordevelopment.meteorclient.utils.notebot.song.*
import meteordevelopment.meteorclient.utils.player.*
import meteordevelopment.meteorclient.utils.render.*
import meteordevelopment.meteorclient.utils.render.color.*
import meteordevelopment.meteorclient.utils.world.BlockUtils
import net.greemdev.meteor.*
import net.greemdev.meteor.util.StringScope
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.misc.Nbt
import net.greemdev.meteor.util.string
import net.greemdev.meteor.util.text.*
import net.minecraft.item.ItemStack
import net.minecraft.text.*

val pythonScriptBase by invoking {
    runPythonBaseGenerator()
    pyBaseFile.readText()
}

private val pyBaseFile by invoking { MeteorClient.FOLDER / "evalCommandScriptBase.py" }

private fun needsGeneration() =
    if (!pyBaseFile.exists())
        true
    else {
        val currentBaseRevision = pyBaseFile.readLines().first().split(" ").last()

        currentBaseRevision.all(Char::isDigit) && currentBaseRevision.toInt() != MeteorClient.REVISION
    }


private fun generateEvalScriptBase(): String {
    fun StringScope.def(name: String, args: Array<String>, body: Getter<String>) {
        +"def $name(${args.joinToString(", ")}):".newline()
        body().trimIndent()
            .lines()
            .map { " ".repeat(4) + it }
            .forEach(::appendln)
        +newline()
    }

    return string {
        appendln("# **THIS IS A GENERATED FILE, DO NOT MODIFY** ${MeteorClient.REVISION}")

        getPythonImports()
            .forEach { (packageName, classes) ->
                appendln("from $packageName import ${classes.joinToString(", ")}")
            }


        +lines(2)

        appendlns(
            "mc = MeteorClient.mc",
            "true = True",
            "false = False",
            "null = None"
        )

        appendln()

        def("info", arrayOf("content")) {
            "ChatUtils.info(str(content))"
        }

        def("speak", arrayOf("message")) {
            "Narrator.getNarrator().say(str(message))"
        }

        +"{{{SCRIPT}}}"
    }
}

fun runPythonBaseGenerator() {
    if (needsGeneration())
        pyBaseFile.writeText(generateEvalScriptBase())
}

fun getPythonImports() =
    buildMap<String, Array<String>> {
        Imports.forEach {
            compute(it.packageName) { _, value ->
                // when run through the generator, the Minecraft classes in the python imports reflect the obfuscated names.
                // jython seems to work with the mapped names and not the ones actually available at runtime (like class_1522 or similar)
                // thus, we need to map the class names
                // simpleNameOrMappingName returns class simple name and if package is from net.minecraft then the canonical name is mapped then is stripped down to its simple name and returned
                // (I am so glad Fabric exposes the MappingResolver to developers, other option is hardcoded qualified names :LMFAO:)

                (value ?: arrayOf()) + it.simpleNameOrMappingName
            }
        }
    }

// Class.forName is used for kotlin top-level declaration files since you can't ::class.java them in Kotlin itself as the JvmName is only visible to java

val Imports = arrayOf<Class<*>>(
    Class.forName("net.greemdev.meteor.utils"),

    Class.forName("net.greemdev.meteor.util.Strings"),

    FormattedText::class.java, ChatColor::class.java, actions::class.java,
    Class.forName("net.greemdev.meteor.util.text.ChatEvents"),

    Nbt::class.java,
    Class.forName("net.greemdev.meteor.util.misc.NbtUtil"),
    Class.forName("net.greemdev.meteor.util.misc.KMC"),

    HiddenModules::class.java, Meteor::class.java, Prompts::class.java,

    Utils::class.java,

    EntityUtils::class.java, TargetUtils::class.java,

    StreamUtils::class.java,

    BaritoneUtils::class.java, Keybind::class.java, NbtUtils::class.java,
    MeteorIdentifier::class.java, MeteorStarscript::class.java,
    Vec2::class.java, Vec3::class.java, Vec4::class.java,

    Input::class.java, KeyAction::class.java, KeyBinds::class.java,

    TextUtils::class.java,

    PacketUtils::class.java, Http::class.java, Capes::class.java,

    NotebotUtils::class.java,

    Note::class.java, Song::class.java,

    NBSSongDecoder::class.java, SongDecoder::class.java, SongDecoders::class.java, TextSongDecoder::class.java,

    ChatUtils::class.java, DamageUtils::class.java, EChestMemory::class.java, InvUtils::class.java, Rotations::class.java, SlotUtils::class.java,

    FontUtils::class.java, NametagUtils::class.java, PlayerHeadUtils::class.java, RenderUtils::class.java,

    MeteorColor::class.java, RainbowColor::class.java, RainbowColors::class.java, SettingColor::class.java,

    BlockUtils::class.java,

    MeteorClient::class.java,

    ItemStack::class.java,

    Narrator::class.java,

    // net.minecraft classes

    Text::class.java, MutableText::class.java, ClickEvent::class.java, HoverEvent::class.java
)
