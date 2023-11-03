/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import net.greemdev.meteor.util.misc.Nbt;
import net.greemdev.meteor.util.misc.NbtDataType;
import net.greemdev.meteor.util.misc.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringListSetting extends Setting<List<String>> {
    public static Builder builder() {
        return new Builder();
    }

    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;
    public final boolean wide;

    protected StringListSetting(String name, String description, Object defaultValue, Consumer<List<String>> onChanged, Consumer<Setting<List<String>>> onModuleActivated, Supplier<Boolean> visible, Class<? extends WTextBox.Renderer> renderer, boolean wide, CharFilter filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.renderer = renderer;
        this.filter = filter;
        this.wide = wide;
    }

    @Override
    protected List<String> parseImpl(String str) {
        return Arrays.asList(str.split(","));
    }

    @Override
    protected boolean isValueValid(List<String> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (int i = 0; i < this.value.size(); i++) {
            valueTag.add(i, NbtString.of(get().get(i)));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public List<String> load(NbtCompound tag) {
        get().clear();

        NbtList valueTag = tag.getList("value", NbtElement.STRING_TYPE);
        for (NbtElement tagI : valueTag) {
            get().add(tagI.asString());
        }

        return get();
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(getDefaultValue());
    }

    public static void fillTable(GuiTheme theme, WTable table, StringListSetting setting) {
        table.clear();

        ArrayList<String> strings = new ArrayList<>(setting.get());
        CharFilter filter = setting.filter == null ? (text, c) -> true : setting.filter;

        for (int i = 0; i < setting.get().size(); i++) {
            int msgI = i;
            String message = setting.get().get(i);

            WTextBox textBox = table.add(theme.textBox(message, filter, setting.renderer)).expandX().widget();
            textBox.action = () -> strings.set(msgI, textBox.get());
            textBox.actionOnUnfocused = () -> setting.set(strings);

            table.add(theme.minus(() -> {
                strings.remove(msgI);
                setting.set(strings);

                fillTable(theme, table, setting);
            }));

            table.row();
        }

        if (!setting.get().isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }

        table.add(theme.button("Add", () -> {
            strings.add("");
            setting.set(strings);

            fillTable(theme, table, setting);
        })).expandX();

        table.add(theme.resetButton(() -> {
            setting.reset();

            fillTable(theme, table, setting);
        }));
    }

    public static class Builder extends SettingBuilder<Builder, List<String>, StringListSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private CharFilter filter;

        private boolean wide = false;

        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder wide() {
            wide = !wide;
            return this;
        }

        public Builder defaultValue(String... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        public Builder renderStarscript() {
            return renderer(StarscriptTextBoxRenderer.class);
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder filter(CharFilter filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public StringListSetting build() {
            return new StringListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, renderer, wide, filter);
        }
    }
}
