/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SelfTrap extends Module {


    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("Which blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );

    private final Setting<TopMode> topPlacement = sgGeneral.add(new EnumSetting.Builder<TopMode>()
            .name("top")
            .description("Which positions to place on your top half.")
            .defaultValue(TopMode.TopOnly)
            .build()
    );

    private final Setting<Boolean> bottomPlacement = sgGeneral.add(new BoolSetting.Builder()
            .name("bottom")
            .description("Whether to cover your feet level.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> floorPlacement = sgGeneral.add(new BoolSetting.Builder()
            .name("floor-mode")
            .description("Whether to try and place a block under your feet.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("place-delay")
            .description("How many ticks between block placements.")
            .defaultValue(1)
            .build()
    );

    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
            .name("center")
            .description("Centers you on the block you are standing on before placing.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
            .name("turn-off")
            .description("Turns off after placing.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Sends rotation packets to the server when placing.")
            .defaultValue(true)
            .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .description("Renders a block overlay where the blocks will be placed.")
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The color of the sides of the blocks being rendered.")
            .defaultValue(new SettingColor(204, 0, 0, 10))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The color of the lines of the blocks being rendered.")
            .defaultValue(new SettingColor(204, 0, 0, 255))
            .build()
    );

    private final List<BlockPos> placePositions = new ArrayList<>();
    private boolean placed;
    private int d; //delay counter

    public SelfTrap(){
        super(Categories.Combat, "self-trap", "Places blocks above your head.");
    }

    @Override
    public void onActivate() {
        if (!placePositions.isEmpty()) placePositions.clear();
        d = 0;
        placed = false;

        if (center.get()) PlayerUtils.centerPlayer();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (turnOff.get() && placed && placePositions.isEmpty()) {
            toggle();
            return;
        }

        for (Block b : blocks.get()) {
            var itemResult = InvUtils.findInHotbar(b.asItem());

            if (!itemResult.found()) {
                placePositions.clear();
                continue;
            }

            findPlacePos(b);

            if (d >= delay.get() && placePositions.size() > 0) {
                BlockPos blockPos = placePositions.get(placePositions.size() - 1);

                if (BlockUtils.place(blockPos, itemResult, rotate.get(), 50)) {
                    placePositions.remove(blockPos);
                    placed = true;
                }

                d = 0;
            }
            else d++;
            return;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || placePositions.isEmpty()) return;
        for (BlockPos pos : placePositions) event.renderer.box(pos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    private void findPlacePos(Block block) {
        placePositions.clear();
        BlockPos pos = mc.player.getBlockPos();

        switch (topPlacement.get()) {
            case Both:
                add(pos.add(0, 2, 0), block);
                add(pos.add(1, 1, 0), block);
                add(pos.add(-1, 1, 0), block);
                add(pos.add(0, 1, 1), block);
                add(pos.add(0, 1, -1), block);
                break;
            case AboveOnly:
                add(pos.add(0, 2, 0), block);
                break;
            case TopOnly:
                add(pos.add(1, 1, 0), block);
                add(pos.add(-1, 1, 0), block);
                add(pos.add(0, 1, 1), block);
                add(pos.add(0, 1, -1), block);
        }

        if (floorPlacement.get())
            add(pos.add(0, -1, 0), block);

        if (bottomPlacement.get()) {
            add(pos.add(1, 0, 0), block);
            add(pos.add(-1, 0, 0), block);
            add(pos.add(0, 0, 1), block);
            add(pos.add(0, 0, -1), block);
        }
    }


    private void add(BlockPos blockPos, Block block) {
        if (!placePositions.contains(blockPos) && mc.world.getBlockState(blockPos).getMaterial().isReplaceable() && mc.world.canPlace(block.getDefaultState(), blockPos, ShapeContext.absent())) placePositions.add(blockPos);
    }

    public enum TopMode {
        TopOnly,
        Both,
        AboveOnly,
        Off;

        @Override
        public String toString() {
            return switch (this) {
                case TopOnly -> "Top Only";
                case Both -> "Top and Over-head";
                case AboveOnly -> "Over-head Only";
                case Off -> "Off";
            };
        }
    }
}
