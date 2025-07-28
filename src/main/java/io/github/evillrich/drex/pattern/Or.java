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
 * @see Builder
 */
public final class Or extends CompositePatternElement {

    /**
     * Constructs a new Or with the specified alternative elements.
     *
     * @param comment optional descriptive comment, may be null
     * @param elements the alternative pattern elements, must not be null and should contain at least one element
     * @throws IllegalArgumentException if elements is null or contains null values
     */
    public Or(String comment, List<PatternElement> elements) {
        super(comment, elements);
        
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Or element must have at least one alternative");
        }
    }

    /**
     * Constructs a new Or with the specified alternative elements.
     *
     * @param comment optional descriptive comment, may be null
     * @param elements the alternative pattern elements, must not be null and should contain at least one element
     * @throws IllegalArgumentException if elements is null or contains null values
     */
    public Or(String comment, PatternElement... elements) {
        super(comment, elements);
        
        if (elements.length == 0) {
            throw new IllegalArgumentException("Or element must have at least one alternative");
        }
    }

    /**
     * Returns the alternative pattern elements.
     * <p>
     * Alternatives are tried in order, and the first successful match is used.
     * The returned list is immutable.
     *
     * @return the list of alternative elements, never null or empty
     */
    public List<PatternElement> getAlternatives() {
        return getElements();
    }

    /**
     * Returns the number of alternative patterns.
     *
     * @return the number of alternatives, always greater than zero
     */
    public int getAlternativeCount() {
        return getElementCount();
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitOr(this);
    }

    @Override
    public String toString() {
        return "Or[" +
               "alternatives=" + getAlternativeCount() +
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }

    /**
     * Creates a new builder for constructing Or instances.
     *
     * @return a new Builder instance, never null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing Or instances using a fluent API.
     */
    public static final class Builder {
        private String comment;
        private List<PatternElement> elements;

        private Builder() {}

        /**
         * Sets an optional comment describing this or element.
         *
         * @param comment the comment text, may be null
         * @return this builder for method chaining
         */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the alternative pattern elements.
         *
         * @param elements the pattern elements, must not be null and should contain at least one element
         * @return this builder for method chaining
         * @throws IllegalArgumentException if elements is null
         */
        public Builder elements(List<PatternElement> elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            this.elements = elements;
            return this;
        }

        /**
         * Sets the alternative pattern elements.
         *
         * @param elements the pattern elements, must not be null and should contain at least one element
         * @return this builder for method chaining
         * @throws IllegalArgumentException if elements is null
         */
        public Builder elements(PatternElement... elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            this.elements = List.of(elements);
            return this;
        }

        /**
         * Constructs a new Or with the configured properties.
         *
         * @return a new Or instance, never null
         * @throws IllegalStateException if required properties are not set
         * @throws IllegalArgumentException if elements is empty
         */
        public Or build() {
            if (elements == null) {
                throw new IllegalStateException("elements is required");
            }

            return new Or(comment, elements);
        }
    }
}