/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.argument
import net.greemdev.meteor.util.misc.editPos
import net.greemdev.meteor.util.misc.network
import net.greemdev.meteor.util.misc.player
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket
import net.minecraft.util.math.Vec3d
import kotlin.math.ceil


/*
 * Kotlin reimplementation of the previously divided HClip and VClip commands.
 * Reimplemented because there's no real need to have 2 functionally similar commands be separate.
 */
object ClipCommand : GCommand("clip", "Lets you clip through blocks vertically or horizontally.", {
    then("v") {
        then("distance", arg.double()) {
            alwaysRuns {
                val distance by it.argument(arg.double(), "distance")

                // Implementation of "PaperClip" aka "TPX" aka "VaultClip" into vclip
                // Allows you to teleport up to 200 blocks in one go (as you can send 20 move packets per tick)
                // Paper allows you to teleport 10 blocks for each move packet you send in that tick
                // Video explanation by LiveOverflow: https://www.youtube.com/watch?v=3HSnDsfkJT8
                val packetsRequired = ceil(distance / 10).toInt()
                val p = mc.player()
                mc.network {
                    if (p.hasVehicle()) {
                        // Vehicle version
                        // For each 10 blocks, send a vehicle move packet with no delta
                        for (packetNumber in 0 until packetsRequired - 1) {
                            sendPacket(VehicleMoveC2SPacket(p.vehicle))
                        }

                        p.vehicle?.editPos(y = distance)
                        sendPacket(VehicleMoveC2SPacket(p.vehicle))
                    } else {
                        // No vehicle version
                        // For each 10 blocks, send a player move packet with no delta
                        for (packetNumber in 0 until packetsRequired - 1) {
                            sendPacket(PlayerMoveC2SPacket.OnGroundOnly(true))
                        }

                        // Now send the final player move packet
                        sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(p.x, p.y + distance, p.z, true))
                        p.editPos(y = distance)
                    }
                }
            }
        }
    }
    then("h") {
        then("distance", arg.double()) {
            alwaysRuns {
                val distance by it.argument(arg.double(), "distance")
                val forward = Vec3d.fromPolar(0f, mc.player().yaw).normalize()
                mc.player().vehicle?.editPos(
                    x = forward.x * distance,
                    z = forward.z * distance
                )
                mc.player().editPos(
                    x = forward.x * distance,
                    z = forward.z * distance
                )
            }
        }
    }
})
