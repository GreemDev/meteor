/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import com.enderzombi102.jythonmc.Jython
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.utils.PostInit
import net.greemdev.meteor.util.misc.getMeteorResource
import org.python.util.PythonInterpreter
import java.io.File
import java.io.StringReader
import java.nio.file.Path

fun pythonFiles(vararg files: File) = PythonOp(files.toList())
fun pythonPaths(vararg paths: Path) = PythonOp(paths.toList())
fun pythonScripts(vararg scripts: String) = PythonOp(scripts.toList())

class PythonOp<T>(codeData: T) {
    private val scripts: List<String>
    init {
        scripts = when (codeData) {
            is String -> listOf(codeData)
            is File -> getCodeFrom(codeData)
            is Path -> getCodeFrom(codeData.toFile())
            is StringReader -> listOf(codeData.readText())
            is Iterable<*> -> {
                buildList {
                    codeData.forEach {
                        when (it) {
                            is File -> addAll(getCodeFrom(it))
                            is String -> add(it)
                            is Path -> addAll(getCodeFrom(it.toFile()))
                        }
                    }
                }
            }
            else -> error("PythonOp input data is of invalid type.")
        }
    }

    private fun getCodeFromFiles(files: List<File>?) = files?.map { it.readText() }

    private fun getCodeFrom(file: File): List<String> {
        if (!file.exists())
            return emptyList()

        return if (file.isDirectory)
            getCodeFromFiles(file.listed {
                it.name.endsWith(".py")
            }).orEmpty()
        else
            listOf(file.readText())
    }

    fun exec(insertIntoBase: Boolean = true, async: Boolean = false, post: (PythonInterpreter.() -> Unit)? = null) {
        if (async)
            scope.launch { _exec(insertIntoBase, post) }
        else
            _exec(insertIntoBase, post)
    }

    private fun _exec(insertIntoBase: Boolean, post: (PythonInterpreter.() -> Unit)?) {
        python {
            if (scripts.size == 1)
                _execInternal(scripts.first(), insertIntoBase)
            else
                scripts.forEach { code ->
                    _execInternal(code, insertIntoBase)
                }

            post?.invoke(this)
        }
    }

    private fun PythonInterpreter._execInternal(code: String, insertIntoBase: Boolean) {
        compile(
            if (insertIntoBase)
                pythonBaseFile.replace("{{{SCRIPT}}}", code)
            else
                code
        )?.also(::exec)
    }
}

val pythonBaseFile = with(minecraft.getMeteorResource("base.py").get().reader) {
    readText().also { close() }
}


private val pyinterp by invoking {
    PythonInterpreter().apply {
        setErr(System.err)
        setOut(System.out)
        setIn(System.`in`)
    }
}

fun python(func: PythonInterpreter.() -> Unit) = using(pyinterp) {
    func()
}

@PostInit
fun postInit() {
    Jython.initSystem()
}
