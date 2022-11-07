/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.commands.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.arguments.FakePlayerArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FakePlayerCommand extends Command {
    public FakePlayerCommand() {
        super("fake-player", "Manages fake players that you can use for testing.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                .executes(context -> {
                    FakePlayer fakePlayer = Modules.get().get(FakePlayer.class);
                    if (active())
                        FakePlayerManager.add(fakePlayer.name.get(), fakePlayer.health.get(), fakePlayer.copyInv.get());
                    return SINGLE_SUCCESS;
                })
                .then(argument("name", StringArgumentType.word())
                    .executes(context -> {
                        FakePlayer fakePlayer = Modules.get().get(FakePlayer.class);
                        if (active())
                            FakePlayerManager.add(StringArgumentType.getString(context, "name"), fakePlayer.health.get(), fakePlayer.copyInv.get());
                        return SINGLE_SUCCESS;
                    }))
            ).then(literal("remove").then(argument("fp", FakePlayerArgumentType.create()))
                .executes(context -> {
                    var fp = FakePlayerArgumentType.get(context);
                    if (fp == null || !FakePlayerManager.contains(fp)) {
                        error("Couldn't find a fake player with that name.");
                    } else {
                        FakePlayerManager.remove(fp);
                        info("Removed fake player %s.".formatted(fp.getEntityName()));
                    }
                    return SINGLE_SUCCESS;
                })
            ).then(literal("list")
                .executes(context -> {
                    info("--- Fake Players ((highlight)%s(default)) ---", FakePlayerManager.count());
                    FakePlayerManager.forEach(fp -> info("(highlight)%s".formatted(fp.getEntityName())));
                    return SINGLE_SUCCESS;
                })
            ).then(literal("clear").executes(context -> {
                if (active()) FakePlayerManager.clear();
                return SINGLE_SUCCESS;
            }));
    }

    private boolean active() {
        if (!Modules.get().isActive(FakePlayer.class)) {
            error("The FakePlayer module must be enabled.");
            return false;
        } else return true;
    }
}
