/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.*;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorLabel;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.*;
import meteordevelopment.meteorclient.gui.widgets.input.*;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class DefaultSettingsWidgetFactory extends SettingsWidgetFactory {
    private static final SettingColor WHITE = new SettingColor();

    public DefaultSettingsWidgetFactory(GuiTheme theme) {
        super(theme);

        map(BoolSetting.class, this::boolW);
        map(IntSetting.class,  this::intW);
        map(DoubleSetting.class, this::doubleW);
        map(EnumSetting.class, this::enumW);
        map(StringSetting.class, this::stringW);
        map(ProvidedStringSetting.class, this::providedStringW);
        map(GenericSetting.class, this::genericW);
        map(ColorSetting.class, this::colorW);
        map(KeybindSetting.class, this::keybindW);
        map(BlockSetting.class, this::blockW);
        map(BlockListSetting.class, this::blockListW);
        map(ItemSetting.class, this::itemW);
        map(ItemListSetting.class, this::itemListW);
        map(EntityTypeListSetting.class, this::entityTypeListW);
        map(EnchantmentListSetting.class, this::enchantmentListW);
        map(ModuleListSetting.class, this::moduleListW);
        map(PacketListSetting.class, this::packetListW);
        map(ParticleTypeListSetting.class, this::particleTypeListW);
        map(SoundEventListSetting.class, this::soundEventListW);
        map(StatusEffectAmplifierMapSetting.class, this::statusEffectAmplifierMapW);
        map(StatusEffectListSetting.class, this::statusEffectListW);
        map(StorageBlockListSetting.class, this::storageBlockListW);
        map(ScreenHandlerListSetting.class, this::screenHandlerListW);
        map(BlockDataSetting.class, this::blockDataW);
        map(PotionSetting.class, this::potionW);
        map(StringListSetting.class, this::stringListW);
        map(StringMapSetting.class, this::stringMapW);
        map(BlockPosSetting.class, this::blockPosW);
        map(ColorListSetting.class, this::colorListW);
        map(FontFaceSetting.class, this::fontW);
        map(Vector3dSetting.class, this::vector3dW);
    }

    @Override
    public WWidget create(GuiTheme theme, Settings settings, String filter) {
        WVerticalList list = theme.verticalList();

        List<RemoveInfo> removeInfoList = new ArrayList<>();

        // Add all settings
        for (SettingGroup group : settings.groups) {
            group(list, group, filter, removeInfoList);
        }

        // Calculate width and set it as minimum width
        list.calculateSize();
        list.minWidth = list.width;

        // Remove hidden settings
        for (RemoveInfo removeInfo : removeInfoList) {
            removeInfo.remove(list);
        }

        return list;
    }

    // If a different theme uses has different heights of widgets this can method can be overwritten to account for it in the setting titles
    protected double settingTitleTopMargin() {
        return 6;
    }

    private void group(WVerticalList list, SettingGroup group, String filter, List<RemoveInfo> removeInfoList) {
        WSection section = list.add(theme.section(group.name, group.sectionExpanded)).expandX().widget();
        section.action = () -> group.sectionExpanded = section.isExpanded();

        WTable table = section.add(theme.table()).expandX().widget();

        RemoveInfo removeInfo = null;

        for (Setting<?> setting : group) {
            if (!StringUtils.containsIgnoreCase(setting.title, filter)) continue;

            boolean visible = setting.isVisible();
            setting.lastWasVisible = visible;
            if (!visible) {
                if (removeInfo == null) removeInfo = new RemoveInfo(section, table);
                removeInfo.markRowForRemoval();
            }

            table.add(theme.label(setting.title)).top().marginTop(settingTitleTopMargin()).widget().tooltip = setting.description;

            var factory = getFactory(setting.getClass());
            if (factory != null) factory.accept(table, setting);

            table.row();
        }

        if (removeInfo != null) removeInfoList.add(removeInfo);
    }

    private static class RemoveInfo {
        private final WSection section;
        private final WTable table;
        private final IntList rowIds = new IntArrayList();

        public RemoveInfo(WSection section, WTable table) {
            this.section = section;
            this.table = table;
        }

        public void markRowForRemoval() {
            rowIds.add(table.rowI());
        }

        public void remove(WVerticalList list) {
            for (int i = 0; i < rowIds.size(); i++) {
                table.removeRow(rowIds.getInt(i) - i);
            }

            if (table.cells.isEmpty()) list.cells.removeIf(cell -> cell.widget() == section);
        }
    }

    // Settings

    private void boolW(WTable table, BoolSetting setting) {
        WCheckbox checkbox = table.add(theme.checkbox(setting.get())).expandCellX().widget();
        checkbox.action = () -> setting.set(checkbox.checked);

        reset(table, setting, () -> checkbox.checked = setting.get());
    }

    private void intW(WTable table, IntSetting setting) {
        WIntEdit edit = table.add(theme.intEdit(setting.get(), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.noSlider)).expandX().widget();

        edit.action = () -> {
            if (!setting.set(edit.get())) edit.set(setting.get());
        };

        reset(table, setting, () -> edit.set(setting.get()));
    }

    private void doubleW(WTable table, DoubleSetting setting) {
        WDoubleEdit edit = theme.doubleEdit(setting.get(), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.decimalPlaces, setting.noSlider);
        table.add(edit).expandX();

        Runnable action = () -> {
            if (!setting.set(edit.get())) edit.set(setting.get());
        };

        if (setting.onSliderRelease) edit.actionOnRelease = action;
        else edit.action = action;

        reset(table, setting, () -> edit.set(setting.get()));
    }

    private void stringW(WTable table, StringSetting setting) {
        Cell<WTextBox> cell = table.add(theme.textBox(setting.get(), CharFilter.orNone(setting.filter), setting.renderer));
        if (setting.wide)
            cell.minWidth(GuiTheme.getWideSettingWidth());

        WTextBox textBox = cell.expandX().widget();
        textBox.action = () -> setting.set(textBox.get());

        reset(table, setting, () -> textBox.set(setting.get()));
    }

    private void stringListW(WTable table, StringListSetting setting) {
        Cell<WTable> cell = table.add(theme.table()).expandX();
        if (setting.wide)
            cell.minWidth(GuiTheme.getWideSettingWidth());

        StringListSetting.fillTable(theme, cell.widget(), setting);
    }


    private void stringMapW(WTable table, StringMapSetting setting) {
        Cell<WTable> cell = table.add(theme.table()).expandX();
        if (setting.wide)
            cell.minWidth(GuiTheme.getWideSettingWidth());

        StringMapSetting.fillTable(theme, cell.widget(), setting);
    }

    private <T extends Enum<?>> void enumW(WTable table, EnumSetting<T> setting) {
        WDropdown<T> dropdown = table.add(
            theme.dropdown(setting.get(), dd -> setting.set(dd.get()))
        ).expandCellX().widget();

        reset(table, setting, () -> dropdown.set(setting.get()));
    }

    private void providedStringW(WTable table, ProvidedStringSetting setting) {
        WDropdown<String> dropdown = table.add(
            theme.dropdown(setting.supplier.get(), setting.get(), dd -> setting.set(dd.get()))
        ).expandCellX().widget();

        reset(table, setting, () -> dropdown.set(setting.get()));
    }

    private void genericW(WTable table, GenericSetting<?> setting) {
        table.add(theme.editButton(() -> mc.setScreen(setting.get().createScreen(theme))));

        reset(table, setting, null);
    }

    private void colorW(WTable table, ColorSetting setting) {
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();

        WQuad quad = list.add(theme.quad(setting.get())).widget();

        list.add(theme.editButton(() -> mc.setScreen(new ColorSettingScreen(theme, setting))));

        reset(table, setting, () -> quad.color = setting.get());
    }

    private void keybindW(WTable table, KeybindSetting setting) {
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();

        WKeybind keybind = list.add(theme.keybind(setting.get(), setting.getDefaultValue())).expandX().widget();
        keybind.action = setting::onChanged;
        setting.widget = keybind;

        list.add(theme.resetButton(keybind::resetBind)).expandCellX().right();
    }

    private void blockW(WTable table, BlockSetting setting) {
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();

        WItem item = list.add(theme.item(setting.get().asItem().getDefaultStack())).widget();

        WButton select = list.add(theme.button("Select")).widget();
        select.action = () -> {
            BlockSettingScreen screen = new BlockSettingScreen(theme, setting);
            screen.onClosed(() -> item.set(setting.get().asItem().getDefaultStack()));

            mc.setScreen(screen);
        };

        reset(table, setting, () -> item.set(setting.get().asItem().getDefaultStack()));
    }

    private void blockPosW(WTable table, BlockPosSetting setting) {
        WBlockPosEdit edit = table.add(theme.blockPosEdit(setting.get())).expandX().widget();

        edit.actionOnRelease = () -> {
            if (!setting.set(edit.get())) edit.set(setting.get());
        };

        reset(table, setting, () -> edit.set(setting.get()));
    }

    private void blockListW(WTable table, BlockListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new BlockListSettingScreen(theme, setting)));
    }

    private void itemW(WTable table, ItemSetting setting) {
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();

        WItem item = list.add(theme.item(setting.get().asItem().getDefaultStack())).widget();

        WButton select = list.add(theme.button("Select")).widget();
        select.action = () -> {
            ItemSettingScreen screen = new ItemSettingScreen(theme, setting);
            screen.onClosed(() -> item.set(setting.get().getDefaultStack()));

            mc.setScreen(screen);
        };

        reset(table, setting, () -> item.set(setting.get().getDefaultStack()));
    }

    private void itemListW(WTable table, ItemListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new ItemListSettingScreen(theme, setting)));
    }

    private void entityTypeListW(WTable table, EntityTypeListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new EntityTypeListSettingScreen(theme, setting)));
    }

    private void enchantmentListW(WTable table, EnchantmentListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new EnchantmentListSettingScreen(theme, setting)));
    }

    private void moduleListW(WTable table, ModuleListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new ModuleListSettingScreen(theme, setting)));
    }

    private void packetListW(WTable table, PacketListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new PacketBoolSettingScreen(theme, setting)));
    }

    private void particleTypeListW(WTable table, ParticleTypeListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new ParticleTypeListSettingScreen(theme, setting)));
    }

    private void soundEventListW(WTable table, SoundEventListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new SoundEventListSettingScreen(theme, setting)));
    }

    private void statusEffectAmplifierMapW(WTable table, StatusEffectAmplifierMapSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new StatusEffectAmplifierMapSettingScreen(theme, setting)));
    }

    private void statusEffectListW(WTable table, StatusEffectListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new StatusEffectListSettingScreen(theme, setting)));
    }

    private void storageBlockListW(WTable table, StorageBlockListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new StorageBlockListSettingScreen(theme, setting)));
    }

    private void screenHandlerListW(WTable table, ScreenHandlerListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new ScreenHandlerSettingScreen(theme, setting)));
    }

    private void blockDataW(WTable table, BlockDataSetting<?> setting) {
        table.add(theme.editButton(() -> mc.setScreen(new BlockDataSettingScreen(theme, setting)))).expandCellX();

        reset(table, setting, null);
    }

    private void potionW(WTable table, PotionSetting setting) {
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();
        WItemWithLabel item = list.add(theme.itemWithLabel(setting.get().potion, setting.get().potion.getName().getString())).widget();

        WButton button = list.add(theme.button("Select")).expandCellX().widget();
        button.action = () -> {
            WidgetScreen screen = new PotionSettingScreen(theme, setting);
            screen.onClosed(() -> item.set(setting.get().potion));

            mc.setScreen(screen);
        };

        reset(list, setting, () -> item.set(setting.get().potion));
    }

    private void fontW(WTable table, FontFaceSetting setting) {
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();
        WLabel label = list.add(theme.label(setting.get().info.family())).widget();

        WButton button = list.add(theme.button("Select")).expandCellX().widget();
        button.action = () -> {
            WidgetScreen screen = new FontFaceSettingScreen(theme, setting);
            screen.onClosed(() -> label.set(setting.get().info.family()));

            mc.setScreen(screen);
        };

        reset(list, setting, () -> label.set(Fonts.DEFAULT_FONT.info.family()));
    }

    private void colorListW(WTable table, ColorListSetting setting) {
        WTable tab = table.add(theme.table()).expandX().widget();
        WTable t = tab.add(theme.table()).expandX().widget();
        tab.row();

        colorListWFill(t, setting);

        WPlus add = tab.add(theme.plus()).expandCellX().widget();
        add.action = () -> {
            setting.get().add(new SettingColor());
            setting.onChanged();

            t.clear();
            colorListWFill(t, setting);
        };

        reset(tab, setting, () -> {
            t.clear();
            colorListWFill(t, setting);
        });
    }

    private void colorListWFill(WTable t, ColorListSetting setting) {
        final AtomicInteger i = new AtomicInteger(0);
        for (SettingColor color : setting.get()) {
            t.add(theme.label(i + ":"));

            t.add(theme.quad(color)).widget();

            t.add(theme.editButton(() -> {
                SettingColor defaultValue = WHITE;
                if (i.get() < setting.getDefaultValue().size()) defaultValue = setting.getDefaultValue().get(i.get());

                ColorSetting colorSetting = ColorSetting.builder()
                    .name(setting.name)
                    .description(setting.description)
                    .defaultValue(defaultValue)
                    .onChanged(clr -> {
                        setting.get().get(i.get()).set(clr);
                        setting.onChanged();
                    })
                    .build();

                colorSetting.set(setting.get().get(i.get()));
                mc.setScreen(new ColorSettingScreen(theme, colorSetting));
            }));

            int _i = i.getAndIncrement();

            t.add(theme.minus(() -> {
                setting.get().remove(_i);
                setting.onChanged();

                t.clear();
                colorListWFill(t, setting);
            })).expandCellX().right();

            t.row();
        }
    }

    private void vector3dW(WTable table, Vector3dSetting setting) {
        WTable internal = table.add(theme.table()).expandX().widget();

        WDoubleEdit x = addVectorComponent(internal, "X", setting.get().x, val -> setting.get().x = val, setting);
        WDoubleEdit y = addVectorComponent(internal, "Y", setting.get().y, val -> setting.get().y = val, setting);
        WDoubleEdit z = addVectorComponent(internal, "Z", setting.get().z, val -> setting.get().z = val, setting);

        reset(table, setting, () -> {
            x.set(setting.get().x);
            y.set(setting.get().y);
            z.set(setting.get().z);
        });
    }

    private WDoubleEdit addVectorComponent(WTable table, String label, double value, Consumer<Double> update, Vector3dSetting setting) {
        table.add(theme.label(label + ": "));

        WDoubleEdit component = table.add(theme.doubleEdit(value, setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.decimalPlaces, setting.noSlider)).expandX().widget();
        if (setting.onSliderRelease) {
            component.actionOnRelease = () -> update.accept(component.get());
        } else {
            component.action = () -> update.accept(component.get());
        }

        table.row();

        return component;
    }

    // Other

    private void selectW(WContainer c, Setting<?> setting, Runnable action) {
        boolean addCount = WSelectedCountLabel.getSize(setting) != -1;

        WContainer c2 = c;
        if (addCount) {
            c2 = c.add(theme.horizontalList()).expandCellX().widget();
            ((WHorizontalList) c2).spacing *= 2;
        }

        c2.add(theme.button("Select", action)).expandCellX().widget();

        if (addCount) c2.add(new WSelectedCountLabel(setting).color(theme.textSecondaryColor()));

        reset(c, setting, null);
    }

    private void reset(WContainer c, Setting<?> setting, Runnable action) {
        c.add(theme.resetButton(() -> {
            setting.reset();
            if (action != null) action.run();
        }));
    }

    private static class WSelectedCountLabel extends WMeteorLabel {
        private final Setting<?> setting;
        private int lastSize = -1;

        public WSelectedCountLabel(Setting<?> setting) {
            super("", false);

            this.setting = setting;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            int size = getSize(setting);

            if (size != lastSize) {
                set("(" + size + " selected)");
                lastSize = size;
            }

            super.onRender(renderer, mouseX, mouseY, delta);
        }

        public static int getSize(Setting<?> setting) {
            if (setting.get() instanceof Collection<?> collection) return collection.size();
            if (setting.get() instanceof Map<?, ?> map) return map.size();

            return -1;
        }
    }
}
