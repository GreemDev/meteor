/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.accounts;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;
import meteordevelopment.meteorclient.systems.accounts.types.TheAlteningAccount;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;

import java.util.*;

public class Accounts extends System<Accounts> implements Iterable<Account<?>> {
    private List<Account<?>> accounts = new ArrayList<>();
    private UUID autoLogin;

    public static UUID getAutoLogin() {
        return get().autoLogin;
    }

    private static boolean autoLoginDone = false;

    public static void setAutoLogin(UUID id) {
        get().autoLogin = id;
    }

    public Accounts() {
        super("accounts");
    }

    public void autoLogin() {
        if (autoLoginDone) return;

        MeteorExecutor.execute(() -> {
            for (; ; ) {
                if (Utils.canOpenGui()) {
                    if (autoLogin != null && exists(autoLogin)) {
                        if (get(autoLogin).login()) {
                            save();
                            autoLoginDone = true;
                        }
                    }
                    break;
                }
            }
        });
    }

    @PostInit
    public static void alInit() {
        get().autoLogin();
    }

    public static Accounts get() {
        return Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        accounts.add(account);
        save();
    }

    public Account<?> get(UUID localId) {
        return accounts.stream()
            .filter(acc -> acc.id.equals(localId))
            .findFirst()
            .orElse(null);
    }

    public boolean exists(UUID localId) {
        return accounts.stream().anyMatch(a -> a.getLocalId().equals(localId));
    }

    public boolean exists(Account<?> account) {
        return accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (accounts.remove(account)) {
            save();
        }
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public Iterator<Account<?>> iterator() {
        return accounts.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("accounts", NbtUtils.listToTag(accounts));

        if (autoLogin != null)
            tag.putUuid("autoLogin", autoLogin);

        return tag;
    }

    @Override
    public Accounts fromTag(NbtCompound tag) {
        MeteorExecutor.execute(() ->
            accounts = NbtUtils.listFromTag(tag.getList("accounts", 10), tag1 -> {
                NbtCompound t = (NbtCompound) tag1;
                if (!t.contains("type")) return null;

                Account.Type type = Account.Type.valueOf(t.getString("type"));

                try {
                    Account<?> account = switch (type) {
                        case Cracked -> new CrackedAccount(null).fromTag(t);
                        case Microsoft -> new MicrosoftAccount(null).fromTag(t);
                        case TheAltening -> new TheAlteningAccount(null).fromTag(t);
                    };

                    if (account.fetchInfo()) return account;
                } catch (NbtException e) {
                    return null;
                }

                return null;
            })
        );

        if (tag.containsUuid("autoLogin"))
            autoLogin = tag.getUuid("autoLogin");

        return this;
    }
}
