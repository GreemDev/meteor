/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.utils.Utils;
import net.greemdev.meteor.util.Strings;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AddHudElementScreen extends WindowScreen {
    private final int x, y;
    private final WTextBox searchBar;

    private Object firstObject;

    public AddHudElementScreen(GuiTheme theme, int x, int y) {
        super(theme, "Add Hud element");

        this.x = x;
        this.y = y;

        searchBar = theme.textBox(Strings.empty);
        searchBar.action = () -> {
            clear();
            initWidgets();
        };

        enterAction = () -> runObject(firstObject);
    }

    @Override
    public void initWidgets() {
        firstObject = null;

        // Search bar
        add(searchBar).expandX();
        searchBar.setFocused(true);

        // Group infos
        Hud hud = Hud.get();
        Map<HudGroup, List<Item>> grouped = new HashMap<>();

        for (HudElementInfo<?> info : hud.infos.values()) {
            if (info.hasPresets() && !searchBar.get().isEmpty()) {
                for (HudElementInfo<?>.Preset preset : info.presets) {
                    String title = info.title + "  -  " + preset.title;
                    if (Utils.searchTextDefault(title, searchBar.get(), false)) grouped.computeIfAbsent(info.group, hudGroup -> new ArrayList<>()).add(new Item(title, info.description, preset));
                }
            }
            else if (Utils.searchTextDefault(info.title, searchBar.get(), false)) grouped.computeIfAbsent(info.group, hudGroup -> new ArrayList<>()).add(new Item(info.title, info.description, info));
        }

        // Create widgets
        for (HudGroup group : grouped.keySet()) {
            WSection section = add(theme.section(group.title())).expandX().widget();

            for (Item item : grouped.get(group)) {
                WHorizontalList l = section.add(theme.horizontalList()).expandX().widget();

                l.add(theme.label(item.title()).tooltip(item.description()));

                Object obj = item.object();

                if (obj instanceof HudElementInfo<?>.Preset preset) {
                    l.add(theme.plus(() -> runObject(preset))).expandCellX().right().widget();

                    if (firstObject == null) firstObject = preset;
                }
                else {
                    HudElementInfo<?> info = (HudElementInfo<?>) obj;

                    Runnable action = () -> runObject(info);

                    l.add(
                        info.hasPresets()
                            ? theme.button(" > ", action)
                            : theme.plus(action)
                    ).expandCellX().right();

                    if (firstObject == null) firstObject = info;
                }
            }
        }
    }

    private void runObject(Object object) {
        if (object == null) return;
        if (object instanceof HudElementInfo<?>.Preset preset) {
            Hud.get().add(preset, x, y);
            close();
        }
        else {
            HudElementInfo<?> info = (HudElementInfo<?>) object;

            if (info.hasPresets()) {
                HudElementPresetsScreen screen = new HudElementPresetsScreen(theme, info, x, y);
                screen.parent = parent;

                mc.setScreen(screen);
            }
            else {
                Hud.get().add(info, x, y);
                close();
            }
        }
    }

    @Override
    protected void onRenderBefore(DrawContext drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }

    private record Item(String title, String description, Object object) {}
}
