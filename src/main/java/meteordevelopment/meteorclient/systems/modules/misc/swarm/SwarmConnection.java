/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SwarmConnection extends Thread {
    public final Socket socket;
    public String messageToSend;

    public SwarmConnection(Socket socket) {
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        ChatUtils.infoPrefix("Swarm", "New worker connected on %s.", connectionString());

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (!isInterrupted()) {
                if (messageToSend != null) {
                    try {
                        out.writeUTF(messageToSend);
                        out.flush();
                    } catch (Exception e) {
                        ChatUtils.errorPrefix("Swarm", "Encountered error when sending command to %s.", connectionString());
                        MeteorClient.LOG.error("[Swarm] Error when sending command to %s.".formatted(connectionString()), e);
                    }

                    messageToSend = null;
                }
            }

            out.close();
        } catch (IOException e) {
            ChatUtils.infoPrefix("Swarm", "Error creating a connection with %s.", connectionString());
            MeteorClient.LOG.error("[Swarm] Error creating a connection with %s.".formatted(connectionString()), e);
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            MeteorClient.LOG.error("[Swarm] Error closing a connection at %s.".formatted(connectionString()), e);
        }

        ChatUtils.infoPrefix("Swarm", "Worker disconnected from %s.", connectionString());

        interrupt();
    }

    public String connectionString() {
        return formatIp(socket.getInetAddress().getHostAddress()) + ":" + socket.getPort();
    }

    private String formatIp(String ip) {
        return ip.equals("127.0.0.1") ? "localhost" : ip;
    }
}
