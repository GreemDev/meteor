/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.utils;

import org.jetbrains.annotations.*;

public interface CharFilter {

    /**
     * Returns the given parameter filter if it is not null.<br/>
     * If it is null, return the default filter of {@link CharFilter#none}
     * @param filter The nullable filter.
     * @return A non-nullable {@link CharFilter} instance.
     */
    @NotNull
    static CharFilter orDefault(@Nullable CharFilter filter) {
        return filter != null
            ? filter
            : none();
    }

    /**
     * Returns the default {@link CharFilter} instance, which returns true always; effectively no filter.
     * @return A {@link CharFilter} that always returns {@code true}.
     */
    static CharFilter none() {
        return (t, c) -> true;
    }

    boolean filter(String text, char c);
}
