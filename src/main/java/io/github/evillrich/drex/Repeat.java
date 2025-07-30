package io.github.evillrich.drex;

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
 * @see Mode
 * @see CompositePatternElement
 */
public final class Repeat extends CompositePatternElement {

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

    private final String comment;
    private final Mode mode;
    private final String bindArray;
    private final List<PatternElement> elements;

    /**
     * Creates a Repeat with validation and immutable list creation.
     *
     * @param comment optional descriptive comment, may be null
     * @param mode the repeat mode determining matching behavior, never null
     * @param bindArray the name of the JSON array to create, never null or empty
     * @param elements the child pattern elements, never null but may be empty
     * @throws IllegalArgumentException if mode is null, bindArray is null/empty, 
     *                                  or elements contains null values
     */
    public Repeat(String comment, Mode mode, String bindArray, List<PatternElement> elements) {
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
        this.comment = comment;
        this.mode = mode;
        this.bindArray = bindArray.trim();
        this.elements = List.copyOf(elements);
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
     * Returns the optional comment.
     *
     * @return the comment string, or null if no comment was provided
     */
    public String comment() {
        return comment;
    }

    /**
     * Returns the repeat mode.
     *
     * @return the repeat mode, never null
     */
    public Mode mode() {
        return mode;
    }

    /**
     * Returns the name of the JSON array to create.
     *
     * @return the array binding name, never null or empty
     */
    public String bindArray() {
        return bindArray;
    }

    /**
     * Returns the child pattern elements.
     *
     * @return an immutable list of child elements, never null but may be empty
     */
    public List<PatternElement> elements() {
        return elements;
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
     *
     * @return an unmodifiable list of child elements, never null but may be empty
     */
    @Override
    public List<PatternElement> getElements() {
        return elements;
    }

    /**
     * Returns the repeat mode that determines matching behavior.
     *
     * @return the repeat mode, never null
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the name of the JSON array that will contain the matched elements.
     *
     * @return the array binding name, never null or empty
     */
    public String getBindArray() {
        return bindArray;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     *
     * @return the comment string, or null if no comment was provided
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * Creates a builder-style pattern construction (backward compatibility).
     *
     * @return a new Repeat instance for fluent construction
     */
    public static Repeat builder() {
        return new Repeat(null, Mode.ZERO_OR_MORE, "", List.of());
    }

    /**
     * Sets the comment for this repeat pattern.
     *
     * @param comment optional descriptive comment, may be null
     * @return a new Repeat instance for method chaining
     */
    public Repeat comment(String comment) {
        return new Repeat(comment, this.mode, this.bindArray, this.elements);
    }

    /**
     * Sets the repeat mode that determines matching behavior.
     *
     * @param mode the repeat mode determining matching behavior, must not be null
     * @return a new Repeat instance for method chaining
     */
    public Repeat mode(Mode mode) {
        return new Repeat(this.comment, mode, this.bindArray, this.elements);
    }

    /**
     * Sets the name of the JSON array that will contain the matched elements.
     *
     * @param bindArray the name of the JSON array to create, must not be null or empty
     * @return a new Repeat instance for method chaining
     */
    public Repeat bindArray(String bindArray) {
        return new Repeat(this.comment, this.mode, bindArray, this.elements);
    }

    /**
     * Sets the child pattern elements for this repeat.
     *
     * @param elements the child pattern elements, must not be null
     * @return a new Repeat instance for method chaining
     */
    public Repeat elements(PatternElement... elements) {
        return new Repeat(this.comment, this.mode, this.bindArray, List.of(elements));
    }

    /**
     * Sets the child pattern elements for this repeat.
     *
     * @param elements the child pattern elements, must not be null
     * @return a new Repeat instance for method chaining
     */
    public Repeat elements(List<PatternElement> elements) {
        return new Repeat(this.comment, this.mode, this.bindArray, elements);
    }

    /**
     * Completes the fluent construction.
     *
     * @return this Repeat instance
     */
    public Repeat build() {
        return this;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Repeat repeat = (Repeat) obj;
        return Objects.equals(comment, repeat.comment) &&
               mode == repeat.mode &&
               Objects.equals(bindArray, repeat.bindArray) &&
               Objects.equals(elements, repeat.elements);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(comment, mode, bindArray, elements);
    }

    /**
     * Returns a string representation of this Repeat.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Repeat[" +
               (comment != null ? "comment=" + comment + ", " : "") +
               "mode=" + mode +
               ", bindArray=" + bindArray +
               ", elements=" + elements.size() + " elements" +
               "]";
    }
}