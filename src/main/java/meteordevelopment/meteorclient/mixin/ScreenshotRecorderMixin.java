/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.greemdev.meteor.Greteor;
import net.greemdev.meteor.modules.greteor.GameTweaks;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

    @Inject(method = "saveScreenshotInner", at = @At("HEAD"), cancellable = true)
    private static void onScreenshot(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (Modules.get().get(GameTweaks.class).screenshots()) {
            try (var nativeImage = ScreenshotRecorder.takeScreenshot(framebuffer)) {
                var image = Toolkit.getDefaultToolkit().createImage(nativeImage.getBytes());
                var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(createTransferable(image), null);
                ChatUtils.info("Game Tweaks", "Copied screenshot to clipboard.");
                ci.cancel();
            } catch (IOException ioe) {
                ChatUtils.error("Game Tweaks", "Internal error: " + ioe.getMessage());
                Greteor.logger().catching(Level.ERROR, ioe);
            }
        }
    }

    @Unique
    private static Transferable createTransferable(Image image) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor == DataFlavor.imageFlavor;
            }

            @NotNull
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (isDataFlavorSupported(flavor))
                    return image;
                else
                    throw new UnsupportedFlavorException(flavor);
            }
        };
    }


}
