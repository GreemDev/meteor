/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.notebot.instrumentdetect;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiFunction;

public interface InstrumentDetectFunction extends BiFunction<BlockState, BlockPos, Instrument> {
    /**
     * Detects an instrument for noteblock
     *
     * @param noteBlock Noteblock state
     * @param blockPos Noteblock position
     * @return Detected instrument
     */
    Instrument detectInstrument(BlockState noteBlock, BlockPos blockPos);

    default Instrument apply(BlockState arg1, BlockPos arg2) {
        return detectInstrument(arg1, arg2);
    }
}
