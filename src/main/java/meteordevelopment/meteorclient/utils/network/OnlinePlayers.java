/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import net.greemdev.meteor.util.HTTP;

public final class OnlinePlayers {
    private static long lastPingTime;

    public static void update() {
        long time = System.currentTimeMillis();

        if (time - lastPingTime > 5 * 60 * 1000) {
            MeteorExecutor.execute(() -> HTTP.post("https://meteorclient.com/api/online/ping").send());

            lastPingTime = time;
        }
    }

    public static void leave() {
        MeteorExecutor.execute(() -> HTTP.post("https://meteorclient.com/api/online/leave").send());
    }
}
