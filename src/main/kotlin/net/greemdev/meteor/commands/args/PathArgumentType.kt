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
import net.greemdev.meteor.onFailureOf
import net.greemdev.meteor.util.asPath
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.modLoader
import net.greemdev.meteor.util.text.*
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*

class PathArgumentType @JvmOverloads constructor(
    private val allowDirectories: Boolean = true
) : ArgumentType<Path> {

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val suggestions = buildList<Path> {
            if (builder.remaining.isEmpty())
                minecraft.runDirectory.listFiles()?.forEach { add(it.toPath()) }
            else
                net.greemdev.meteor.getOrNull(builder.remaining::asPath)?.also {
                    if (it.isDirectory())
                        it.listDirectoryEntries().forEach(::add)
                }
        }

        if (modLoader.isDevelopmentEnvironment)
            Greteor.logger.info(suggestions.joinToString(", ", "[", "]") { it.toString() })

        return builder.matching(suggestions.map { it.fileName.toString() })
    }



    override fun parse(reader: StringReader): Path {
        val path = runCatching {
            reader.readRemainingAndUpdateCursor().asPath()
        }.onFailureOf(InvalidPathException::class) {
            invalidPath.throwNew(it)
        }.getOrThrow()

        if (!path.exists())
            notFound.throwNew(path.absolute())
        if (path.isDirectory() && !allowDirectories)
            disallowedDirectory.throwNew(path.absolute())

        return path
    }
}

fun StringReader.readRemainingAndUpdateCursor(): String {
    val result = remaining
    cursor = totalLength
    return result
}

private val invalidPath by CommandExceptions.dynamic<InvalidPathException> { ipe ->
    colored(ChatColor.red)
    addString("The input path ")
    addString("'${ipe.input}'") { colored(ChatColor.white).underlined() }
    addString(" produced an error at index ${ipe.index}, because: ")
    addString(ipe.reason, ChatColor.gold)
}

private val notFound by CommandExceptions.dynamic<Path> { path ->
    colored(ChatColor.red)
    addString("The file/directory at ")
    addString("'${path}'") { colored(ChatColor.white).underlined() }
    addString(" cannot be found.")
}

private val disallowedDirectory by CommandExceptions.dynamic<Path> { path ->
    colored(ChatColor.red)
    addString("Cannot use the path ")
    addString("'${path}'") { colored(ChatColor.white).underlined() }
    addString(" because it points to a directory, and that is disallowed by this command.")
}
