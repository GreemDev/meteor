/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface CharFilter {

    /**
     * Returns the given parameter filter if it is not null.<br/>
     * If it is null, return the default filter of {@link CharFilter#none}
     * @param filter The nullable filter.
     * @return A non-nullable {@link CharFilter} instance.
     */
    @NotNull
    static CharFilter orNone(@Nullable CharFilter filter) {
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

    /**
     * {@link CharFilter} for only alphanumeric characters, with leniency for periods, hyphens, underscores, and spaces.
     * @return A lenient alphanumeric {@link CharFilter}.
     */
    static CharFilter lenientAlphanumeric() {
        return alphanumeric(true, true, true, true);
    }

    /**
     * {@link CharFilter} for only alphanumeric characters.
     * @return A strictly alphanumeric {@link CharFilter}.
     */
    static CharFilter strictAlphanumeric() {
        return alphanumeric(false, false, false, false);
    }

    /**
     * An adjustable alphanumeric {@link CharFilter}.
     * @param period Allow .
     * @param hyphen Allow -
     * @param underscore Allow _
     * @param space Allow spaces
     * @return A {@link CharFilter} accepting alphanumeric characters.
     */
    static CharFilter alphanumeric(boolean period, boolean hyphen, boolean underscore, boolean space) {
        return (t, c) ->
            (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                (underscore && c == '_') ||
                (hyphen && c == '-') ||
                (period && c == '.') ||
                (space && c == ' ');
    }

    /**
     * A {@link CharFilter} that accepts only IP addresses/hostnames.
     * @return An IP/hostname {@link CharFilter}.
     */
    static CharFilter ip(boolean allowPorts) {
        return (t, c) -> {
            if (!allowPorts && (t.contains(":") && c == ':')) return false;
            return alphanumeric(true, true, true, false).filter(t, c);
        };
    }

    /**
     * A {@link CharFilter} that ensures the input content is at most length long.
     * @return A length-capping {@link CharFilter}.
     */
    static CharFilter noLongerThan(int length) {
        return (t, c) -> t.length() <= length;
    }

    /**
     * A {@link CharFilter} combinator, allowing you to more easily combine the conditions of 2 different {@link CharFilter} together into one callable {@link CharFilter}.
     * <br/><br/>
     * An example use for this would be {@code combine(strictAlphanumeric(), noLongerThan(25))} to create a {@link CharFilter} that is both strictly alphanumeric and has a capped length.
     * @param first The first CharFilter to test.
     * @param second The second CharFilter to test.
     * @return A {@link CharFilter} which is effectively a combination of the 2 {@link CharFilter}s into one.
     * @throws NullPointerException When either of the input arguments are null.
     */
    static CharFilter combine(CharFilter first, CharFilter second) {
        Objects.requireNonNull(first, "CharFilter.combine cannot take null CharFilters.");
        Objects.requireNonNull(second, "CharFilter.combine cannot take null CharFilters.");

        return (t, c) -> first.filter(t, c) && second.filter(t, c);
    }

    boolean filter(String text, char c);
}
