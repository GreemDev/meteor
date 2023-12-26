/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import meteordevelopment.meteorclient.gui.tabs.builtin.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.java;
import net.greemdev.meteor.gui.tab.WaypointsTab;
import net.greemdev.meteor.utils;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Tabs {
    private static Tab _lastTab;

    public static void setLastTab(@NotNull Tab tab) {
        if (Config.get().lastTabMemory.get()) {
            if (tab.equals(_lastTab))
                _lastTab = tab;
        } else if (_lastTab != null) {
            _lastTab = null;
        }
    }

    public static Tab getTabToOpen() {
        return Config.get().lastTabMemory.get() && _lastTab != null
            ? _lastTab
            : modules();
    }
    private static final List<Tab> tabs = new ArrayList<>();

    public static Pair<List<Tab>, List<Tab>> renderSections() {
        ArrayList<Tab> right = get().stream().filter(tab -> tab.displayIcon.get())
            .collect(Collectors.toCollection(ArrayList::new));
        //Config is added at the end to ensure it's always at the very right of the top bar
        right.removeIf(t -> t instanceof ConfigTab);
        right.add(config());

        return new Pair<>(
            get().stream().filter(tab -> !tab.displayIcon.get()).toList(),
            right
        );
    }

    public static <T extends Tab> T get(String name) {
        return java.cast(
            tabs.stream()
                .filter(t -> t.name.equals(name))
                .findFirst()
                .orElse(null)
        );
    }

    @NotNull
    public static ModulesTab modules() {
        return get(ModulesTab.NAME);
    }

    @NotNull
    public static GuiTab gui() {
        return get(GuiTab.NAME);
    }

    @NotNull
    public static HudTab hud() {
        return get(HudTab.NAME);
    }

    @NotNull
    public static FriendsTab friends() {
        return get(FriendsTab.NAME);
    }

    @NotNull
    public static MacrosTab macros() {
        return get(MacrosTab.NAME);
    }

    @NotNull
    public static ProfilesTab profiles() {
        return get(ProfilesTab.NAME);
    }

    @NotNull
    public static BaritoneTab baritone() {
        return get(BaritoneTab.NAME);
    }

    @NotNull
    public static WaypointsTab waypoints() {
        return get(WaypointsTab.NAME);
    }

    @NotNull
    public static ConfigTab config() {
        return get(ConfigTab.NAME);
    }

    static {
        add(new ModulesTab());
        add(new ConfigTab());
        add(new GuiTab());
        add(new HudTab());
        add(new FriendsTab());
        add(new MacrosTab());
        add(new ProfilesTab());
        add(new BaritoneTab());
        add(new WaypointsTab());
    }

    public static void add(Tab tab) {
        tabs.add(tab);
    }

    public static List<Tab> get() {
        return new ObjectImmutableList<>(tabs);
    }
}
