/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.events.entity.player;

public abstract class SendMovementPacketsEvent {
    public static class Pre extends SendMovementPacketsEvent {
        private static final Pre INSTANCE = new Pre();

        public static SendMovementPacketsEvent.Pre get() {
            return INSTANCE;
        }
    }

    public static class Post extends SendMovementPacketsEvent {
        private static final Post INSTANCE = new Post();

        public static SendMovementPacketsEvent.Post get() {
            return INSTANCE;
        }
    }
}
