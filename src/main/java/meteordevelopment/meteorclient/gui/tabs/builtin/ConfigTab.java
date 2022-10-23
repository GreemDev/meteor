/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.misc.MeteorIdentifier;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.greemdev.meteor.util.meteor.HiddenModules;
import net.greemdev.meteor.util.meteor.Prompts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class ConfigTab extends Tab {
    public static final String NAME = "Config";

    public ConfigTab() {
        super(NAME, GuiRenderer.COG);
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        Config.get().hiddenModules.set(HiddenModules.getModules());
        return new ConfigScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof ConfigScreen;
    }

    public static class ConfigScreen extends WindowTabScreen {
        private final Settings settings;

        public ConfigScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            settings = Config.get().settings;
            settings.onActivated();

            onClosed(() -> {
                String prefix = Config.get().prefix.get();

                var prompts = Prompts.java(theme, parent);

                if (prefix.isBlank()) {
                    prompts.showConfirm("empty-command-prefix", p ->
                        p.title("Empty command prefix")
                            .message("You have set your command prefix to nothing.")
                            .message("This WILL prevent you from sending chat messages.")
                            .message("Do you want to reset your prefix to '" + Command.DEFAULT_PREFIX + "'?")
                            .onYes(() -> Config.get().prefix.reset())
                    );
                } else if (prefix.equals("/")) {
                    prompts.showConfirm("minecraft-prefix-conflict", p ->
                        p.title("Potential prefix conflict")
                            .message("You have set your command prefix to '/', which is used by Minecraft.")
                            .message("This can cause conflict issues between Meteor and Minecraft commands.")
                            .message("Do you want to reset your prefix to '" + Command.DEFAULT_PREFIX + "'?")
                            .onYes(() -> Config.get().prefix.reset())
                    );
                } else if (prefix.length() > 7) {
                    prompts.showConfirm("long-command-prefix",
                        p ->
                            p.title("Long command prefix")
                                .message("You have set your command prefix to a very long string.")
                                .message("This means that in order to execute any command, you will need to type %s followed by the command you want to run.", prefix)
                                .message("Do you want to reset your prefix to '" + Command.DEFAULT_PREFIX + "'?")
                                .onYes(() -> Config.get().prefix.reset())
                    );
                }
            });
        }

        @Override
        public void initWidgets() {
            add(theme.settings(settings)).expandX();
        }

        @Override
        public void tick() {
            super.tick();

            settings.tick(window, theme);
            if (WTopBar.NEEDS_REFRESH) {
                reloadTopBar();
                WTopBar.NEEDS_REFRESH = false;
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Config.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Config.get());
        }
    }
}
