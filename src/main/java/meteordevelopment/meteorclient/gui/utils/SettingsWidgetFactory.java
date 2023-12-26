/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.utils;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class SettingsWidgetFactory {
    private static final Map<Class<?>, Function<GuiTheme, BiConsumer<WTable, Setting<?>>>> customFactories = new HashMap<>();

    protected final GuiTheme theme;
    private final Map<Class<?>, BiConsumer<WTable, Setting<?>>> factories = new HashMap<>();

    protected void map(Class<?> settingClass, BiConsumer<WTable, Setting<?>> factory) {
        if (factory != null)
            factories.put(settingClass, factory);
    }

    public SettingsWidgetFactory(GuiTheme theme) {
        this.theme = theme;
    }

    /** {@code SettingsWidgetFactory.registerCustomFactory(SomeSetting.class, (theme) -> (table, setting) -> {//create widget})} */
    public static void registerCustomFactory(Class<?> settingClass, Function<GuiTheme, BiConsumer<WTable, Setting<?>>> factoryFunction) {
        if (factoryFunction != null)
            customFactories.put(settingClass, factoryFunction);
    }

    public static void unregisterCustomFactory(Class<?> settingClass) {
        customFactories.remove(settingClass);
    }

    public abstract WWidget create(GuiTheme theme, Settings settings, String filter);

    @NotNull
    protected BiConsumer<WTable, Setting<?>> getFactory(Class<?> settingClass) {
        if (customFactories.containsKey(settingClass)) return customFactories.get(settingClass).apply(theme);
        if (factories.containsKey(settingClass))
            return factories.get(settingClass);
        else
            throw new IllegalArgumentException("Class %s does not have a registered factory function.".formatted(settingClass.getCanonicalName()));
    }
}
