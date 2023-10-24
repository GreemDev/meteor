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
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HudTab extends Tab {

    public static final String NAME = "HUD";
    public HudTab() {
        super(NAME);
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HudScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HudScreen;
    }

    public static class HudScreen extends WindowTabScreen {
        private final Hud hud;

        public HudScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            hud = Hud.get();
            hud.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            add(theme.settings(hud.settings)).expandX();

            add(theme.horizontalSeparator()).expandX();

            add(theme.button("Edit", () -> mc.setScreen(new HudEditorScreen(theme)))).expandX();

            WHorizontalList buttons = add(theme.horizontalList()).expandX().widget();
            buttons.add(theme.button("Clear", hud::clear)).expandX();
            buttons.add(theme.button("Reset to default elements", hud::resetToDefaultElements)).expandX();

            add(theme.horizontalSeparator()).expandX();

            WHorizontalList bottom = add(theme.horizontalList()).expandX().widget();

            bottom.add(theme.label("Active: "));
            bottom.add(theme.checkbox(hud.active, (checked) -> hud.active = checked)).expandCellX();
            bottom.add(theme.resetButton(hud.settings::reset));
        }

        @Override
        protected void onRenderBefore(float delta) {
            HudEditorScreen.renderElements();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard("hud-settings", hud.settings.toTag());
        }

        @Override
        public boolean fromClipboard() {
            NbtCompound clipboard = NbtUtils.fromClipboard(hud.settings.toTag());

            if (clipboard != null) {
                hud.settings.fromTag(clipboard);
                return true;
            }

            return false;
        }
    }
}
