package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a line pattern element that matches any single line of text.
 * <p>
 * Anyline elements are used to match arbitrary lines in documents, typically
 * for skipping unwanted content or capturing lines with unknown patterns.
 * Unlike Line elements, Anyline does not use regular expressions - it simply
 * matches any non-empty line.
 * <p>
 * Anyline elements can optionally bind the entire matched line to JSON properties
 * through PropertyBinding objects. When bindings are provided, the complete line
 * text is bound to each specified property (usually just one binding is used).
 * <p>
 * Common use cases include:
 * <ul>
 * <li>Skipping separator lines or whitespace</li>
 * <li>Capturing unknown content that needs to be preserved</li>
 * <li>Matching optional lines that don't have predictable patterns</li>
 * </ul>
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @param comment optional descriptive comment, may be null
 * @param bindProperties the property bindings for the matched line, never null but may be empty
 * @since 1.0
 * @see LineElement
 * @see PropertyBinding
 */
public record Anyline(
    String comment,
    List<PropertyBinding> bindProperties
) implements LineElement {

    /**
     * Creates an Anyline record with validation and immutable list creation.
     */
    public Anyline {
        Objects.requireNonNull(bindProperties, "bindProperties must not be null");
        
        // Validate no null bindings
        for (int i = 0; i < bindProperties.size(); i++) {
            if (bindProperties.get(i) == null) {
                throw new IllegalArgumentException("Property binding at index " + i + " must not be null");
            }
        }
        
        // Create immutable list
        bindProperties = List.copyOf(bindProperties);
    }

    /**
     * Creates a new Anyline with the specified property bindings.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindProperties the property bindings for the matched line, must not be null but may be empty
     * @throws IllegalArgumentException if bindProperties is null or contains null values
     */
    public Anyline(String comment, PropertyBinding... bindProperties) {
        this(comment, List.of(bindProperties));
    }

    /**
     * Creates a new Anyline with no property bindings.
     * <p>
     * This is commonly used for skipping lines without capturing their content.
     *
     * @param comment optional descriptive comment, may be null
     */
    public Anyline(String comment) {
        this(comment, Collections.emptyList());
    }

    /**
     * Returns the number of property bindings.
     *
     * @return the number of bindings, zero or greater
     */
    public int getBindingCount() {
        return bindProperties.size();
    }

    /**
     * Returns true if this anyline has property bindings.
     *
     * @return true if there are bindings, false if empty
     */
    public boolean hasBindings() {
        return !bindProperties.isEmpty();
    }

    @Override
    public void compile() {
        // Anyline doesn't need compilation - no-op but required by interface
        // This method is idempotent and can be called multiple times safely
    }

    @Override
    public LineMatchResult match(String inputLine) {
        Objects.requireNonNull(inputLine, "inputLine must not be null");
        
        // Anyline matches any non-empty line
        if (inputLine.trim().isEmpty()) {
            return LineMatchResult.failure();
        }
        
        // For Anyline, there are no regex capture groups, but the entire line
        // can be bound to properties if bindings are specified
        return LineMatchResult.success(inputLine, Collections.emptyList());
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitAnyline(this);
    }

    /**
     * Returns the property bindings that define how the matched line maps to JSON properties.
     * <p>
     * This is a convenience method equivalent to accessing the {@code bindProperties} component directly.
     *
     * @return the list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> getBindProperties() {
        return bindProperties;
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
     * Creates a new AnylineBuilder for fluent construction of Anyline instances.
     *
     * @return a new AnylineBuilder instance, never null
     */
    public static AnylineBuilder builder() {
        return new AnylineBuilder();
    }

    /**
     * A fluent builder for constructing Anyline instances.
     * <p>
     * This builder provides a fluent interface for creating Anyline pattern elements
     * with validation and type safety. The builder is mutable during construction
     * but produces immutable Anyline records.
     * <p>
     * Example usage:
     * <pre>{@code
     * Anyline anyline = Anyline.builder()
     *     .comment("Skip blank or unknown lines")
     *     .build();
     * 
     * // With binding (rare case)
     * Anyline withBinding = Anyline.builder()
     *     .comment("Capture unknown content")
     *     .bindProperties(PropertyBinding.of("skippedContent"))
     *     .build();
     * }</pre>
     */
    public static class AnylineBuilder {
        private String comment;
        private List<PropertyBinding> bindProperties = new ArrayList<>();

        /**
         * Sets the comment for this anyline pattern.
         *
         * @param comment optional descriptive comment, may be null
         * @return this builder for method chaining
         */
        public AnylineBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the property bindings for the matched line.
         *
         * @param bindings the property bindings for the matched line, must not be null
         * @return this builder for method chaining
         */
        public AnylineBuilder bindProperties(PropertyBinding... bindings) {
            this.bindProperties = List.of(bindings);
            return this;
        }

        /**
         * Sets the property bindings for the matched line.
         *
         * @param bindings the property bindings for the matched line, must not be null
         * @return this builder for method chaining
         */
        public AnylineBuilder bindProperties(List<PropertyBinding> bindings) {
            this.bindProperties = List.copyOf(bindings);
            return this;
        }

        /**
         * Builds and returns a new Anyline instance with the configured properties.
         *
         * @return a new Anyline instance, never null
         * @throws IllegalArgumentException if bindProperties contains null values
         */
        public Anyline build() {
            return new Anyline(comment, bindProperties);
        }
    }
}