/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import kotlinx.coroutines.launch
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.python.PythonOp
import net.greemdev.meteor.util.python.py

object EvalCommand : GCommand(
    "eval",
    "Evaluate python code, a file, or a directory of .py files at the given file path.", {
        then("code", arg.greedyString()) {
            alwaysRuns {
                val code by it.argument(arg.greedyString(), "code")
                _evalInternal(py.scripts(code))
            }
        }
        then("file") {
            then("path", arg.path()) {
                alwaysRuns {
                    val path by it.argument(arg.path(), "path")
                    _evalInternal(py.paths(path))
                }
            }
        }
    }, "py", "exec")

private fun _evalInternal(op: PythonOp) {
    scope.launch {
        runCatching {
            op.exec()
        }.onFailure {
            EvalCommand.error("Script failed: ${it.message ?: "No message."}")
        }
    }
}
