/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.builtin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.greemdev.meteor.commands.api.Arguments;
import net.greemdev.meteor.util.misc.KMC;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.function.Function;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EnchantCommand extends Command {
    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode to use this."));
    private final static SimpleCommandExceptionType NOT_HOLDING_ITEM = new SimpleCommandExceptionType(Text.literal("You need to hold some item to enchant."));

    public EnchantCommand() {
        super("enchant", "Enchants the item in your hand. REQUIRES Creative mode.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientCommandSource> builder) {
        builder.then(literal("one").then(argument("enchantment", Arguments.registryEntry(RegistryKeys.ENCHANTMENT))
            .then(literal("level").then(argument("level", IntegerArgumentType.integer()).executes(context -> {
                one(context, enchantment -> context.getArgument("level", Integer.class));
                return SINGLE_SUCCESS;
            })))
            .then(literal("max").executes(context -> {
                one(context, Enchantment::getMaxLevel);
                return SINGLE_SUCCESS;
            }))
        ));

        builder.then(literal("all_possible")
            .then(literal("level").then(argument("level", IntegerArgumentType.integer()).executes(context -> {
                all(true, enchantment -> context.getArgument("level", Integer.class));
                return SINGLE_SUCCESS;
            })))
            .then(literal("max").executes(context -> {
                all(true, Enchantment::getMaxLevel);
                return SINGLE_SUCCESS;
            }))
        );

        builder.then(literal("all")
            .then(literal("level").then(argument("level", IntegerArgumentType.integer()).executes(context -> {
                all(false, enchantment -> context.getArgument("level", Integer.class));
                return SINGLE_SUCCESS;
            })))
            .then(literal("max").executes(context -> {
                all(false, Enchantment::getMaxLevel);
                return SINGLE_SUCCESS;
            }))
        );

        builder.then(literal("clear").executes(context -> {
            ItemStack itemStack = tryGetItemStack();
            Utils.clearEnchantments(itemStack);

            syncItem();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(argument("enchantment", Arguments.registryEntry(RegistryKeys.ENCHANTMENT)).executes(context -> {
            ItemStack itemStack = tryGetItemStack();
            RegistryEntry.Reference<Enchantment> enchantment = context.getArgument("enchantment", RegistryEntry.Reference.class);
            Utils.removeEnchantment(itemStack, enchantment.value());

            syncItem();
            return SINGLE_SUCCESS;
        })));
    }

    private void one(CommandContext<ClientCommandSource> context, Function<Enchantment, Integer> level) throws CommandSyntaxException {
        ItemStack itemStack = tryGetItemStack();

        RegistryEntry.Reference<Enchantment> enchantment = context.getArgument("enchantment", RegistryEntry.Reference.class);
        Utils.addEnchantment(itemStack, enchantment.value(), level.apply(enchantment.value()));

        syncItem();
    }

    private void all(boolean onlyPossible, Function<Enchantment, Integer> level) throws CommandSyntaxException {
        ItemStack itemStack = tryGetItemStack();

        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            if (!onlyPossible || enchantment.isAcceptableItem(itemStack)) {
                Utils.addEnchantment(itemStack, enchantment, level.apply(enchantment));
            }
        }

        syncItem();
    }

    private void syncItem() {
        mc.setScreen(new InventoryScreen(KMC.player(mc)));
        mc.setScreen(null);
    }

    private ItemStack tryGetItemStack() throws CommandSyntaxException {
        require(mc.player.isCreative(), NOT_IN_CREATIVE);

        return require(getItemStack(), NOT_HOLDING_ITEM);
    }

    private ItemStack getItemStack() {
        ItemStack itemStack = mc.player.getMainHandStack();
        if (itemStack == null) itemStack = mc.player.getOffHandStack();
        return itemStack.isEmpty() ? null : itemStack;
    }
}
