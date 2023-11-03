/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.args

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.greemdev.meteor.Greteor
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.asPath
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.text.*
import net.minecraft.SharedConstants
import net.minecraft.command.CommandSource
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*

class PathArgumentType private constructor(
    val stringType: ArgumentType<String>,
    val allowDirectories: Boolean
) : ArgumentType<Path> {

    /*override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val suggestions = buildList<Path> {
            if (builder.remaining.isEmpty())
                minecraft.runDirectory.listFiles()?.forEach { add(it.toPath()) }
            else
                net.greemdev.meteor.getOrNull { Path(builder.remaining) }?.also {
                    if (it.isDirectory())
                        it.listDirectoryEntries().forEach(::add)
                }
        }

        if (SharedConstants.isDevelopment)
            Greteor.logger.info(suggestions.joinToString(", ", "[", "]") { it.toString() })

        return builder.matching(suggestions.map { it.fileName.toString() })
    }*/


    companion object {
        @JvmOverloads
        @JvmStatic
        fun create(
            stringType: ArgumentType<String> = Arguments.greedyString(),
            allowDirectories: Boolean = true
        ) = PathArgumentType(stringType, allowDirectories)

        /*@JvmStatic
        fun argSuggests(builder: SuggestionsBuilder, ctx: MinecraftCommandContext): CompletableFuture<Suggestions> {
            val suggestions = buildList<Path> {
                if (builder.remaining.isEmpty())
                    minecraft.runDirectory.listFiles()?.forEach { add(it.toPath()) }
                else
                    //complex kotlin logic spaghetti
                    net.greemdev.meteor.getOrNull {
                        Path(builder.remaining) //get remaining content as a path
                    }?.also {
                        if (it.isDirectory()) //if getting a path object didn't error, and it leads to a directory,
                            it.listDirectoryEntries().forEach(::add) //add files in that directory to the suggestions.

                    } ?: net.greemdev.meteor.getOrNull { // If the previous path object errored,
                        Path(builder.remaining.substringBeforeLast('/')) // get a new path obj, whose path is that of the argument's value before the very last path separator /.
                    }?.also {
                        if (it.isDirectory()) //if getting a path object didn't error, and it leads to a directory,
                            it.listDirectoryEntries().forEach(::add) //add files in that directory to the suggestions
                    }
            }

            if (SharedConstants.isDevelopment)
                Greteor.logger.info(suggestions.joinToString(", ", "[", "]") { it.toString() })

            return builder.matching(suggestions.map { it.fileName.toString() })
        }*/
    }


    override fun parse(reader: StringReader): Path {
        val path = try {
            stringType.parse(reader).asPath()
        } catch (e: InvalidPathException) {
            errorOccurred.throwNew(e)
        }
        if (!path.exists())
            notFound.throwNew(path.absolute())
        if (path.isDirectory() && !allowDirectories)
            disallowedDirectory.throwNew(path.absolute())

        return path
    }
}

private val errorOccurred by CommandExceptions.dynamic<InvalidPathException> { ipe ->
    buildText {
        colored(ChatColor.red)
        addString("The input path '")
        addString(ipe.input) {
            colored(ChatColor.white).underlined()
        }
        addString("' produced an error at index ${ipe.index}, because: ")
        addString(ipe.reason, ChatColor.gold)
    }
}

private val notFound by CommandExceptions dynamic { path ->
    buildText {
        colored(ChatColor.red)
        addString("The file/directory at '")
        addString(path.toString()) {
            colored(ChatColor.white).underlined()
        }
        addString("' cannot be found.")
    }
}

private val disallowedDirectory by CommandExceptions dynamic { path ->
    buildText {
        colored(ChatColor.red)
        addString("Cannot use the path '")
        addString(path.toString()) {
            colored(ChatColor.white).underlined()
        }
        addString("' because it points to a directory, and that is disallowed by this argument.")
    }
}
