/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ItemHud extends HudElement {
    public static final HudElementInfo<ItemHud> INFO = new HudElementInfo<>(Hud.GROUP, "item", "Displays the item count.", ItemHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General

    private final Setting<Boolean> currentItem = sgGeneral.add(new BoolSetting.Builder()
        .name("show-current-item")
        .description("Show a dynamic display of how many items you have for the type in your hand.")
        .defaultValue(true)
        .build()
    );

    private final Setting<PlayerHand> inHand = sgGeneral.add(new EnumSetting.Builder<PlayerHand>()
        .name("target-hand")
        .description("Which hand to display the item stack from.")
        .defaultValue(PlayerHand.Main)
        .visible(currentItem)
        .build()
    );

    private final Setting<Boolean> ignoreHeldTools = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-tools")
        .description("Ignore tools in the dynamic display.")
        .defaultValue(true)
        .visible(currentItem)
        .build()
    );

    private final Setting<Item> item = sgGeneral.add(new ItemSetting.Builder()
        .name("item")
        .description("Item to display")
        .defaultValue(Items.TOTEM_OF_UNDYING)
        .build()
    );

    private final Setting<NoneMode> noneMode = sgGeneral.add(new EnumSetting.Builder<NoneMode>()
        .name("none-mode")
        .description("How to render the item when you don't have the specified item in your inventory.")
        .defaultValue(NoneMode.HideCount)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the item.")
        .defaultValue(2)
        .onChanged(aDouble -> calculateSize())
        .min(1)
        .sliderRange(1, 4)
        .build()
    );

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the element.")
        .defaultValue(0)
        .onChanged(integer -> calculateSize())
        .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private ItemHud() {
        super(INFO);

        calculateSize();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    private void calculateSize() {
        setSize(17 * scale.get(), 17 * scale.get());
    }

    private ItemStack displayStack() {
        if (mc.player == null)
            return ItemStack.EMPTY;

        ItemStack itemStack;
        if (currentItem.get()) {
            itemStack = mc.player.getStackInHand(inHand.get().mc).copy();
            if (itemStack.getMaxDamage() > 0 && ignoreHeldTools.get())
                return ItemStack.EMPTY;

            itemStack.setCount(InvUtils.find(itemStack.getItem()).count());
        } else
            itemStack = new ItemStack(item.get(), InvUtils.find(item.get()).count());

        return itemStack;
    }

    @Override
    public void render(HudRenderer renderer) {
        ItemStack itemStack = new ItemStack(item.get(), InvUtils.find(item.get()).count());

        if (noneMode.get() == NoneMode.HideItem && itemStack.isEmpty())
            if (isInEditor()) {
                renderer.line(x, y, x + getWidth(), y + getHeight(), Color.GRAY);
                renderer.line(x, y + getHeight(), x + getWidth(), y, Color.GRAY);
            } else renderer.post(() -> {
                MatrixStack matrices = RenderSystem.getModelViewStack();
                matrices.push();
                matrices.scale(scale.get().floatValue(), scale.get().floatValue(), 1);

                double x = this.x + border.get();
                double y = this.y + border.get();

                render(renderer, displayStack(), (int) (x / scale.get()), (int) (y / scale.get()));

                matrices.pop();
            });

        if (background.get()) renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
    }

    private void render(HudRenderer renderer, ItemStack itemStack, int x, int y) {
        if (noneMode.get() == NoneMode.HideItem) {
            renderer.item(itemStack, x, y, scale.get().floatValue(), true);
            return;
        }

        String countOverride = null;
        boolean resetToZero = false;

        if (itemStack.isEmpty()) {
            if (noneMode.get() == NoneMode.ShowCount)
                countOverride = "0";

            itemStack.setCount(1);
            resetToZero = true;
        }

        renderer.item(itemStack, x, y, scale.get().floatValue(), true, countOverride);

        if (resetToZero)
            itemStack.setCount(0);
    }

    public enum PlayerHand {
        Main(Hand.MAIN_HAND),
        Off(Hand.OFF_HAND);

        public final Hand mc;

        PlayerHand(Hand mc) {
            this.mc = mc;
        }

        @Override
        public String toString() {
            return switch (this) {
                case Main -> "Main Hand";
                case Off -> "Offhand";
            };
        }
    }

    public enum NoneMode {
        HideItem,
        HideCount,
        ShowCount;

        @Override
        public String toString() {
            return switch (this) {
                case HideItem -> "Hide Item";
                case HideCount -> "Hide Count";
                case ShowCount -> "Show Count";
            };
        }
    }
}
