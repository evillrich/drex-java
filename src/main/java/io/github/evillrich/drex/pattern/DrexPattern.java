package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the root of a document pattern that can extract structured JSON data.
 * <p>
 * DrexPattern is the top-level pattern element that defines a complete document
 * processing pattern. It contains metadata about the pattern (version, name) and
 * the root collection of pattern elements that define the matching logic.
 * <p>
 * DrexPattern instances are immutable and thread-safe once constructed.
 *
 * @param version the pattern version string, never null or empty
 * @param name the pattern name, never null or empty
 * @param comment optional descriptive comment, may be null
 * @param bindObject the name of the root JSON object, never null or empty
 * @param editDistance the maximum edit distance for fuzzy matching, never negative
 * @param elements the child pattern elements, never null but may be empty
 * @since 1.0
 * @see PatternElement
 */
public record DrexPattern(
    String version,
    String name,
    String comment,
    String bindObject,
    int editDistance,
    List<PatternElement> elements
) implements GroupingPatternElement {

    /**
     * Creates a DrexPattern record with validation and immutable list creation.
     */
    public DrexPattern {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version must not be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be null or empty");
        }
        if (bindObject == null || bindObject.trim().isEmpty()) {
            throw new IllegalArgumentException("bindObject must not be null or empty");
        }
        if (editDistance < 0) {
            throw new IllegalArgumentException("editDistance must be non-negative");
        }
        Objects.requireNonNull(elements, "elements must not be null");
        
        // Validate no null elements
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null) {
                throw new IllegalArgumentException("Element at index " + i + " must not be null");
            }
        }
        
        // Trim strings and create immutable list
        version = version.trim();
        name = name.trim();
        bindObject = bindObject.trim();
        elements = List.copyOf(elements);
    }

    /**
     * Creates a new DrexPattern with the specified properties.
     *
     * @param version the pattern version string, must not be null or empty
     * @param name the pattern name, must not be null or empty
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the root JSON object, must not be null or empty
     * @param editDistance the maximum edit distance for fuzzy matching, must be non-negative
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if version, name, or bindObject are null/empty,
     *                                  if editDistance is negative, or if elements contains null values
     */
    public DrexPattern(String version, String name, String comment, String bindObject, 
                       int editDistance, PatternElement... elements) {
        this(version, name, comment, bindObject, editDistance, List.of(elements));
    }

    /**
     * Returns true if fuzzy matching is enabled for this pattern.
     *
     * @return true if editDistance is greater than 0, false otherwise
     */
    public boolean isFuzzyMatchingEnabled() {
        return editDistance > 0;
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitDrexPattern(this);
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
     * Returns the name of the JSON object that this element creates.
     * <p>
     * This is a convenience method equivalent to accessing the {@code bindObject} component directly.
     *
     * @return the object binding name, never null or empty
     */
    @Override
    public String getBindObject() {
        return bindObject;
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
     * Returns the version string of this pattern.
     * <p>
     * This is a convenience method equivalent to accessing the {@code version} component directly.
     *
     * @return the version string, never null or empty
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the name of this pattern.
     * <p>
     * This is a convenience method equivalent to accessing the {@code name} component directly.
     *
     * @return the pattern name, never null or empty
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the maximum edit distance allowed for fuzzy matching.
     * <p>
     * This is a convenience method equivalent to accessing the {@code editDistance} component directly.
     *
     * @return the edit distance, zero or greater
     */
    public int getEditDistance() {
        return editDistance;
    }

    /**
     * Compiles all pattern elements contained in this DrexPattern to ensure
     * all regex patterns are valid and ready for matching.
     * <p>
     * This method delegates to each child element's compile method, ensuring
     * that all Line elements have their regex patterns compiled.
     */
    public void compile() {
        for (PatternElement element : elements) {
            element.compile();
        }
    }

    /**
     * Creates a new DrexPatternBuilder for fluent construction of DrexPattern instances.
     *
     * @return a new DrexPatternBuilder instance, never null
     */
    public static DrexPatternBuilder builder() {
        return new DrexPatternBuilder();
    }

    /**
     * A fluent builder for constructing DrexPattern instances.
     * <p>
     * This builder provides a fluent interface for creating DrexPattern instances
     * with validation and type safety. The builder is mutable during construction
     * but produces immutable DrexPattern records.
     * <p>
     * Example usage:
     * <pre>{@code
     * DrexPattern pattern = DrexPattern.builder()
     *     .version("1.0")
     *     .name("InvoicePattern")
     *     .comment("Extract basic invoice information")
     *     .bindObject("invoice")
     *     .editDistance(0)
     *     .elements(
     *         Line.builder().regex("Header (.*)").bindProperties(PropertyBinding.of("header")).build(),
     *         Group.builder().bindObject("details").elements(
     *             Line.builder().regex("Invoice #(\\d+)").bindProperties(PropertyBinding.of("id")).build()
     *         ).build()
     *     )
     *     .build();
     * }</pre>
     */
    public static class DrexPatternBuilder {
        private String version;
        private String name;
        private String comment;
        private String bindObject;
        private int editDistance = 0;
        private List<PatternElement> elements = new ArrayList<>();

        /**
         * Sets the version for this pattern.
         *
         * @param version the pattern version string, must not be null or empty
         * @return this builder for method chaining
         */
        public DrexPatternBuilder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the name for this pattern.
         *
         * @param name the pattern name, must not be null or empty
         * @return this builder for method chaining
         */
        public DrexPatternBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the comment for this pattern.
         *
         * @param comment optional descriptive comment, may be null
         * @return this builder for method chaining
         */
        public DrexPatternBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the name of the root JSON object that this pattern creates.
         *
         * @param bindObject the name of the root JSON object, must not be null or empty
         * @return this builder for method chaining
         */
        public DrexPatternBuilder bindObject(String bindObject) {
            this.bindObject = bindObject;
            return this;
        }

        /**
         * Sets the maximum edit distance for fuzzy matching.
         *
         * @param editDistance the maximum edit distance for fuzzy matching, must be non-negative
         * @return this builder for method chaining
         */
        public DrexPatternBuilder editDistance(int editDistance) {
            this.editDistance = editDistance;
            return this;
        }

        /**
         * Sets the child pattern elements for this pattern.
         *
         * @param elements the child pattern elements, must not be null
         * @return this builder for method chaining
         */
        public DrexPatternBuilder elements(PatternElement... elements) {
            this.elements = List.of(elements);
            return this;
        }

        /**
         * Sets the child pattern elements for this pattern.
         *
         * @param elements the child pattern elements, must not be null
         * @return this builder for method chaining
         */
        public DrexPatternBuilder elements(List<PatternElement> elements) {
            this.elements = List.copyOf(elements);
            return this;
        }

        /**
         * Builds and returns a new DrexPattern instance with the configured properties.
         *
         * @return a new DrexPattern instance, never null
         * @throws IllegalArgumentException if version, name, or bindObject are null/empty,
         *                                  if editDistance is negative, or if elements contains null values
         */
        public DrexPattern build() {
            return new DrexPattern(version, name, comment, bindObject, editDistance, elements);
        }
    }
}