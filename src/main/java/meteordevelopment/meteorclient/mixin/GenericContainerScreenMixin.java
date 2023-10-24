/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.utils.render.ContainerButtonWidget;
import net.greemdev.meteor.util.misc.ButtonWidgetBuilder;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    public GenericContainerScreenMixin(GenericContainerScreenHandler container, PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    @Override
    protected void init() {
        super.init();

        InventoryTweaks invTweaks = Modules.get().get(InventoryTweaks.class);

        if (invTweaks.isActive() && invTweaks.showButtons()) {
            addDrawableChild(new ButtonWidgetBuilder()
                .x(x + backgroundWidth - 88)
                .y(y + 3)
                .width(40)
                .height(12)
                .text("Steal")
                .onPress(button -> invTweaks.steal(handler))
                .buildContainer()
            );

            addDrawableChild(new ButtonWidgetBuilder()
                .x(x + backgroundWidth - 46)
                .y(y + 3)
                .width(40)
                .height(12)
                .text("Dump")
                .onPress(button -> invTweaks.dump(handler))
                .buildContainer()
            );
        }

        if (invTweaks.autoSteal()) invTweaks.steal(handler);
        if (invTweaks.autoDump()) invTweaks.dump(handler);
    }
}
