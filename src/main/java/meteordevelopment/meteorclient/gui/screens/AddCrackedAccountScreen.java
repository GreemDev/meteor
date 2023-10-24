/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import net.greemdev.meteor.utils;

public class AddCrackedAccountScreen extends AddAccountScreen {
    public AddCrackedAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Cracked Account", parent);
    }

    @Override
    public void initWidgets() {
        within(add(theme.table()), t -> {
            // Name
            t.add(theme.label("Name: "));
            WTextBox name = t.add(theme.textBox("", MeteorClient.randomAuthor().getName(), (text, c) ->
                // Username can't contain spaces
                c != ' '
            )).minWidth(400).expandX().widget();
            name.setFocused(true);
            t.row();

            // Add
            add = t.add(theme.button("Add", () -> {
                if (!name.get().isEmpty() && (name.get().length() < 17) && name.get().matches("^[a-zA-Z0-9_]+$")) {
                    CrackedAccount account = new CrackedAccount(name.get());
                    if (!(Accounts.get().exists(account))) {
                        AccountsScreen.addAccount(this, parent, account);
                    }
                }
            })).expandX().widget();

            enterAction = add.action;
        });
    }
}
