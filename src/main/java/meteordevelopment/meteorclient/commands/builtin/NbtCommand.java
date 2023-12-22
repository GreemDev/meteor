/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.builtin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.commands.arguments.CompoundNbtTagArgumentType;
import net.greemdev.meteor.util.misc.KMC;
import net.greemdev.meteor.util.misc.NbtUtil;
import net.greemdev.meteor.util.text.ChatColor;
import net.greemdev.meteor.util.text.FormattedText;
import net.greemdev.meteor.util.text.actions;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class NbtCommand extends Command {
    public NbtCommand() {
        super("nbt", "Modifies NBT data for an item, example: .nbt add {display:{Name:'{\"text\":\"$cRed Name\"}'}}");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientCommandSource> builder) {
        builder.then(literal("add").then(argument("nbt", CompoundNbtTagArgumentType.create()).executes(s -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (validBasic(stack)) {
                NbtCompound tag = CompoundNbtTagArgumentType.get(s);
                NbtCompound source = stack.getOrCreateNbt();

                if (tag != null) {
                    source.copyFrom(tag);
                    setStack(stack);
                } else {
                    error("Some of the NBT data could not be found, try using: " + Commands.prefix() + "nbt set {nbt}");
                }
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("set").then(argument("nbt", CompoundNbtTagArgumentType.create()).executes(context -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (validBasic(stack)) {
                stack.setNbt(CompoundNbtTagArgumentType.get(context));
                setStack(stack);
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(argument("nbt_path", NbtPathArgumentType.nbtPath()).executes(context -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (validBasic(stack)) {
                NbtPathArgumentType.NbtPath path = context.getArgument("nbt_path", NbtPathArgumentType.NbtPath.class);
                path.remove(stack.getNbt());
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("get").executes(context -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (stack == null) {
                error("You must hold an item in your main hand.");
            } else {
                NbtCompound tag = stack.getNbt();

                info(new FormattedText(b -> {
                    b.colored(ChatColor.grey);
                    b.addString("NBT", nbt ->
                        nbt.underlined()
                            .clicked(actions.runCommand, subcommand("copy"))
                            .hoveredText(Text.literal("Copy the NBT data to your clipboard."))
                    );
                    if (tag != null)
                        b.addString(" ").addText(NbtUtil.asPrettyText(tag));
                    else
                        b.addString("{}");
                }));
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("copy").executes(context -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (stack == null) {
                error("You must hold an item in your main hand.");
            } else {
                NbtCompound tag = stack.getOrCreateNbt();
                mc.keyboard.setClipboard(tag.toString());

                info(new FormattedText(b ->
                    b.colored(ChatColor.grey)
                        .addString("NBT", nbt -> nbt.underlined().hoveredText(NbtUtil.asPrettyText(tag)))
                        .addString(" data copied!")
                ));
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("paste").executes(context -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (validBasic(stack)) {
                stack.setNbt(new CompoundNbtTagArgumentType().parse(new StringReader(mc.keyboard.getClipboard())));
                setStack(stack);
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("count").then(argument("count", IntegerArgumentType.integer(-127, 127)).executes(context -> {
            ItemStack stack = mc.player.getInventory().getMainHandStack();

            if (validBasic(stack)) {
                int count = IntegerArgumentType.getInteger(context, "count");
                stack.setCount(count);
                setStack(stack);
                info("Set mainhand stack count to %s.",count);
            }

            return SINGLE_SUCCESS;
        })));
    }

    private void setStack(ItemStack stack) {
        KMC.network(mc).sendPacket(new CreativeInventoryActionC2SPacket(36 + mc.player.getInventory().selectedSlot, stack));
    }

    private boolean validBasic(ItemStack stack) {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            return false;
        }

        if (stack == null) {
            error("You must hold an item in your main hand.");
            return false;
        }
        return true;
    }
}
