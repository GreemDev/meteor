/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.screens;

import kotlin.text.StringsKt;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class ModuleScreen extends WindowScreen {
    private final Module module;

    private WContainer settingsContainer;
    private WKeybind keybind;

    public ModuleScreen(GuiTheme theme, Module module) {
        super(theme, theme.favorite(module.favorite, c ->
            module.favorite = c
        ), module.title);

        this.module = module;
    }

    @Override
    public void initWidgets() {
        // Description
        StringsKt.split(module.description, new char[]{'\n'}, false, 0)
            .forEach(line ->
                add(theme.label(line, getWindowWidth() / 2.0))
            );

        // Settings
        if (module.settings.groups.size() > 0) {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(module.settings)).expandX();
        }

        // Custom widget
        WWidget widget = module.getWidget(theme);

        if (widget != null) {
            add(theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
        }

        // Bind
        within(add(theme.section("Bind", true)).expandX(), sec -> {
            if (module.canBind && module.canActivate)
                keybind = sec.add(theme.moduleKeybind(module.keybind, () ->
                    Modules.get().setModuleToBind(module))
                ).expandX().widget();

            if (module.allowChatFeedback || module.forceDisplayChatFeedbackCheckbox) {
                within(sec.add(theme.horizontalList()), list -> {
                    list.add(theme.label("Chat Feedback: "));
                    list.add(theme.checkbox(module.chatFeedback, (c) ->
                        module.chatFeedback = c)
                    );
                });
            }
        });
        add(theme.horizontalSeparator()).expandX();


        if (module.canActivate) {
            // Bottom
            within(add(theme.horizontalList()).expandX(), list -> {
                // Active
                list.add(theme.label("Active: "));
                list.add(theme.checkbox(module.isActive(), checked -> {
                    if (module.isActive() != checked) {
                        module.toggle();
                        reload();
                    }
                })).expandCellX();
            });
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !Modules.get().isBinding();
    }

    @Override
    public void tick() {
        super.tick();

        module.settings.tick(settingsContainer, theme);
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        if (module.canBind)
            keybind.reset();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(module.title, module.toTag());
    }

    @Override
    public boolean fromClipboard() {
        NbtCompound clipboard = NbtUtils.fromClipboard(module.toTag());

        if (clipboard != null) {
            module.fromTag(clipboard);
            return true;
        }

        return false;
    }
}
