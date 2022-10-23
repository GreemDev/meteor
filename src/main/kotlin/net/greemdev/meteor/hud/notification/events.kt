/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.notification

import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.hud.element.NotificationHud

fun NotificationEvent?.canDisplay(hud: NotificationHud) = this?.predicate?.invoke(hud) ?: true

sealed class NotificationEvent(val predicate: (NotificationHud) -> Boolean)

data class ModuleToggledNEvent(val module: Module, val newState: Boolean = module.isActive) : NotificationEvent({
    module in it.modules()
})
