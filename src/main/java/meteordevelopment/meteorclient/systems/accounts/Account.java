/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.util.Session;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public abstract class Account<T extends Account<?>> implements ISerializable<T> {
    protected UUID id;
    protected Type type;
    protected String name;

    protected final AccountCache cache;

    public Account(Type type, String name) {
        this.type = type;
        this.name = name;
        this.cache = new AccountCache();
        this.id = UUID.randomUUID();
    }

    public abstract boolean fetchInfo();

    public boolean login() {
        YggdrasilMinecraftSessionService service = (YggdrasilMinecraftSessionService) mc.getSessionService();
        AccountUtils.setBaseUrl(service, YggdrasilEnvironment.PROD.getEnvironment().getSessionHost() + "/session/minecraft/");
        AccountUtils.setJoinUrl(service, YggdrasilEnvironment.PROD.getEnvironment().getSessionHost() + "/session/minecraft/join");
        AccountUtils.setCheckUrl(service, YggdrasilEnvironment.PROD.getEnvironment().getSessionHost() + "/session/minecraft/hasJoined");

        return true;
    }

    public String getUsername() {
        if (cache.username.isEmpty()) return name;
        return cache.username;
    }

    public UUID getLocalId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public AccountCache getCache() {
        return cache;
    }

    protected void setSession(Session session) {
        ((MinecraftClientAccessor) mc).setSession(session);
        mc.getSessionProperties().clear();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putUuid("localId",
            id != null
                ? id
                : UUID.randomUUID()
        );
        tag.putString("type", type.name());
        tag.putString("name", name);
        tag.put("cache", cache.toTag());

        return tag;
    }

    @Override
    public T fromTag(NbtCompound tag) {
        if (!tag.contains("name") || !tag.contains("cache")) throw new NbtException();


        id = tag.containsUuid("localId")
            ? tag.getUuid("localId")
            : UUID.randomUUID();

        name = tag.getString("name");
        cache.fromTag(tag.getCompound("cache"));

        return Utils.cast(this);
    }

    public enum Type {
        Cracked,
        Microsoft,
        TheAltening
    }
}
