/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.builtin;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmWorker;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Random;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SwarmCommand extends Command {

    private final static SimpleCommandExceptionType SWARM_NOT_ACTIVE = new SimpleCommandExceptionType(Text.literal("The swarm module must be active to use this command."));

    public SwarmCommand() {
        super("swarm", "Sends commands to connected swarm workers.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientCommandSource> builder) {
        builder.then(literal("disconnect").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);

            require(swarm.isActive(), SWARM_NOT_ACTIVE);
            swarm.close();

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("join")
                .then(argument("ip", StringArgumentType.string())
                        .then(argument("port", IntegerArgumentType.integer(0, 65535))
                                .executes(context -> {
                                        Swarm swarm = Modules.get().get(Swarm.class);
                                        if (!swarm.isActive()) swarm.toggle();

                                        swarm.close();
                                        swarm.mode.set(Swarm.Mode.Worker);
                                        swarm.worker = new SwarmWorker(StringArgumentType.getString(context, "ip"), IntegerArgumentType.getInteger(context, "port"));

                                        return SINGLE_SUCCESS;
                                })
                        )
                )
        );

        builder.then(literal("connections").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);
            if (swarm.isHost()) {
                if (swarm.host.getConnectionCount() > 0) {
                    ChatUtils.info("--- Swarm Connections (highlight)(%s/%s)(default) ---", swarm.host.getConnectionCount(), swarm.host.getConnections().length);

                    for (int i = 0; i < swarm.host.getConnections().length; i++) {
                        SwarmConnection connection = swarm.host.getConnections()[i];
                        if (connection != null) ChatUtils.info("(highlight)Worker %s(default): %s.", i, connection.connectionString());
                    }
                } else {
                    warning("No active connections");
                }
            }
            else if (swarm.isWorker()) {
                info("Connected to (highlight)%s", swarm.worker.getConnection());
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("follow").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput() + " " + mc.player.getEntityName());
            }
            else if (swarm.isWorker()) {
                error("The follow host command must be used by the host.");
            }

            return SINGLE_SUCCESS;
        }).then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity playerEntity = PlayerArgumentType.get(context);

            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            }
            else if (swarm.isWorker() && playerEntity != null) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getFollowProcess().follow(entity -> entity.getEntityName().equalsIgnoreCase(playerEntity.getEntityName()));
            }
            return SINGLE_SUCCESS;
        }))
        );

        builder.then(literal("goto")
                .then(argument("x", IntegerArgumentType.integer())
                        .then(argument("z", IntegerArgumentType.integer()).executes(context -> {
                            Swarm swarm = Modules.get().get(Swarm.class);
                            require(swarm.isActive(), SWARM_NOT_ACTIVE);

                            if (swarm.isHost()) {
                                swarm.host.sendMessage(context.getInput());
                            }
                            else if (swarm.isWorker()) {
                                int x = IntegerArgumentType.getInteger(context, "x");
                                int z = IntegerArgumentType.getInteger(context, "z");

                                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(x, z));
                            }

                            return SINGLE_SUCCESS;
                        }))
                )
        );

        builder.then(literal("infinity-miner").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            }
            else if (swarm.isWorker()) {
                runInfinityMiner();
            }

            return SINGLE_SUCCESS;
        })
        .then(argument("target", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            }
            else if (swarm.isWorker()) {
                Modules.get().get(InfinityMiner.class).targetBlocks.set(List.of(context.getArgument("target", BlockStateArgument.class).getBlockState().getBlock()));
                runInfinityMiner();
            }

            return SINGLE_SUCCESS;
        })
        .then(argument("repair", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            }
            else if (swarm.isWorker()) {
                Modules.get().get(InfinityMiner.class).targetBlocks.set(List.of(context.getArgument("target", BlockStateArgument.class).getBlockState().getBlock()));
                Modules.get().get(InfinityMiner.class).repairBlocks.set(List.of(context.getArgument("repair", BlockStateArgument.class).getBlockState().getBlock()));
                runInfinityMiner();
            }

            return SINGLE_SUCCESS;
        })))
        .then(literal("logout").then(argument("logout", BoolArgumentType.bool()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            }
            else if (swarm.isWorker()) {
                Modules.get().get(InfinityMiner.class).logOut.set(BoolArgumentType.getBool(context, "logout"));
            }

            return SINGLE_SUCCESS;
        })))
        .then(literal("walkhome").then(argument("walkhome", BoolArgumentType.bool()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            } else if (swarm.isWorker()) {
                Modules.get().get(InfinityMiner.class).walkHome.set(BoolArgumentType.getBool(context, "walkhome"));
            }

            return SINGLE_SUCCESS;
        }))));

        builder.then(literal("mine")
                .then(argument("block", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).executes(context -> {
                    Swarm swarm = Modules.get().get(Swarm.class);
                    require(swarm.isActive(), SWARM_NOT_ACTIVE);

                    if (swarm.isHost()) {
                        swarm.host.sendMessage(context.getInput());
                    } else if (swarm.isWorker()) {
                        swarm.worker.target = context.getArgument("block", BlockStateArgument.class).getBlockState().getBlock();
                    }

                    return SINGLE_SUCCESS;
                }))
        );

        builder.then(literal("toggle")
                .then(argument("module", ModuleArgumentType.create())
                        .executes(context -> {
                            Swarm swarm = Modules.get().get(Swarm.class);
                            require(swarm.isActive(), SWARM_NOT_ACTIVE);

                            if (swarm.isHost()) {
                                swarm.host.sendMessage(context.getInput());
                            } else if (swarm.isWorker()) {
                                ModuleArgumentType.get(context).toggle();
                            }

                            return SINGLE_SUCCESS;
                        }).then(literal("on")
                                .executes(context -> {
                                    Swarm swarm = Modules.get().get(Swarm.class);
                                    require(swarm.isActive(), SWARM_NOT_ACTIVE);

                                    if (swarm.isHost()) {
                                        swarm.host.sendMessage(context.getInput());
                                    } else if (swarm.isWorker()) {
                                        ModuleArgumentType.get(context).enable();
                                    }

                                    return SINGLE_SUCCESS;
                                })).then(literal("off")
                                .executes(context -> {
                                    Swarm swarm = Modules.get().get(Swarm.class);
                                    require(swarm.isActive(), SWARM_NOT_ACTIVE);

                                    if (swarm.isHost()) {
                                        swarm.host.sendMessage(context.getInput());
                                    } else if (swarm.isWorker()) {
                                        ModuleArgumentType.get(context).disable();
                                    }

                                    return SINGLE_SUCCESS;
                                })
                        )
                )
        );

        builder.then(literal("scatter").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            } else if (swarm.isWorker()) {
                scatter(100);
            }

            return SINGLE_SUCCESS;
        }).then(argument("radius", IntegerArgumentType.integer()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            } else if (swarm.isWorker()) {
                scatter(IntegerArgumentType.getInteger(context, "radius"));
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("stop").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            } else if (swarm.isWorker()) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("exec").then(argument("command", StringArgumentType.greedyString()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            require(swarm.isActive(), SWARM_NOT_ACTIVE);

            if (swarm.isHost()) {
                swarm.host.sendMessage(context.getInput());
            } else if (swarm.isWorker()) {
                ChatUtils.sendPlayerMsg(StringArgumentType.getString(context, "command"));
            }

            return SINGLE_SUCCESS;
        })));
    }

    private void runInfinityMiner() {
        InfinityMiner infinityMiner = Modules.get().get(InfinityMiner.class);
        infinityMiner.disable();
        infinityMiner.enable();
    }

    private void scatter(int radius) {
        Random random = new Random();
        double a = random.nextDouble() * 2 * Math.PI;
        double r = radius * Math.sqrt(random.nextDouble());
        double x = mc.player.getX() + r * Math.cos(a);
        double z = mc.player.getZ() + r * Math.sin(a);
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) x, (int) z));
    }
}
