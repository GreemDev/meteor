/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render.prompts;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class YesNoPrompt {
    private final GuiTheme theme;
    private final Screen parent;

    private String title = "";
    private final List<String> messages = new ArrayList<>();
    private String id = null;

    private boolean requiredToDisplay = false;

    private Runnable onYes = () -> {};
    private Runnable onNo = () -> {};

    private YesNoPrompt() {
        this(GuiThemes.get(), mc.currentScreen);
    }

    private YesNoPrompt(GuiTheme theme, Screen parent) {
        this.theme = theme;
        this.parent = parent;
    }

    public static YesNoPrompt create() {
        return new YesNoPrompt();
    }

    public static YesNoPrompt create(GuiTheme theme, Screen parent) {
        return new YesNoPrompt(theme, parent);
    }

    public YesNoPrompt title(String title) {
        this.title = title;
        return this;
    }

    public YesNoPrompt message(String message) {
        this.messages.add(message);
        return this;
    }

    public YesNoPrompt message(String message, Object... args) {
        this.messages.add(message.formatted(args));
        return this;
    }

    public YesNoPrompt messageLines(String... lines) {
        this.messages.clear();
        this.messages.addAll(Arrays.stream(lines).toList());
        return this;
    }

    public YesNoPrompt displayRequired(boolean required) {
        this.requiredToDisplay = required;
        return this;
    }

    public YesNoPrompt id(String from) {
        this.id = from;
        return this;
    }

    public YesNoPrompt onYes(Runnable action) {
        this.onYes = action;
        return this;
    }

    public YesNoPrompt onNo(Runnable action) {
        this.onNo = action;
        return this;
    }

    public boolean show() {
        if (id == null) id(this.title);
        if (!requiredToDisplay && Config.get().dontShowAgainPrompts.contains(id)) return false;

        RenderUtils.executeOnRenderThread(() -> mc.setScreen(createScreen()));
        return true;
    }

    public WindowScreen createScreen() {
        WindowScreen screen = new WindowScreen(theme, title) {

            WCheckbox dontShowAgainCheckbox = null;

            @Override
            public void initWidgets() {
                for (String line : messages) add(theme.label(line)).expandX();
                add(theme.horizontalSeparator()).expandX();

                if (!requiredToDisplay) {
                    WHorizontalList checkboxContainer = add(theme.horizontalList()).expandX().widget();
                    dontShowAgainCheckbox = checkboxContainer.add(theme.checkbox()).widget();
                    checkboxContainer.add(theme.label("Don't show this again.")).expandX();
                }

                WHorizontalList list = add(theme.horizontalList()).expandX().widget();

                list.add(theme.button("Yes", () -> {
                    if (!requiredToDisplay) {
                        if (dontShowAgainCheckbox.checked) Config.get().dontShowAgainPrompts.add(id);
                    }
                    onYes.run();
                    close();
                })).expandX();

                list.add(theme.button("No", () -> {
                    if (!requiredToDisplay) {
                        if (dontShowAgainCheckbox.checked) Config.get().dontShowAgainPrompts.add(id);
                    }
                    onNo.run();
                    close();
                })).expandX();
            }
        };
        screen.parent = parent;
        return screen;
    }
}
