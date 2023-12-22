/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.gui.screens.NotebotSongsScreen;
import meteordevelopment.meteorclient.gui.screens.ProxiesScreen;
import meteordevelopment.meteorclient.gui.screens.accounts.AccountsScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.*;
import meteordevelopment.meteorclient.gui.widgets.input.*;
import meteordevelopment.meteorclient.gui.widgets.pressable.*;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.utils.java;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.gui.widget.WGuiTexture;
import net.greemdev.meteor.gui.widget.WWaypointIcon;
import net.greemdev.meteor.type.ColorSettingScreenMode;
import net.greemdev.meteor.util.meteor.Meteor;
import net.greemdev.meteor.util.misc.Nbt;
import net.greemdev.meteor.utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

public abstract class GuiTheme implements ISerializable<GuiTheme> {

    public static double getWideSettingWidth() {
        return Utils.getWindowWidth() / getWidthDivisor();
    }

    public static double getWidthDivisor() {
        return Meteor.currentTheme().widthDivisor.get();
    }

    public static final double TITLE_TEXT_SCALE = 1.25;

    public final String name;
    public final Settings settings = new Settings();

    public boolean disableHoverColor;

    public Setting<ColorSettingScreenMode> colorScreenMode;
    public Setting<Double> widthDivisor;

    protected SettingsWidgetFactory settingsFactory;

    protected final Map<String, WindowConfig> windowConfigs = new HashMap<>();

    public GuiTheme(String name) {
        this.name = name;
    }

    public void beforeRender() {
        disableHoverColor = false;
    }

    // Widgets

    public abstract WWindow window(WWidget icon, String title);
    public WWindow window(String title) {
        return window(null, title);
    }

    public abstract WLabel label(String text, boolean title, double maxWidth);
    public WLabel label(String text, boolean title) {
        return label(text, title, 0);
    }
    public WLabel label(String text, double maxWidth) {
        return label(text, false, maxWidth);
    }
    public WLabel label(String text) {
        return label(text, false);
    }

    public WLabel label(String text, String onHover) {
        return net.greemdev.meteor.utils.apply(label(text, false), l -> l.tooltip = onHover);
    }

    public abstract WHorizontalSeparator horizontalSeparator(String text);
    public WHorizontalSeparator horizontalSeparator() {
        return horizontalSeparator(null);
    }
    public abstract WVerticalSeparator verticalSeparator(boolean unicolor);
    public WVerticalSeparator verticalSeparator() {
        return verticalSeparator(false);
    }

    protected abstract WButton button(String text, GuiTexture texture);
    public WButton button(String text) {
        return button(text, (GuiTexture)null);
    }
    public WButton button(String text, Runnable action) {
        return button(text).action(action);
    }
    public WButton button(GuiTexture texture) {
        return button(null, texture);
    }
    public WButton button(GuiTexture texture, Runnable action) {
        return button(texture).action(action);
    }

    public WButton resetButton(Runnable action) {
        return button(GuiRenderer.RESET).action(action);
    }

    public WButton editButton(Runnable action) {
        return button(GuiRenderer.EDIT).action(action);
    }

    public WButton cogButton(Runnable action) {
        return button(GuiRenderer.COG).action(action);
    }

    public abstract WMinus minus();
    public WMinus minus(Runnable action) {
        return minus().action(action);
    }
    public abstract WPlus plus();
    public WPlus plus(Runnable action) {
        return plus().action(action);
    }

    public abstract WCheckbox checkbox(boolean checked);

    public WCheckbox checkbox() {
        return checkbox(false);
    }

    public WCheckbox checkbox(Consumer<Boolean> action) {
        return checkbox().action(action);
    }

    public WCheckbox checkbox(boolean checked, Consumer<Boolean> action) {
        return checkbox(checked).action(action);
    }

    public abstract WSlider slider(double value, double min, double max);

    public abstract WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer);
    public WTextBox textBox(String text, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return textBox(text, null, filter, renderer);
    }
    public WTextBox textBox(String text, Class<? extends WTextBox.Renderer> renderer) {
        return textBox(text, null, renderer);
    }
    public WTextBox textBox(String text, String placeholder, CharFilter filter) {
        return textBox(text, placeholder, filter, null);
    }
    public WTextBox textBox(String text, CharFilter filter) {
        return textBox(text, filter, null);
    }
    public WTextBox textBox(String text, String placeholder) {
        return textBox(text, placeholder, (CharFilter)null);
    }
    public WTextBox textBox(String text) {
        return textBox(text, (CharFilter)null);
    }

    public WTextBox textBox(String text, Runnable action) {
        return textBox(text, (CharFilter)null).action(action);
    }

    public WTextBox textBox(String text, String placeholder, Runnable action) {
        return textBox(text, placeholder).action(action);
    }

    public abstract <T> WDropdown<T> dropdown(T[] values, T value);
    public <T extends Enum<?>> WDropdown<T> dropdown(T value) {
        Class<?> klass = value.getClass();
        T[] values = null;
        try {
            values = java.cast(klass.getDeclaredMethod("values").invoke(null));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            MeteorClient.LOG.error("Error getting enum values", e);
        }

        return dropdown(values, value);
    }

    public <T extends Enum<?>> WDropdown<T> dropdown(T value, Consumer<WDropdown<T>> action) {
        return dropdown(value).action(action);
    }

    public <T> WDropdown<T> dropdown(T[] values, T value, Consumer<WDropdown<T>> action) {
        return dropdown(values, value).action(action);
    }

    public abstract WTriangle triangle();

    public abstract WTooltip tooltip(String text);

    public abstract WView view();

    public WVerticalList verticalList() {
        return w(new WVerticalList());
    }
    public WVerticalList verticalList(Consumer<WVerticalList> listModifier) {
        return utils.apply(verticalList(), listModifier);
    }

    public WHorizontalList horizontalList() {
        return w(new WHorizontalList());
    }
    public WHorizontalList horizontalList(Consumer<WHorizontalList> listModifier) {
        return utils.apply(horizontalList(), listModifier);
    }

    public WTable table() {
        return w(new WTable());
    }

    public WTable table(Consumer<WTable> tableModifier) {
        return utils.apply(table(), tableModifier);
    }

    public WWaypointIcon waypointIcon(Waypoint waypoint) {
        return w(new WWaypointIcon(waypoint));
    }

    public abstract WSection section(String title, boolean expanded, WWidget headerWidget);
    public WSection section(String title, boolean expanded) {
        return section(title, expanded, null);
    }
    public WSection section(String title) {
        return section(title, true);
    }

    public abstract WAccount account(WidgetScreen screen, Account<?> account);

    public abstract WWidget module(Module module);

    public abstract WQuad quad(Color color);

    public abstract WTopBar topBar();

    public abstract WFavorite favorite(boolean checked);

    public WItem item(ItemStack itemStack) {
        return w(new WItem(itemStack));
    }
    public WItemWithLabel itemWithLabel(ItemStack stack, String name) {
        return w(new WItemWithLabel(stack, name));
    }
    public WItemWithLabel itemWithLabel(ItemStack stack) {
        return itemWithLabel(stack, Names.get(stack.getItem()));
    }

    public WGuiTexture guiTexture(GuiTexture texture, Color color) {
        return w(new WGuiTexture(texture, color));
    }
    public WTexture texture(double width, double height, double rotation, Texture texture) {
        return w(new WTexture(width, height, rotation, texture));
    }

    public WIntEdit intEdit(int value, int min, int max, int sliderMin, int sliderMax, boolean noSlider) {
        return w(new WIntEdit(value, min, max, sliderMin, sliderMax, noSlider));
    }
    public WIntEdit intEdit(int value, int min, int max, int sliderMin, int sliderMax) {
        return w(new WIntEdit(value, min, max, sliderMin, sliderMax, false));
    }
    public WIntEdit intEdit(int value, int min, int max, boolean noSlider) {
        return w(new WIntEdit(value, min, max, 0, 0, noSlider));
    }

    public WDoubleEdit doubleEdit(double value, double min, double max, double sliderMin, double sliderMax, int decimalPlaces, boolean noSlider) {
        return w(new WDoubleEdit(value, min, max, sliderMin, sliderMax, decimalPlaces, noSlider));
    }
    public WDoubleEdit doubleEdit(double value, double min, double max, double sliderMin, double sliderMax) {
        return w(new WDoubleEdit(value, min, max, sliderMin, sliderMax, 3, false));
    }
    public WDoubleEdit doubleEdit(double value, double min, double max) {
        return w(new WDoubleEdit(value, min, max, 0, 10, 3, false));
    }

    public WBlockPosEdit blockPosEdit(BlockPos value) {
        return w(new WBlockPosEdit(value));
    }

    public WKeybind keybind(Keybind keybind) {
        return keybind(keybind, Keybind.none());
    }

    public WKeybind keybind(Keybind keybind, Runnable onSet) {
        return keybind(keybind).onSet(onSet);
    }

    public WKeybind moduleKeybind(Keybind keybind, Runnable onSet) {
        var kb = keybind(keybind, onSet);
        kb.module = true;
        return kb;
    }

    public WKeybind keybind(Keybind keybind, Keybind defaultValue) {
        return keybind(keybind, defaultValue, false);
    }

    public WKeybind keybind(Keybind keybind, Keybind defaultValue, boolean withResetButton) {
        return w(new WKeybind(keybind, defaultValue, withResetButton));
    }

    public WWidget settings(Settings settings, String filter) {
        return settingsFactory.create(this, settings, filter);
    }
    public WWidget settings(Settings settings) {
        return settings(settings, "");
    }

    // Screens

    public TabScreen modulesScreen() {
        return new ModulesScreen(this);
    }
    public boolean isModulesScreen(Screen screen) {
        return screen instanceof ModulesScreen;
    }

    public WidgetScreen moduleScreen(Module module) {
        return new ModuleScreen(this, module);
    }

    public WidgetScreen accountsScreen() {
        return new AccountsScreen(this);
    }

    public NotebotSongsScreen notebotSongs() {
        return new NotebotSongsScreen(this);
    }

    public WidgetScreen proxiesScreen() {
        return new ProxiesScreen(this);
    }

    // Colors

    public abstract Color textColor();

    public abstract Color titleTextColor();

    public abstract Color textSecondaryColor();

    //     Starscript

    public abstract Color starscriptTextColor();

    public abstract Color starscriptBraceColor();

    public abstract Color starscriptParenthesisColor();

    public abstract Color starscriptDotColor();

    public abstract Color starscriptCommaColor();

    public abstract Color starscriptOperatorColor();

    public abstract Color starscriptStringColor();

    public abstract Color starscriptNumberColor();

    public abstract Color starscriptKeywordColor();

    public abstract Color starscriptAccessedObjectColor();

    // Other

    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    public double scale(double value) {
        double scaled = value * scalar();

        if (IS_SYSTEM_MAC) {
            scaled /= (double) mc.getWindow().getWidth() / mc.getWindow().getFramebufferWidth();
        }

        return scaled;
    }

    public abstract double scalar();

    public abstract boolean categoryIcons();

    public abstract boolean hideHUD();

    public double textWidth(String text, int length, boolean title) {
        return scale(textRenderer().getWidth(text, length, false) * (title ? TITLE_TEXT_SCALE : 1));
    }
    public double textWidth(String text) {
        return textWidth(text, text.length(), false);
    }

    public double textHeight(boolean title) {
        return scale(textRenderer().getHeight() * (title ? TITLE_TEXT_SCALE : 1));
    }
    public double textHeight() {
        return textHeight(false);
    }

    public double pad() {
        return scale(6);
    }

    public WindowConfig getWindowConfig(String id) {
        WindowConfig config = windowConfigs.get(id);
        if (config != null) return config;

        config = new WindowConfig();
        windowConfigs.put(id, config);
        return config;
    }

    public void clearWindowConfigs() {
        windowConfigs.clear();
    }

    protected <T extends WWidget> T w(T widget) {
        widget.theme = this;
        return widget;
    }

    // Saving / Loading

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        tag.put("settings", settings.toTag());

        tag.put("windowConfigs", Nbt.newCompound(configs ->
            windowConfigs.forEach((k, v) ->
                configs.put(k, v.toTag())
            )
        ));

        return tag;
    }

    @Override
    public GuiTheme fromTag(NbtCompound tag) {
        settings.fromTag(tag.getCompound("settings"));

        NbtCompound configs = tag.getCompound("windowConfigs");
        for (String id : configs.getKeys()) {
            windowConfigs.put(id, new WindowConfig().fromTag(configs.getCompound(id)));
        }

        return this;
    }
}
