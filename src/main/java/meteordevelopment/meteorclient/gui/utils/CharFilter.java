/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface CharFilter {

    CharFilter NONE = (t, c) -> true;

    CharFilter ALPHANUMERIC_LENIENT = alphanumeric(true, true, true, true);
    CharFilter ALPHANUMERIC_STRICT = alphanumeric(false, false, false, false);

    CharFilter NUMERIC = (t, c) -> Character.isDigit(c);

    CharFilter NO_SPACES = (t, c) -> !Character.isWhitespace(c);


    /**
     * Returns the default {@link CharFilter} instance, which returns true always; effectively no filter.
     * @return A {@link CharFilter} that always returns {@code true}.
     */
    static CharFilter none() {
        return NONE;
    }

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
            : NONE;
    }

    /**
     * {@link CharFilter} for only alphanumeric characters, with leniency for periods, hyphens, underscores, and spaces.
     * @return A lenient alphanumeric {@link CharFilter}.
     */
    static CharFilter alphanumericLenient() {
        return ALPHANUMERIC_LENIENT;
    }

    /**
     * {@link CharFilter} for only alphanumeric characters.
     * @return A strictly alphanumeric {@link CharFilter}.
     */
    static CharFilter alphanumericStrict() {
        return ALPHANUMERIC_STRICT;
    }

    /**
     * {@link CharFilter} for only numeric characters.
     * @return A strictly numeric {@link CharFilter}.
     */
    static CharFilter numeric() {
        return NUMERIC;
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
     * A {@link CharFilter} that only prevents whitespace.
     * @return A space-less {@link CharFilter}.
     */
    static CharFilter noSpaces() {
        return NO_SPACES;
    }

    /**
     * A {@link CharFilter} that accepts only IP addresses/hostnames.
     * @return An IP/hostname {@link CharFilter}.
     */
    static CharFilter ip() {
        CharFilter hostnamesAndIps = alphanumeric(true, true, true, false);
        return (t, c) -> (!t.contains(":") || c != ':') && hostnamesAndIps.filter(t, c);
    }

    /**
     * A {@link CharFilter} that ensures the input content is at most length long.
     * @return A length-capping {@link CharFilter}.
     */
    static CharFilter noLongerThan(int length) {
        return (t, c) -> t.length() <= length;
    }

    /**
     * A {@link CharFilter} combinator, allowing you to more easily combine the conditions of 2 different {@link CharFilter} together into one {@link CharFilter}.
     * The resulting {@link CharFilter} is equivalent to ANDing the results of the provided {@link CharFilter}s.
     * <br/><br/>
     * @param first The first {@link CharFilter} to test.
     * @param second The second {@link CharFilter} to test.
     * @return A {@link CharFilter} which ANDs the two {@link CharFilter}s into one.
     * @throws NullPointerException When either of the input arguments are null.
     */
    static CharFilter combine(@NotNull CharFilter first, @NotNull CharFilter second) {
        Objects.requireNonNull(first, "cannot combine null CharFilters");
        Objects.requireNonNull(second, "cannot combine null CharFilters");

        return (t, c) -> first.filter(t, c) && second.filter(t, c);
    }

    /**
     * A {@link CharFilter} combinator, allowing you to more easily combine the conditions of 2 different {@link CharFilter} together into one {@link CharFilter}.
     * The resulting {@link CharFilter} is equivalent to ORing the results of the provided {@link CharFilter}s.
     * <br/><br/>
     * @param first The first {@link CharFilter} to test.
     * @param second The second {@link CharFilter} to test.
     * @return A {@link CharFilter} which ORs the two {@link CharFilter}s into one.
     * @throws NullPointerException When either of the input arguments are null.
     */
    static CharFilter or(@NotNull CharFilter first, @NotNull CharFilter second) {
        Objects.requireNonNull(first, "cannot combine null CharFilters");
        Objects.requireNonNull(second, "cannot combine null CharFilters");

        return (t, c) -> first.filter(t, c) || second.filter(t, c);
    }

    boolean filter(String text, char c);

    default CharFilter and(@NotNull CharFilter second) {
        return combine(this, second);
    }

    default CharFilter or(@NotNull CharFilter second) {
        return or(this, second);
    }
}
