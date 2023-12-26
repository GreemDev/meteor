/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {

    @Inject(method = "addStackTrace", at = @At("TAIL"))
    private void onAddStackTrace(StringBuilder crashReportBuilder, CallbackInfo info) {
        crashReportBuilder.append("\n\n-- Meteor Client --\n\n");
        crashReportBuilder.append("Do NOT report this to the original Meteor Client developers. Report only to GreemDev.\n");
        crashReportBuilder.append("Version: ").append(MeteorClient.fullVersion()).append("\n");

        if (Modules.get() != null) {
            boolean modulesActive = false;
            for (Category category : Modules.categories()) {
                List<Module> modules = Modules.get().getGroup(category);
                boolean categoryActive = false;

                for (Module module : modules) {
                    if (module == null || !module.isActive()) continue;

                    if (!modulesActive) {
                        modulesActive = true;
                        crashReportBuilder.append("\n[[ Active Modules ]]\n");
                    }

                    if (!categoryActive) {
                        categoryActive = true;
                        crashReportBuilder.append("\n[")
                          .append(category)
                          .append("]:\n");
                    }

                    crashReportBuilder.append(module.name).append("\n");
                }
            }
        }

        if (Hud.get() != null && Hud.get().active) {
            boolean hudActive = false;
            for (HudElement element : Hud.get()) {
                if (element == null || !element.isActive()) continue;

                if (!hudActive) {
                    hudActive = true;
                    crashReportBuilder.append("\n[[ Active Hud Elements ]]\n");
                }

                if (element instanceof TextHud textHud) {
                    crashReportBuilder.append("Text\n{")
                        .append(textHud.text.get())
                        .append("}\n");
                    if (!textHud.shown.get().always()) {
                        crashReportBuilder.append("(")
                            .append(textHud.shown.get())
                            .append(textHud.condition.get())
                            .append(")\n");
                    }
                }
                else crashReportBuilder.append(element.info.name).append("\n");
            }
        }

        if (Modules.get() == null && Hud.get() == null) {
            crashReportBuilder.append("\nThis crash happened too early for Modules or Hud to be available.");
        } else
            crashReportBuilder.append("\n\n");
    }
}
