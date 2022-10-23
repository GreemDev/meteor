/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.network.Http
import meteordevelopment.meteorclient.utils.network.MeteorExecutor
import meteordevelopment.meteorclient.utils.player.ChatUtils
import meteordevelopment.meteorclient.utils.player.PlayerUtils
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.CommandBuilder
import net.greemdev.meteor.commands.api.invoke
import net.greemdev.meteor.util.format
import net.greemdev.meteor.util.meteor.sendJson
import net.greemdev.meteor.util.misc.currentWorld
import net.greemdev.meteor.util.text.*
import java.text.SimpleDateFormat
import java.util.Date

object NameHistoryCommand : GCommand(
    "name-history", "Provides a list of a player's previous names from the laby.net API.",
    "history", "names"
) {
    override fun CommandBuilder.inject() {
        then("player", arg.playerListEntry()) {
            alwaysRuns {
                MeteorExecutor.execute {
                    val target by it(arg.playerListEntry(), "player")

                    val history = Http.get("https://laby.net/api/v2/user/${target.profile.id}/get-profile").sendJson<NameHistory>()
                    if (history == null || history.username_history.isNullOrEmpty()) {
                        error("There was an error fetching that player's name history.")
                        return@execute
                    }

                    info(buildText {
                        addString(target.profile.name)
                        if (target.profile.name.endsWith('s'))
                            addString("'")
                        else
                            addString("'s")

                        colored(PlayerUtils.getPlayerColor(mc.currentWorld().getPlayerByUuid(target.profile.id), Color.WHITE))

                        clicked(actions.openURL, "https://laby.net/@${target.profile.name}")

                        hovered(actions.showText, buildText {
                            addString("View on laby.net")
                            colored(yellow).italicized()
                        })

                        addString(" Username History:", grey)
                    })

                    history.username_history.forEach {
                        ChatUtils.sendMsg(buildText {
                            addString(it.name, aqua)

                            if (it.changed_at != null && it.changed_at.time != 0L) {
                                hovered(actions.showText, buildText {
                                    addString("Changed at: ", grey)
                                    addString(it.changed_at.format("hh:mm:ss, dd/MM/yyyy"), white)
                                })
                            }

                            if (!it.accurate) {
                                addString("*") {
                                    colored(white)
                                    hovered(actions.showText, textOf("This name history entry is not accurate according to laby.net"))
                                }
                            }

                        })
                    }

                }
            }
        }
    }
}

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
