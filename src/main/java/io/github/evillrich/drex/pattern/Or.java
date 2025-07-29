package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
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
 * @param comment optional descriptive comment, may be null
 * @param elements the alternative pattern elements, never null and never empty
 * @since 1.0
 * @see CompositePatternElement
 */
public record Or(
    String comment,
    List<PatternElement> elements
) implements CompositePatternElement {

    /**
     * Creates an Or record with validation and immutable list creation.
     */
    public Or {
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
        elements = List.copyOf(elements);
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
     * Returns the alternative pattern elements.
     * <p>
     * Alternatives are tried in order, and the first successful match is used.
     * This is a convenience method equivalent to accessing the {@code elements} component directly.
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
     * Creates a new OrBuilder for fluent construction of Or instances.
     *
     * @return a new OrBuilder instance, never null
     */
    public static OrBuilder builder() {
        return new OrBuilder();
    }

    /**
     * A fluent builder for constructing Or instances.
     * <p>
     * This builder provides a fluent interface for creating Or pattern elements
     * with validation and type safety. The builder is mutable during construction
     * but produces immutable Or records.
     * <p>
     * Example usage:
     * <pre>{@code
     * Or or = Or.builder()
     *     .comment("Handle different total formats")
     *     .elements(
     *         Line.builder().regex("Total: \\$([\\d,]+\\.\\d{2})").bindProperties(PropertyBinding.of("total")).build(),
     *         Line.builder().regex("Amount Due: \\$([\\d,]+\\.\\d{2})").bindProperties(PropertyBinding.of("total")).build(),
     *         Line.builder().regex("Final Total: ([\\d,]+\\.\\d{2})").bindProperties(PropertyBinding.of("total")).build()
     *     )
     *     .build();
     * }</pre>
     */
    public static class OrBuilder {
        private String comment;
        private List<PatternElement> elements = new ArrayList<>();

        /**
         * Sets the comment for this or pattern.
         *
         * @param comment optional descriptive comment, may be null
         * @return this builder for method chaining
         */
        public OrBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the alternative pattern elements.
         *
         * @param elements the alternative pattern elements, must not be null and should contain at least one element
         * @return this builder for method chaining
         */
        public OrBuilder elements(PatternElement... elements) {
            this.elements = List.of(elements);
            return this;
        }

        /**
         * Sets the alternative pattern elements.
         *
         * @param elements the alternative pattern elements, must not be null and should contain at least one element
         * @return this builder for method chaining
         */
        public OrBuilder elements(List<PatternElement> elements) {
            this.elements = List.copyOf(elements);
            return this;
        }

        /**
         * Builds and returns a new Or instance with the configured properties.
         *
         * @return a new Or instance, never null
         * @throws IllegalArgumentException if elements is null or contains null values
         */
        public Or build() {
            return new Or(comment, elements);
        }
    }
}