/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class AutoClicker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> attack = sgGeneral.add(new BoolSetting.Builder()
        .name("attack")
        .description("Whether to automatically press Attack.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Mode> aMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("attack-mode")
        .description("The method of clicking the Attack button.")
        .defaultValue(Mode.Press)
        .visible(attack::get)
        .build()
    );

    private final Setting<Integer> aDelay = sgGeneral.add(new IntSetting.Builder()
        .name("attack-click-delay")
        .description("The amount of delay between attack clicks in ticks.")
        .defaultValue(2)
        .range(0, 1200)
        .visible(attack::get)
        .build()
    );

    private final Setting<Boolean> use = sgGeneral.add(new BoolSetting.Builder()
        .name("use-item")
        .description("Whether to automatically press Use Item.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Mode> uMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("use-mode")
        .description("The method of clicking the Use Item button.")
        .defaultValue(Mode.Press)
        .visible(use::get)
        .build()
    );

    private final Setting<Integer> uDelay = sgGeneral.add(new IntSetting.Builder()
        .name("use-click-delay")
        .description("The amount of delay between Use Item clicks in ticks.")
        .defaultValue(2)
        .range(0, 1200)
        .visible(use::get)
        .build()
    );

    private int attackTimer, useTimer;

    public AutoClicker() {
        super(Categories.Player, "auto-clicker", "Automatically clicks.");
    }

    @Override
    public void onActivate() {
        attackTimer = 0;
        useTimer = 0;
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
    }

    @Override
    public void onDeactivate() {
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (attack.get()) {
            switch (aMode.get()) {
                case Hold -> mc.options.attackKey.setPressed(true);
                case Press -> {
                    attackTimer++;
                    if (attackTimer > aDelay.get()) {
                        Utils.pressAttackKey();
                        attackTimer = 0;
                    }
                }
            }
        }

        if (use.get()) {
            switch (uMode.get()) {
                case Hold -> mc.options.useKey.setPressed(true);
                case Press -> {
                    useTimer++;
                    if (useTimer > uDelay.get()) {
                        Utils.pressItemUseKey();
                        useTimer = 0;
                    }
                }
            }
        }
    }

    public enum Mode {
        Hold,
        Press
    }
}
