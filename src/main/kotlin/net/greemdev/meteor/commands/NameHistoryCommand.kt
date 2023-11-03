/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.network.MeteorExecutor
import meteordevelopment.meteorclient.utils.player.ChatUtils
import meteordevelopment.meteorclient.utils.player.PlayerUtils
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.argument
import net.greemdev.meteor.format
import net.greemdev.meteor.util.HTTP
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.currentWorld
import net.greemdev.meteor.util.text.*
import java.util.Date

object NameHistoryCommand : GCommand(
    "name-history",
    "Provides a list of a player's previous names from the laby.net API.", {
        then("player", arg.playerListEntry()) {
            alwaysRuns {
                MeteorExecutor.execute {
                    val target by it.argument(arg.playerListEntry(), "player")

                    val history = HTTP.get("https://laby.net/api/v2/user/${target.profile.id}/get-profile").requestJson<NameHistory>()
                    if (history == null || history.username_history.isNullOrEmpty()) {
                        error("There was an error fetching that player's name history.")
                        return@execute
                    }

                    info(buildText {
                        addString(target.profile.name)
                        addString(
                            if (target.profile.name.endsWith('s'))
                                "'"
                            else
                                "'s"
                        )

                        colored(
                            PlayerUtils.getPlayerColor(
                                minecraft.currentWorld().getPlayerByUuid(target.profile.id),
                                Color.WHITE
                            )
                        )

                        clicked(actions.openURL, "https://laby.net/@${target.profile.name}")

                        hoveredText {
                            addString("View on laby.net")
                            colored(ChatColor.yellow).italicized()
                        }

                        addString(" Username History:", ChatColor.grey)
                    })

                    history.username_history.forEach { nameEntry ->
                        ChatUtils.sendMsg(buildText {
                            addString(nameEntry.name, ChatColor.aqua)

                            if (nameEntry.changed_at != null && nameEntry.changed_at.time != 0L) {
                                hoveredText {
                                    addString("Changed at: ", ChatColor.grey)
                                    addString(nameEntry.changed_at.format("hh:mm:ss, dd/MM/yyyy"), ChatColor.white)
                                }
                            }

                            addString(
                                "*".takeIf { nameEntry.accurate }
                            ) {
                                colored(ChatColor.yellow)
                                hoveredText(textOf("This name history entry is not accurate according to laby.net"))
                            }
                        })
                    }
                }
            }
        }
    },
    "history", "names"
)

private class NameHistory(
    @JvmField
    val username_history: Array<Name>?
)


private class Name(
    @JvmField
    val name: String,
    @JvmField
    val changed_at: Date?,
    @JvmField
    val accurate: Boolean
)
