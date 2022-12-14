/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.commands.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.arguments.PlayerArgumentType;
import net.greemdev.meteor.util.misc.KMC;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SpectateCommand extends Command {

    private final StaticListener shiftListener = new StaticListener();

    public SpectateCommand() {
        super("spectate", "Allows you to spectate nearby players.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset")
                .executes(context -> {
                    mc.setCameraEntity(mc.player);
                    return SINGLE_SUCCESS;
                }))
            .then(argument("player", PlayerArgumentType.create())
                .executes(context -> {
                    mc.setCameraEntity(PlayerArgumentType.get(context));
                    KMC.showActionBar(mc, "Sneak to un-spectate.");
                    MeteorClient.EVENT_BUS.subscribe(shiftListener);
                    return SINGLE_SUCCESS;
                })
            );
    }

    private static class StaticListener {
        @EventHandler
        private void onKey(KeyEvent event) {
            if (mc.options.sneakKey.matchesKey(event.key, 0) || mc.options.sneakKey.matchesMouse(event.key)) {
                mc.setCameraEntity(mc.player);
                event.cancel();
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}
