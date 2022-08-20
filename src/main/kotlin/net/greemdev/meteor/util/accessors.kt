/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.MeteorClient
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient

val minecraft: MinecraftClient = MeteorClient.mc
val meteor: MeteorClient = MeteorClient.INSTANCE
val modLoader: FabricLoader = FabricLoader.getInstance()
