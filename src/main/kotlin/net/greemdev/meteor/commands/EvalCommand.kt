/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import kotlinx.coroutines.launch
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.*

object EvalCommand :
    GCommand("eval", "Evaluate python code, a file, or a directory of .py files at the given file path.", {
        then("code", arg.greedyString()) {
            alwaysRuns {
                val code by it(arg.greedyString(), "code")
                _evalInternal(pythonScripts(code))
            }
        }
        then("file") {
            then("path", arg.path()) {
                alwaysRuns {
                    val path by it(arg.path(), "path")
                    _evalInternal(pythonPaths(path))
                }
            }
        }
    }, "py", "exec")

private fun _evalInternal(op: PythonOp<*>) {
    scope.launch {
        val (_, err) = catchErrors {
            op.exec()
        }
        if (err != null)
            EvalCommand.error("Script failed: ${err.message ?: "No message."}")
    }
}
