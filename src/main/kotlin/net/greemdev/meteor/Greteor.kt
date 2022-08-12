/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.MeteorClient
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
    fun lambdaFactory() {
        MeteorClient.EVENT_BUS.registerLambdaFactory("net.greemdev.meteor") { lookupInMethod, klass ->
            lookupInMethod(null, klass, MethodHandles.lookup())
                as MethodHandles.Lookup
        }
    }
}
