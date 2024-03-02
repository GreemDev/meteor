/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import baritone.api.BaritoneAPI
import baritone.api.IBaritone
import baritone.api.IBaritoneProvider
import meteordevelopment.meteorclient.MeteorClient
import net.fabricmc.loader.api.FabricLoader
import net.greemdev.meteor.invoking
import net.greemdev.meteor.util.meteor.Meteor
import net.minecraft.client.MinecraftClient

@get:JvmName("minecraft")
val minecraft: MinecraftClient by invoking(MinecraftClient::getInstance)
@get:JvmName("meteor")
val meteor: MeteorClient = Meteor.client
@get:JvmName("modLoader")
val modLoader: FabricLoader by invoking(FabricLoader::getInstance)

@get:JvmName("baritoneProvider")
val baritoneProvider: IBaritoneProvider by invoking(BaritoneAPI::getProvider)
@get:JvmName("baritone")
val baritone: IBaritone by invoking(baritoneProvider::getPrimaryBaritone)
