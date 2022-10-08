/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin.starscript;

import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Starscript.class, remap = false)
public abstract class StarscriptMixin {

    @Shadow public abstract Value pop();
    @Shadow public abstract void error(String format, Object... args);

    /**
     * @author GreemDev
     * @reason Default Starscript returns isBool here, NOT what we want! That ONLY checks if the value is of a bool type, NOT the bool's value.
     */
    @Overwrite
    public boolean popBool(String errorMsg) {
        Value a = pop();
        if (!a.isBool()) error(errorMsg);
        return a.getBool();
    }
}
