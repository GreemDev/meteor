/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.addons.AddonManager
import meteordevelopment.meteorclient.addons.MeteorAddon
import meteordevelopment.meteorclient.systems.commands.Command
import meteordevelopment.meteorclient.systems.modules.*
import net.greemdev.meteor.util.*
import net.minecraft.item.Items
import java.lang.invoke.MethodHandles

abstract class GModule(name: String, description: String) : Module(Greteor.moduleCategory(), name, description)

object Greteor {

    private val category = Category("Greteor", Items.DARK_PRISMARINE.defaultStack)

    fun moduleCategory() = category

    @JvmStatic
    fun modules() {
        createSubtypesOf<GModule>("net.greemdev.meteor.modules")
            .forEach(Meteor.modules()::add)
    }

    @JvmStatic
    fun commands() {
        createSubtypesOf<Command>("net.greemdev.meteor.commands")
            .forEach(Meteor.commands()::add)
    }

    @JvmStatic
    fun categories() {
        Modules.registerCategory(moduleCategory())
    }

    @JvmStatic
    fun getAddonPackages(): List<String> =
        buildList {
            add(MeteorClient.ADDON.`package`)
            addAll(AddonManager.ADDONS.map(MeteorAddon::getPackage))
        }


    @JvmStatic
    fun lambdaFactoriesFor(packages: List<String>, vararg extraPackages: String) =
        (packages + extraPackages).forEach {
            MeteorClient.EVENT_BUS.registerLambdaFactory(it) { lookupInMethod, klass ->
                lookupInMethod(null, klass, MethodHandles.lookup())
                    as MethodHandles.Lookup
            }
        }
}
