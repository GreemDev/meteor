/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.greemdev.meteor.Greteor;
import net.greemdev.meteor.modules.GameTweaks;
import net.greemdev.meteor.util.text.ChatColor;
import net.greemdev.meteor.util.text.FormattedText;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
import java.util.Arrays;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

    @Shadow
    private static File getScreenshotFilename(File directory) { throw null; }

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "saveScreenshotInner", at = @At("HEAD"), cancellable = true)
    private static void onScreenshot(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (GameTweaks.screenshots()) {
            try (var nativeImage = ScreenshotRecorder.takeScreenshot(framebuffer)) {
                var image = Toolkit.getDefaultToolkit().createImage(nativeImage.getBytes());
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(createTransferable(image), null);
                GameTweaks.INSTANCE.info("Copied screenshot to clipboard.");

                if (GameTweaks.screenshotFile()) {
                    // normal behavior of saveScreenshotInner because I didn't want to take the screenshot twice so I copied the code into here.
                    // it's behaviorally different though; the messages sent to the player are routed through meteor's utilities instead of messageReceiver
                    File screenshotFile = getScreenshotFile(gameDirectory, fileName);

                    MeteorExecutor.executeOnIoExecutor(() -> {
                        try {
                            nativeImage.writeTo(screenshotFile);
                            GameTweaks.INSTANCE.info(Text.translatable(
                                "screenshot.success",
                                FormattedText.fileHyperlink(
                                    screenshotFile.getName(),
                                    screenshotFile.getAbsolutePath()
                                )
                            ));
                        } catch (IOException ioe) {
                            LOGGER.warn("Couldn't save screenshot", ioe);
                            GameTweaks.INSTANCE.info(FormattedText.withColor(
                                Text.translatable("screenshot.failure", ioe.getMessage()),
                                ChatColor.red
                            ));
                        }
                    });
                }

                ci.cancel();
            } catch (IOException ioe) {
                GameTweaks.INSTANCE.error("Internal error: %s", ioe.getMessage());
                Greteor.logger().error("Error taking/saving screenshot", ioe);
            }
        }
    }

    @NotNull
    @Unique
    private static File getScreenshotFile(File gameDirectory, @Nullable String fileName) {
        File screenshotsDir = new File(gameDirectory, "screenshots");
        screenshotsDir.mkdir();
        return fileName != null
            ? new File(screenshotsDir, fileName)
            : getScreenshotFilename(screenshotsDir);
    }

    @Unique
    private static Transferable createTransferable(Image image) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.imageFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return Arrays.stream(getTransferDataFlavors()).anyMatch(flavor::equals);
            }

            @NotNull
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);

                return image;
            }
        };
    }
}
