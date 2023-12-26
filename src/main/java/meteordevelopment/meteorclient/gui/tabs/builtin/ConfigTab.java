/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.prompts.YesNoPrompt;
import net.minecraft.client.gui.screen.Screen;

public class ConfigTab extends Tab {
    public static String NAME = "Config";
    public ConfigTab() {
        super(NAME, GuiRenderer.COG);
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
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
                String prefix = Commands.prefix();

                if (prefix.isBlank())
                    YesNoPrompt.create(theme, this.parent)
                        .displayRequired(true)
                        .title("Empty command prefix")
                        .messageLines(
                            "You have set your command prefix to nothing.",
                            "This WILL prevent you from sending chat messages.",
                            "Do you want to reset your prefix back to '%s'?".formatted(Config.get().prefix.getDefaultValue())
                        )
                        .onYes(() -> Config.get().prefix.reset())
                        .show();
                else if (prefix.equals("/"))
                    YesNoPrompt.create(theme, this.parent)
                        .displayRequired(true)
                        .title("Potential prefix conflict")
                        .messageLines(
                            "You have set your command prefix to '/', which is used by Minecraft.",
                            "This can cause conflict issues between Meteor and Minecraft commands.",
                            "Do you want to reset your prefix back to '%s'?".formatted(Config.get().prefix.getDefaultValue())
                        )
                        .onYes(() -> Config.get().prefix.reset())
                        .show();
                else if (prefix.length() > 7)
                    YesNoPrompt.create(theme, this.parent)
                        .displayRequired(true)
                        .title("Long command prefix")
                        .messageLines(
                            "You have set your command prefix to a very long string.",
                            "This means that in order to execute any command, you will need to type '%s' followed by the command you want to run.".formatted(prefix),
                            "Do you want to reset your prefix back to '%s'?".formatted(Config.get().prefix.getDefaultValue())
                        )
                        .onYes(() -> Config.get().prefix.reset())
                        .show();
            });
        }

        @Override
        public void initWidgets() {
            add(theme.settings(settings)).expandX();
        }

        @Override
        public void tick() {
            settings.tick(window, theme);

            WTopBar.tick(root);
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
