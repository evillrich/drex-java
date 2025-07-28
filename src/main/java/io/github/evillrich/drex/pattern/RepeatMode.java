package io.github.evillrich.drex.pattern;

/**
 * Enumeration of repeat modes for {@link Repeat} pattern elements.
 * <p>
 * RepeatMode defines how many times a repeat pattern should attempt to match
 * its child elements. The Drex engine uses a greedy, non-backtracking algorithm,
 * so repeat modes determine the initial matching strategy.
 *
 * @since 1.0
 * @see Repeat
 */
public enum RepeatMode {

    /**
     * Match zero or more times.
     * <p>
     * The pattern will attempt to match as many times as possible, but will
     * succeed even if no matches are found. This is equivalent to the regex
     * quantifier {@code *}.
     */
    ZERO_OR_MORE,

    /**
     * Match one or more times.
     * <p>
     * The pattern must match at least once to succeed, but will attempt to
     * match as many times as possible. This is equivalent to the regex
     * quantifier {@code +}.
     */
    ONE_OR_MORE,

    /**
     * Match zero or one time.
     * <p>
     * The pattern will attempt to match once, but will succeed even if no
     * match is found. This is equivalent to the regex quantifier {@code ?}.
     */
    ZERO_OR_ONE;

    /**
     * Returns true if this repeat mode requires at least one successful match.
     *
     * @return true for ONE_OR_MORE, false for ZERO_OR_MORE and ZERO_OR_ONE
     */
    public boolean requiresMatch() {
        return this == ONE_OR_MORE;
    }

    /**
     * Returns true if this repeat mode allows multiple matches.
     *
     * @return true for ZERO_OR_MORE and ONE_OR_MORE, false for ZERO_OR_ONE
     */
    public boolean allowsMultiple() {
        return this == ZERO_OR_MORE || this == ONE_OR_MORE;
    }

    /**
     * Returns the maximum number of matches allowed by this repeat mode.
     *
     * @return Integer.MAX_VALUE for modes that allow multiple matches, 1 for ZERO_OR_ONE
     */
    public int getMaxMatches() {
        return allowsMultiple() ? Integer.MAX_VALUE : 1;
    }

    /**
     * Returns the minimum number of matches required by this repeat mode.
     *
     * @return 1 for ONE_OR_MORE, 0 for ZERO_OR_MORE and ZERO_OR_ONE
     */
    public int getMinMatches() {
        return requiresMatch() ? 1 : 0;
    }
}