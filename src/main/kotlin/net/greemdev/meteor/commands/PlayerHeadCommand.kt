/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import com.mojang.authlib.GameProfile
import com.mojang.util.UndashedUuid
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.utils.player.InvUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.CommandExceptions
import net.greemdev.meteor.commands.api.contextArg
import net.greemdev.meteor.undashedString
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.createRandomNbtIntArray
import net.greemdev.meteor.util.misc.player
import net.greemdev.meteor.util.text.textOf
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader
import kotlin.random.Random

// based on https://github.com/cally72jhb/vector-addon/blob/main/src/main/java/cally72jhb/addon/commands/commands/PlayerHeadCommand.java

object PlayerHeadCommand : GCommand("player-head", "Gives you a player's current head. Creative mode only.", {
    runs { scope.launch { execute(minecraft.player().gameProfile, 1) } }

    then("player", ArgType.playerListEntry()) {
        runs {
            val player by contextArg("player", ArgType.playerListEntry())
            scope.launch { execute(player.profile, 1) }
        }
        then("amount", ArgType.int(1, 64)) {
            runs {
                val player by contextArg("player", ArgType.playerListEntry())
                val amount by contextArg("amount", ArgType.int(1, 64))
                scope.launch { execute(player.profile, amount) }
            }
        }
    }
})

private val notCreative by CommandExceptions simple "You must be in creative mode to use this."

private suspend fun GCommand.execute(player: GameProfile, amount: Int) {

    require(minecraft.player().abilities.creativeMode, notCreative)

    val profileResponse = (HTTP GET "https://sessionserver.mojang.com/session/minecraft/profile/${player.id.undashedString}")
        .requestJsonAsync().await() ?: return

    val newItemStack = ItemStack(Items.PLAYER_HEAD)

    newItemStack.nbt = StringNbtReader.parse(minify("""{
                    SkullOwner: {
                        Id:${Random(player.name.hashCode()).createRandomNbtIntArray(4)},
                        Properties: {
                            textures: [
                                {
                                    Value: "${profileResponse.get("properties").asJsonArray.get(0).asJsonObject.get("value").asString}"
                                }
                            ]
                        }
                    }
               }"""))

    newItemStack.count = amount.coerceIn(1, 64)

    if (!InvUtils.tryInsertStack(newItemStack))
        error("Not enough space in your inventory.")
}
