/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.accounts.types;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UndashedUuid;
import meteordevelopment.meteorclient.mixin.accessor.MinecraftClientAccessor;
import meteordevelopment.meteorclient.mixin.accessor.YggdrasilMinecraftSessionServiceAccessor;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import net.greemdev.meteor.util.HTTP;
import net.minecraft.client.session.Session;

import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EasyMCAccount extends Account<EasyMCAccount> {

    private static final Environment ENVIRONMENT = new Environment("https://authserver.mojang.com", "https://sessionserver.easymc.io", "https://api.minecraftservices.com", "EasyMC");
    private static final YggdrasilAuthenticationService SERVICE = new YggdrasilAuthenticationService(((MinecraftClientAccessor) mc).getProxy(), ENVIRONMENT);

    public EasyMCAccount(String token) {
        super(AccountType.EasyMC, token);
    }

    @Override
    public boolean fetchInfo() {
        // we set the name to the session id after we redeem the token - the token length is 20, session id length is 43
        if (name.length() == 43) return true;

        AuthResponse res = HTTP.post("https://api.easymc.io/v1/token/redeem")
            .bodyJson("{\"token\":\"" + name + "\"}")
            .requestJson(AuthResponse.class);

        if (res != null) {
            cache.username = res.mcName;
            cache.uuid = res.uuid;

            name = res.session;

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean login() {
        applyLoginEnvironment(SERVICE, YggdrasilMinecraftSessionServiceAccessor.createYggdrasilMinecraftSessionService(SERVICE.getServicesKeySet(), SERVICE.getProxy(), ENVIRONMENT));
        setSession(new Session(cache.username, UndashedUuid.fromStringLenient(cache.uuid), name, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));

        cache.loadHead();
        return true;
    }

    private static class AuthResponse {
        public String mcName;
        public String uuid;
        public String session;
        public String message;
    }
}
