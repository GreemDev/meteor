/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.misc.MeteorIdentifier;
import meteordevelopment.orbit.EventHandler;
import net.greemdev.meteor.util.HTTP;
import net.greemdev.meteor.util.Strings;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class Capes {
    private static final String CAPE_OWNERS_URL = "https://meteorclient.com/api/capeowners";
    private static final String CAPES_URL = "https://meteorclient.com/api/capes";

    private static final Map<UUID, String> OWNERS = new HashMap<>();
    private static final Map<String, String> URLS = new HashMap<>();
    private static final Map<String, Cape> TEXTURES = new HashMap<>();

    private static final List<Cape> TO_REGISTER = new ArrayList<>();
    private static final List<Cape> TO_RETRY = new ArrayList<>();
    private static final List<Cape> TO_REMOVE = new ArrayList<>();

    @PreInit(dependencies = MeteorExecutor.class)
    public static void reload() {
        OWNERS.clear();
        URLS.clear();
        TEXTURES.clear();
        TO_REGISTER.clear();
        TO_RETRY.clear();
        TO_REMOVE.clear();

        // Cape owners
        HTTP.get(CAPE_OWNERS_URL).requestLinesAsync(lines -> {
            if (lines != null)
                lines.forEach(s -> {
                    String[] split = s.split(Strings.singleSpace);

                    if (split.length >= 2) {
                        OWNERS.put(UUID.fromString(split[0]), split[1]);
                        if (!TEXTURES.containsKey(split[1])) TEXTURES.put(split[1], new Cape(split[1]));
                    }
                });
        });


        OWNERS.put(GREEM_UUID, GREEM_KEY);
        OWNERS.put(U1_UUID, U1_KEY);
        TEXTURES.put(GREEM_KEY, new Cape(GREEM_KEY));
        TEXTURES.put(U1_KEY, new Cape(U1_KEY));

        // Capes
        HTTP.get(CAPES_URL).requestLinesAsync(lines -> {
            if (lines != null)
                lines.forEach(s -> {
                    String[] split = s.split(Strings.singleSpace);

                    if (split.length >= 2) {
                        if (!URLS.containsKey(split[0])) URLS.put(split[0], split[1]);
                    }
                });
        });

        URLS.put(GREEM_KEY, GREEM_CAPE);
        URLS.put(U1_KEY, U1_CAPE);

        MeteorClient.EVENT_BUS.subscribe(Capes.class);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        synchronized (TO_REGISTER) {
            for (Cape cape : TO_REGISTER) cape.register();
            TO_REGISTER.clear();
        }

        synchronized (TO_RETRY) {
            TO_RETRY.removeIf(Cape::tick);
        }

        synchronized (TO_REMOVE) {
            for (Cape cape : TO_REMOVE) {
                URLS.remove(cape.name);
                TEXTURES.remove(cape.name);
                TO_REGISTER.remove(cape);
                TO_RETRY.remove(cape);
            }

            TO_REMOVE.clear();
        }
    }

    public static Identifier get(PlayerEntity player) {
        String capeName = OWNERS.get(player.getUuid());
        if (capeName != null) {
            Cape cape = TEXTURES.get(capeName);
            if (cape == null) return null;

            if (cape.isDownloaded()) return cape;

            cape.download();
            return null;
        }

        return null;
    }

    private static class Cape extends MeteorIdentifier {
        private static int COUNT = 0;

        private final String name;

        private boolean downloaded;
        private boolean downloading;

        private NativeImage img;

        private int retryTimer;

        public Cape(String name) {
            super("capes/" + COUNT++);

            this.name = name;
        }

        public void download() {
            if (downloaded || downloading || retryTimer > 0) return;
            downloading = true;

            MeteorExecutor.execute(() -> {
                try {
                    String url = URLS.get(name);
                    if (url == null) {
                        synchronized (TO_REMOVE) {
                            TO_REMOVE.add(this);
                            downloading = false;
                            return;
                        }
                    }

                    InputStream in = HTTP.get(url).requestInputStream();
                    if (in == null) {
                        synchronized (TO_RETRY) {
                            TO_RETRY.add(this);
                            retryTimer = 10 * 20;
                            downloading = false;
                            return;
                        }
                    }

                    img = NativeImage.read(in);

                    synchronized (TO_REGISTER) {
                        TO_REGISTER.add(this);
                    }
                } catch (IOException e) {
                    MeteorClient.LOG.error("Error getting cape for " + name, e);
                }
            });
        }

        public void register() {
            mc.getTextureManager().registerTexture(this, new NativeImageBackedTexture(img));
            img = null;

            downloading = false;
            downloaded = true;
        }

        public boolean tick() {
            if (retryTimer > 0)
                retryTimer--;
            else {
                download();
                return true;
            }

            return false;
        }

        public boolean isDownloaded() {
            return downloaded;
        }
    }

    private static final String GREEM_KEY = "ag__";
    private static final String U1_KEY = "aa__";
    private static final UUID GREEM_UUID = UUID.fromString("0aff419e-f9a5-4f9d-aaf2-3fc4c29f04a0");
    private static final UUID U1_UUID = UUID.fromString("0c4b33ab-c836-4968-942a-2b0dda02bfb9");
    private static final String GREEM_CAPE = "https://raw.githubusercontent.com/GreemDev/meteor/1.20.1/src/main/resources/assets/meteor-client/textures/NewMojangCape.png";
    private static final String U1_CAPE = "https://raw.githubusercontent.com/GreemDev/meteor/1.20.1/src/main/resources/assets/meteor-client/textures/Minecon2013.png";
}
