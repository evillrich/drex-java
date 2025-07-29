package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.Objects;

/**
 * Represents an or pattern element that tries alternative matching paths.
 * <p>
 * Or elements provide multiple alternative patterns and use the first one that
 * successfully matches. This is useful for handling documents with variable
 * formats or optional sections that might appear in different forms.
 * <p>
 * Or elements do not create their own binding context - they simply pass through
 * the binding context from whichever alternative successfully matches. This means
 * the alternatives should be designed to bind their data consistently.
 * <p>
 * The Drex engine tries alternatives in order and uses the first successful match,
 * following a greedy, non-backtracking approach. Therefore, more specific patterns
 * should be placed before more general ones.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see CompositePatternElement
 */
public final class Or extends CompositePatternElement {

    private final String comment;
    private final List<PatternElement> elements;

    /**
     * Creates an Or with validation and immutable list creation.
     *
     * @param comment optional descriptive comment, may be null
     * @param elements the alternative pattern elements, never null and never empty
     * @throws IllegalArgumentException if elements is null, empty, or contains null values
     */
    public Or(String comment, List<PatternElement> elements) {
        Objects.requireNonNull(elements, "elements must not be null");
        
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Or element must have at least one alternative");
        }
        
        // Validate no null elements
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null) {
                throw new IllegalArgumentException("Element at index " + i + " must not be null");
            }
        }
        
        // Create immutable list
        this.comment = comment;
        this.elements = List.copyOf(elements);
    }

    /**
     * Creates a new Or with the specified alternative elements.
     *
     * @param comment optional descriptive comment, may be null
     * @param elements the alternative pattern elements, must not be null and should contain at least one element
     * @throws IllegalArgumentException if elements is null or contains null values
     */
    public Or(String comment, PatternElement... elements) {
        this(comment, List.of(elements));
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
     * Returns the alternative pattern elements.
     *
     * @return an immutable list of alternative elements, never null or empty
     */
    public List<PatternElement> elements() {
        return elements;
    }

    /**
     * Returns the alternative pattern elements.
     * <p>
     * Alternatives are tried in order, and the first successful match is used.
     *
     * @return the list of alternative elements, never null or empty
     */
    public List<PatternElement> getAlternatives() {
        return elements;
    }

    /**
     * Returns the number of alternative patterns.
     *
     * @return the number of alternatives, always greater than zero
     */
    public int getAlternativeCount() {
        return elements.size();
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitOr(this);
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
     * @return a new Or instance for fluent construction
     */
    public static Or builder() {
        return new Or(null, List.of());
    }

    /**
     * Sets the comment for this or pattern.
     *
     * @param comment optional descriptive comment, may be null
     * @return a new Or instance for method chaining
     */
    public Or comment(String comment) {
        return new Or(comment, this.elements);
    }

    /**
     * Sets the alternative pattern elements for this or.
     *
     * @param elements the alternative pattern elements, must not be null
     * @return a new Or instance for method chaining
     */
    public Or elements(PatternElement... elements) {
        return new Or(this.comment, List.of(elements));
    }

    /**
     * Sets the alternative pattern elements for this or.
     *
     * @param elements the alternative pattern elements, must not be null
     * @return a new Or instance for method chaining
     */
    public Or elements(List<PatternElement> elements) {
        return new Or(this.comment, elements);
    }

    /**
     * Completes the fluent construction.
     *
     * @return this Or instance
     */
    public Or build() {
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
        Or or = (Or) obj;
        return Objects.equals(comment, or.comment) &&
               Objects.equals(elements, or.elements);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(comment, elements);
    }

    /**
     * Returns a string representation of this Or.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Or[" +
               (comment != null ? "comment=" + comment + ", " : "") +
               "elements=" + elements.size() + " alternatives" +
               "]";
    }
}