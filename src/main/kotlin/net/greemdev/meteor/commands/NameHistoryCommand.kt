/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.contextArg
import net.greemdev.meteor.format
import net.greemdev.meteor.util.HTTP
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.*
import net.greemdev.meteor.util.text.*
import java.util.Date

object NameHistoryCommand : GCommand(
    "name-history",
    "Provides a list of a player's previous names from the laby.net API.", {
        then("player", ArgType.playerListEntry()) {
            runs {
                val target by contextArg("player", ArgType.playerListEntry())

                (HTTP GET "https://laby.net/api/v2/user/${target.profile.id}/get-profile")
                    .requestFutureJson<NameHistory>()
                    .thenAccept { history ->
                        if (history?.username_history.isNullOrEmpty()) {
                            error("There was an error fetching that player's name history.")
                            return@thenAccept
                        }

                        info {
                            addString(target.profile.name)
                            addString(
                                if (target.profile.name.endsWith('s'))
                                    "'"
                                else
                                    "'s"
                            )

                            colored(minecraft.currentWorld().getPlayerByUuid(target.profile.id).getColor())

                            clicked(actions.openURL, "https://laby.net/@${target.profile.name}")

                            hoveredText(
                                FormattedText.withColor("View on laby.net", ChatColor.yellow)
                                    .italicized()
                            )

                            addString(" Username History:", ChatColor.grey)
                        }

                        history!!.username_history!!.forEach { nameEntry ->
                            sendMeteorMessage {
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
                                    hoveredText("This name history entry is not accurate according to laby.net")
                                }
                            }
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
