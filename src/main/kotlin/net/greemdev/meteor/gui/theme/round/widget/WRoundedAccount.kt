/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget

import meteordevelopment.meteorclient.gui.WidgetScreen
import meteordevelopment.meteorclient.gui.widgets.WAccount
import meteordevelopment.meteorclient.systems.accounts.Account
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.gui.theme.round.RoundedWidget

class WRoundedAccount(screen: WidgetScreen, account: Account<*>) : WAccount(screen, account), RoundedWidget {
    override fun loggedInColor(): Color = theme().loggedInColor.get()
    override fun accountTypeColor(): Color = theme().textSecondaryColor()
}
