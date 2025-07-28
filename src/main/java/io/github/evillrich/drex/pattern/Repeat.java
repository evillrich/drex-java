package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.Objects;

/**
 * Represents a repeat pattern element that matches its children multiple times.
 * <p>
 * Repeat elements are used to process repetitive document structures, such as
 * lists of line items, multiple addresses, or repeated data blocks. The repeat
 * mode determines how many matches are required and allowed.
 * <p>
 * Repeat elements bind their matches to JSON arrays, with each iteration creating
 * a new object in the array containing the extracted data from the child elements.
 * <p>
 * The Drex engine uses a greedy matching algorithm, so repeats will consume as
 * many matches as possible according to their mode.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see RepeatMode
 * @see CompositePatternElement
 * @see Builder
 */
public final class Repeat extends CompositePatternElement {

    /**
     * Enumeration of repeat modes for {@link Repeat} pattern elements.
     * <p>
     * RepeatMode defines how many times a repeat pattern should attempt to match
     * its child elements. The Drex engine uses a greedy, non-backtracking algorithm,
     * so repeat modes determine the initial matching strategy.
     *
     * @since 1.0
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

    private final RepeatMode mode;
    private final String bindArray;

    /**
     * Constructs a new Repeat with the specified properties.
     *
     * @param comment optional descriptive comment, may be null
     * @param mode the repeat mode determining matching behavior, must not be null
     * @param bindArray the name of the JSON array to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if mode is null, bindArray is null/empty, 
     *                                  or elements contains null values
     */
    public Repeat(String comment, RepeatMode mode, String bindArray, List<PatternElement> elements) {
        super(comment, elements);
        
        Objects.requireNonNull(mode, "mode must not be null");
        if (bindArray == null || bindArray.trim().isEmpty()) {
            throw new IllegalArgumentException("bindArray must not be null or empty");
        }
        
        this.mode = mode;
        this.bindArray = bindArray.trim();
    }

    /**
     * Constructs a new Repeat with the specified properties.
     *
     * @param comment optional descriptive comment, may be null
     * @param mode the repeat mode determining matching behavior, must not be null
     * @param bindArray the name of the JSON array to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if mode is null, bindArray is null/empty, 
     *                                  or elements contains null values
     */
    public Repeat(String comment, RepeatMode mode, String bindArray, PatternElement... elements) {
        super(comment, elements);
        
        Objects.requireNonNull(mode, "mode must not be null");
        if (bindArray == null || bindArray.trim().isEmpty()) {
            throw new IllegalArgumentException("bindArray must not be null or empty");
        }
        
        this.mode = mode;
        this.bindArray = bindArray.trim();
    }

    /**
     * Returns the repeat mode that determines matching behavior.
     * <p>
     * The repeat mode specifies how many times the child elements should be
     * matched and whether matches are required or optional.
     *
     * @return the repeat mode, never null
     */
    public RepeatMode getMode() {
        return mode;
    }

    /**
     * Returns the name of the JSON array that will contain the matched elements.
     * <p>
     * Each successful iteration of the repeat creates a new object in this array,
     * populated with data extracted by the child pattern elements.
     *
     * @return the array binding name, never null or empty
     */
    public String getBindArray() {
        return bindArray;
    }

    /**
     * Returns true if this repeat requires at least one successful match.
     *
     * @return true for ONE_OR_MORE mode, false otherwise
     */
    public boolean requiresMatch() {
        return mode.requiresMatch();
    }

    /**
     * Returns true if this repeat allows multiple matches.
     *
     * @return true for ZERO_OR_MORE and ONE_OR_MORE modes, false for ZERO_OR_ONE
     */
    public boolean allowsMultiple() {
        return mode.allowsMultiple();
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitRepeat(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        Repeat repeat = (Repeat) obj;
        return mode == repeat.mode && 
               Objects.equals(bindArray, repeat.bindArray);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mode, bindArray);
    }

    @Override
    public String toString() {
        return "Repeat[" +
               "mode=" + mode + 
               ", bindArray=" + bindArray +
               ", elements=" + getElementCount() +
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }

    /**
     * Creates a new builder for constructing Repeat instances.
     *
     * @return a new Builder instance, never null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing Repeat instances using a fluent API.
     */
    public static final class Builder {
        private String comment;
        private RepeatMode mode;
        private String bindArray;
        private List<PatternElement> elements;

        private Builder() {}

        /**
         * Sets an optional comment describing this repeat.
         *
         * @param comment the comment text, may be null
         * @return this builder for method chaining
         */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the repeat mode.
         *
         * @param mode the repeat mode, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if mode is null
         */
        public Builder mode(RepeatMode mode) {
            Objects.requireNonNull(mode, "mode must not be null");
            this.mode = mode;
            return this;
        }

        /**
         * Sets the array binding name.
         *
         * @param bindArray the JSON array name, must not be null or empty
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindArray is null or empty
         */
        public Builder bindArray(String bindArray) {
            if (bindArray == null || bindArray.trim().isEmpty()) {
                throw new IllegalArgumentException("bindArray must not be null or empty");
            }
            this.bindArray = bindArray.trim();
            return this;
        }

        /**
         * Sets the child pattern elements.
         *
         * @param elements the pattern elements, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if elements is null
         */
        public Builder elements(List<PatternElement> elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            this.elements = elements;
            return this;
        }

        /**
         * Sets the child pattern elements.
         *
         * @param elements the pattern elements, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if elements is null
         */
        public Builder elements(PatternElement... elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            this.elements = List.of(elements);
            return this;
        }

        /**
         * Constructs a new Repeat with the configured properties.
         *
         * @return a new Repeat instance, never null
         * @throws IllegalStateException if required properties are not set
         */
        public Repeat build() {
            if (mode == null) {
                throw new IllegalStateException("mode is required");
            }
            if (bindArray == null) {
                throw new IllegalStateException("bindArray is required");
            }
            if (elements == null) {
                throw new IllegalStateException("elements is required");
            }

            return new Repeat(comment, mode, bindArray, elements);
        }
    }
}