/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.MeteorColor
import net.greemdev.meteor.commands.api.Arguments
import net.greemdev.meteor.commands.api.contextArg
import net.greemdev.meteor.util.meteor.Meteor
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.player
import net.greemdev.meteor.util.misc.showMessage
import net.greemdev.meteor.util.text.*
import java.util.function.Predicate

private val HiddenModulesArgument = Arguments.module(Module::isHidden)
private val VisibleModulesArgument = Arguments.module(Predicate.not(Module::isHidden))
private const val ModulesPerLine = 7
private const val ListCommand = "list"
private const val AddCommand = "add"
private const val RemoveCommand = "remove"
private const val ToggleCommand = "toggle"

private val Module.unhideCommand
    get() = HiddenModulesCommand.subcommand("$RemoveCommand $name")

object HiddenModulesCommand : GCommand("hidden-modules", "List, add, remove, or toggle hidden modules.", {
    then(ListCommand) {
        runs {
            val modules = Meteor.modules().allHidden
            if (modules.isEmpty()) {
                info("You have no modules hidden.")
                return@runs
            }

            info("The following modules are currently hidden:")
            modules.chunked(ModulesPerLine)
                .forEach {
                    minecraft.showMessage {
                        colored(ChatColor.grey)
                        it.forEachIndexed { i, mdl ->
                            addString(mdl.name) {
                                colored(mdl.color)
                                hoveredText("Click to unhide this module.")
                                clicked(actions.runCommand, mdl.unhideCommand)
                            }
                            if (i < it.lastIndex)
                                addString(", ")
                        }
                    }
                }
        }
    }

    then(AddCommand) {
        then(VisibleModulesArgument) {
            runs {
                val module by contextArg(VisibleModulesArgument)

                if (module.isHidden) return@runs
                module.isHidden = true

                info {
                    colored(ChatColor.grey)
                    addString("That module is now ")
                    addText(visibilityText(true))
                    addString(". ")
                    addCommandHyperlink("Click here to unhide.", module.unhideCommand)
                }
            }
        }
    }

    then(RemoveCommand) {
        then(HiddenModulesArgument) {
            runs {
                val module by contextArg(HiddenModulesArgument)

                if (!module.isHidden) return@runs
                module.isHidden = false

                info {
                    colored(ChatColor.grey)
                    addString("That module is now ")
                    addText(visibilityText(false))
                    addString(". ")
                    addCommandHyperlink("Click here to hide.", subcommand("$AddCommand ${module.name}"))
                }
            }
        }
    }

    then(ToggleCommand) {
        then(ArgType.module()) {
            runs {
                val module by contextArg(ArgType.module())
                module.toggleHidden()

                info {
                    colored(ChatColor.grey)
                    addString("That module is now ")

                    addText(visibilityText(module.isHidden))

                    addString(". ")
                    addCommandHyperlink("Click here to undo.", subcommand("$ToggleCommand ${module.name}"))
                }
            }
        }
    }
})

private fun visibilityText(isHidden: Boolean) =
    FormattedText.withColor(
        if (isHidden) "hidden" else "visible",
        if (isHidden) ChatColor.red else ChatColor.green
    )
