/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render.prompts;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.config.Config;
import net.greemdev.meteor.type.MeteorPromptException;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class OkPrompt {
    private final GuiTheme theme;
    private final Screen parent;

    private String title = "";
    private final List<String> messages = new ArrayList<>();
    private String id = null;

    private Runnable onOk = () -> {};

    private OkPrompt() {
        this(GuiThemes.get(), mc.currentScreen);
    }

    private OkPrompt(GuiTheme theme, Screen parent) {
        this.theme = theme;
        this.parent = parent;
    }

    public static OkPrompt create() {
        return new OkPrompt();
    }

    public static OkPrompt create(GuiTheme theme, Screen parent) {
        return new OkPrompt(theme, parent);
    }

    public OkPrompt title(String title) {
        this.title = title;
        return this;
    }

    public OkPrompt message(String message) {
        this.messages.add(message);
        return this;
    }

    public OkPrompt message(String message, Object... args) {
        this.messages.add(String.format(message, args));
        return this;
    }

    public OkPrompt id(String from) {
        this.id = from;
        return this;
    }

    public OkPrompt onOk(Runnable action) {
        this.onOk = action;
        return this;
    }

    public boolean show() {
        if (id == null) this.id(this.title);
        if (Config.get().dontShowAgainPrompts.contains(id)) return false;

        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> mc.setScreen(new PromptScreen(theme)));
        }
        else {
            mc.setScreen(new PromptScreen(theme));
        }
        return true;
    }

    public void error() throws MeteorPromptException {
        error(null);
    }

    public void error(@Nullable Throwable cause) throws MeteorPromptException {
        throw new MeteorPromptException(cause, () -> this);
    }

    private class PromptScreen extends WindowScreen {
        public PromptScreen(GuiTheme theme) {
            super(theme, OkPrompt.this.title);

            this.parent = OkPrompt.this.parent;
        }

        WCheckbox dontShowAgainCheckbox;

        @Override
        public void initWidgets() {
            for (String line : messages)
                add(theme.label(line)).expandX();

            add(theme.horizontalSeparator()).expandX();
            add(theme.horizontalList(), (cell, list) -> {
                cell.expandX();

                dontShowAgainCheckbox = list.add(theme.checkbox(false)).widget();
                list.add(theme.label("Don't show this again.")).expandX();
            });

            add(theme.horizontalList(), (cell, list) -> {
                cell.expandX();

                list.add(theme.button("Ok", () -> {
                    if (dontShowAgainCheckbox.checked) Config.get().dontShowAgainPrompts.add(id);
                    onOk.run();
                    close();
                }));
            });
        }
    }
}
