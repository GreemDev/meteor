/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import com.google.gson.Gson;
import com.mojang.util.UUIDTypeAdapter;
import meteordevelopment.meteorclient.systems.accounts.TexturesJson;
import meteordevelopment.meteorclient.systems.accounts.UuidToProfileResponse;
import meteordevelopment.meteorclient.utils.PostInit;
import net.greemdev.meteor.util.Http;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class PlayerHeadUtils {
    public static PlayerHeadTexture STEVE_HEAD;

    @PostInit
    public static void init() {
        STEVE_HEAD = new PlayerHeadTexture();
    }

    public static Optional<PlayerHeadTexture> fetchHead(UUID id) {
        String url = getSkinUrl(id);
        return Optional.ofNullable(url != null
            ? new PlayerHeadTexture(url)
            : null);
    }

    public static String getSkinUrl(UUID id) {
        if (id == null) return null;
        UuidToProfileResponse res = Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(id))
            .requestJson(UuidToProfileResponse.class);
        if (res == null) return null;

        String base64Textures = res.getPropertyValue("textures");
        if (base64Textures == null) return null;

        TexturesJson textures = new Gson().fromJson(new String(Base64.getDecoder().decode(base64Textures)), TexturesJson.class);
        if (textures.textures.SKIN == null) return null;

        return textures.textures.SKIN.url;
    }
}
