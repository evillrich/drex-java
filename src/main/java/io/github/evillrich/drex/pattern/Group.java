package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.Objects;

/**
 * Represents a group pattern element that creates nested JSON objects.
 * <p>
 * Group elements are used to create structured JSON objects within the output
 * hierarchy. They bind their child elements' extracted data to a named JSON
 * object, allowing for complex nested data structures.
 * <p>
 * Groups are commonly used to organize related data fields, such as collecting
 * address information into an "address" object or contact details into a
 * "contact" object.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see GroupingPatternElement
 * @see Builder
 */
public final class Group extends GroupingPatternElement {

    /**
     * Constructs a new Group with the specified properties.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the JSON object to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if bindObject is null/empty or elements contains null values
     */
    public Group(String comment, String bindObject, List<PatternElement> elements) {
        super(comment, bindObject, elements);
    }

    /**
     * Constructs a new Group with the specified properties.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the JSON object to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if bindObject is null/empty or elements contains null values
     */
    public Group(String comment, String bindObject, PatternElement... elements) {
        super(comment, bindObject, elements);
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitGroup(this);
    }

    /**
     * Creates a new builder for constructing Group instances.
     *
     * @return a new Builder instance, never null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing Group instances using a fluent API.
     */
    public static final class Builder {
        private String comment;
        private String bindObject;
        private List<PatternElement> elements;

        private Builder() {}

        /**
         * Sets an optional comment describing this group.
         *
         * @param comment the comment text, may be null
         * @return this builder for method chaining
         */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the object binding name for this group.
         *
         * @param bindObject the JSON object name, must not be null or empty
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindObject is null or empty
         */
        public Builder bindObject(String bindObject) {
            if (bindObject == null || bindObject.trim().isEmpty()) {
                throw new IllegalArgumentException("bindObject must not be null or empty");
            }
            this.bindObject = bindObject.trim();
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
         * Constructs a new Group with the configured properties.
         *
         * @return a new Group instance, never null
         * @throws IllegalStateException if required properties are not set
         */
        public Group build() {
            if (bindObject == null) {
                throw new IllegalStateException("bindObject is required");
            }
            if (elements == null) {
                throw new IllegalStateException("elements is required");
            }

            return new Group(comment, bindObject, elements);
        }
    }
}