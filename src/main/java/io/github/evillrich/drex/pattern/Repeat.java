package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Collections;
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
 * @param comment optional descriptive comment, may be null
 * @param mode the repeat mode determining matching behavior, never null
 * @param bindArray the name of the JSON array to create, never null or empty
 * @param elements the child pattern elements, never null but may be empty
 * @since 1.0
 * @see Mode
 * @see CompositePatternElement
 */
public record Repeat(
    String comment,
    Mode mode,
    String bindArray,
    List<PatternElement> elements
) implements CompositePatternElement {

    /**
     * Enumeration of repeat modes for {@link Repeat} pattern elements.
     * <p>
     * Mode defines how many times a repeat pattern should attempt to match
     * its child elements. The Drex engine uses a greedy, non-backtracking algorithm,
     * so repeat modes determine the initial matching strategy.
     *
     * @since 1.0
     */
    public enum Mode {

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

    /**
     * Creates a Repeat record with validation and immutable list creation.
     */
    public Repeat {
        Objects.requireNonNull(mode, "mode must not be null");
        if (bindArray == null || bindArray.trim().isEmpty()) {
            throw new IllegalArgumentException("bindArray must not be null or empty");
        }
        Objects.requireNonNull(elements, "elements must not be null");
        
        // Validate no null elements
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null) {
                throw new IllegalArgumentException("Element at index " + i + " must not be null");
            }
        }
        
        // Trim bindArray and create immutable list
        bindArray = bindArray.trim();
        elements = List.copyOf(elements);
    }

    /**
     * Creates a new Repeat with the specified properties.
     *
     * @param comment optional descriptive comment, may be null
     * @param mode the repeat mode determining matching behavior, must not be null
     * @param bindArray the name of the JSON array to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if mode is null, bindArray is null/empty, 
     *                                  or elements contains null values
     */
    public Repeat(String comment, Mode mode, String bindArray, PatternElement... elements) {
        this(comment, mode, bindArray, List.of(elements));
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

    /**
     * Returns an immutable list of child pattern elements.
     * <p>
     * This is a convenience method equivalent to accessing the {@code elements} component directly.
     *
     * @return an unmodifiable list of child elements, never null but may be empty
     */
    @Override
    public List<PatternElement> getElements() {
        return elements;
    }

    /**
     * Returns the repeat mode that determines matching behavior.
     * <p>
     * This is a convenience method equivalent to accessing the {@code mode} component directly.
     *
     * @return the repeat mode, never null
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the name of the JSON array that will contain the matched elements.
     * <p>
     * This is a convenience method equivalent to accessing the {@code bindArray} component directly.
     *
     * @return the array binding name, never null or empty
     */
    public String getBindArray() {
        return bindArray;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     * <p>
     * This is a convenience method equivalent to accessing the {@code comment} component directly.
     *
     * @return the comment string, or null if no comment was provided
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * Creates a new RepeatBuilder for fluent construction of Repeat instances.
     *
     * @return a new RepeatBuilder instance, never null
     */
    public static RepeatBuilder builder() {
        return new RepeatBuilder();
    }

    /**
     * A fluent builder for constructing Repeat instances.
     * <p>
     * This builder provides a fluent interface for creating Repeat pattern elements
     * with validation and type safety. The builder is mutable during construction
     * but produces immutable Repeat records.
     * <p>
     * Example usage:
     * <pre>{@code
     * Repeat repeat = Repeat.builder()
     *     .comment("Process line items")
     *     .mode(Repeat.Mode.ONE_OR_MORE)
     *     .bindArray("items")
     *     .elements(
     *         Line.builder().regex("(.+)\\s+(\\d+)\\s+([\\d.]+)").bindProperties(
     *             PropertyBinding.of("name"),
     *             PropertyBinding.of("quantity"),
     *             PropertyBinding.of("price")
     *         ).build()
     *     )
     *     .build();
     * }</pre>
     */
    public static class RepeatBuilder {
        private String comment;
        private Mode mode;
        private String bindArray;
        private List<PatternElement> elements = new ArrayList<>();

        /**
         * Sets the comment for this repeat pattern.
         *
         * @param comment optional descriptive comment, may be null
         * @return this builder for method chaining
         */
        public RepeatBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the repeat mode that determines matching behavior.
         *
         * @param mode the repeat mode determining matching behavior, must not be null
         * @return this builder for method chaining
         */
        public RepeatBuilder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the name of the JSON array that will contain the matched elements.
         *
         * @param bindArray the name of the JSON array to create, must not be null or empty
         * @return this builder for method chaining
         */
        public RepeatBuilder bindArray(String bindArray) {
            this.bindArray = bindArray;
            return this;
        }

        /**
         * Sets the child pattern elements for this repeat.
         *
         * @param elements the child pattern elements, must not be null
         * @return this builder for method chaining
         */
        public RepeatBuilder elements(PatternElement... elements) {
            this.elements = List.of(elements);
            return this;
        }

        /**
         * Sets the child pattern elements for this repeat.
         *
         * @param elements the child pattern elements, must not be null
         * @return this builder for method chaining
         */
        public RepeatBuilder elements(List<PatternElement> elements) {
            this.elements = List.copyOf(elements);
            return this;
        }

        /**
         * Builds and returns a new Repeat instance with the configured properties.
         *
         * @return a new Repeat instance, never null
         * @throws IllegalArgumentException if mode is null, bindArray is null/empty, 
         *                                  or elements contains null values
         */
        public Repeat build() {
            return new Repeat(comment, mode, bindArray, elements);
        }
    }
}