/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.python

import com.enderzombi102.jythonmc.Jython
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.utils.PostInit
import net.greemdev.meteor.*
import net.greemdev.meteor.util.scope
import org.python.util.PythonInterpreter
import java.io.File
import java.io.StringReader
import java.nio.file.Path

@Suppress("ClassName")
object py {
    fun files(vararg files: File) = PythonOp(files.toList())
    fun paths(vararg paths: Path) = PythonOp(paths.toList())
    fun scripts(vararg scripts: String) = PythonOp(scripts.toList())
}

class PythonOp(codeData: Any) {
    private val scripts: List<String>
    init {
        scripts = when (codeData) {
            is String -> listOf(codeData)
            is File -> load(codeData)
            is Path -> load(codeData.toFile())
            is StringReader -> listOf(codeData.readText())
            is Iterable<*> -> {
                buildList {
                    codeData.forEach {
                        when (it) {
                            is File -> addAll(load(it))
                            is String -> add(it)
                            is Path -> addAll(load(it.toFile()))
                        }
                    }
                }
            }
            else -> error(
                """
                PythonOp input data is of invalid type.
                Valid types: StringReader, String, File, Path, Iterable<String>, Iterable<File>, Iterable<Path>
                """.trimIndent()
            )
        }
    }

    private fun getCodeFromFiles(files: List<File>?) = files?.map(File::readText).orEmpty()

    private fun getCodeFromDirectory(directory: File) =
        getCodeFromFiles(directory.filter {
            it.name.endsWith(".py") && !it.isDirectory
        })

    private fun load(file: File): List<String> =
        if (!file.exists())
            emptyList()
        else if (file.isDirectory)
            getCodeFromDirectory(file)
        else
            listOf(file.readText())


    fun exec(insertIntoBase: Boolean = true, async: Boolean = false, post: (PythonInterpreter.() -> Unit)? = null) {
        if (async)
            scope.launch { _exec(insertIntoBase, post) }
        else
            _exec(insertIntoBase, post)
    }

    private fun _exec(insertIntoBase: Boolean, post: Initializer<PythonInterpreter>?) =
        python {
            if (scripts.size == 1)
                _execInternal(scripts.first(), insertIntoBase)
            else
                scripts.forEach { code ->
                    _execInternal(code, insertIntoBase)
                }

            post?.invoke(this)
        }


    private fun PythonInterpreter._execInternal(code: String, insertIntoBase: Boolean) {
        compile(
            if (insertIntoBase)
                pythonScriptBase.replace("{{{SCRIPT}}}", code)
            else
                code
        )?.also(::exec)
    }
}

private val pythonInterpreter by invoking {
    PythonInterpreter().apply {
        setErr(System.err)
        setOut(System.out)
        setIn(System.`in`)
    }
}

fun python(func: Initializer<PythonInterpreter>) = pythonInterpreter.use(func)

@PostInit
private fun postInit() {
    Jython.initSystem()
}
