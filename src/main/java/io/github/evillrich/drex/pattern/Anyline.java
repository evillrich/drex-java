package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Arrays;
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
 * @since 1.0
 * @see LineElement
 * @see PropertyBinding
 * @see Builder
 */
public final class Anyline extends LineElement {

    private final List<PropertyBinding> bindProperties;

    /**
     * Constructs a new Anyline with the specified property bindings.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindProperties the property bindings for the matched line, must not be null but may be empty
     * @throws IllegalArgumentException if bindProperties is null or contains null values
     */
    public Anyline(String comment, List<PropertyBinding> bindProperties) {
        super(comment);
        
        Objects.requireNonNull(bindProperties, "bindProperties must not be null");
        
        // Validate no null bindings
        for (int i = 0; i < bindProperties.size(); i++) {
            if (bindProperties.get(i) == null) {
                throw new IllegalArgumentException("Property binding at index " + i + " must not be null");
            }
        }
        
        this.bindProperties = Collections.unmodifiableList(new ArrayList<>(bindProperties));
    }

    /**
     * Constructs a new Anyline with the specified property bindings.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindProperties the property bindings for the matched line, must not be null but may be empty
     * @throws IllegalArgumentException if bindProperties is null or contains null values
     */
    public Anyline(String comment, PropertyBinding... bindProperties) {
        this(comment, Arrays.asList(bindProperties));
    }

    /**
     * Constructs a new Anyline with no property bindings.
     * <p>
     * This is commonly used for skipping lines without capturing their content.
     *
     * @param comment optional descriptive comment, may be null
     */
    public Anyline(String comment) {
        this(comment, Collections.emptyList());
    }

    /**
     * Returns the property bindings that define how the matched line maps to JSON properties.
     * <p>
     * The returned list is immutable and contains no null elements. When bindings
     * are present, the entire matched line text is bound to each specified property.
     *
     * @return the list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> getBindProperties() {
        return bindProperties;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        Anyline anyline = (Anyline) obj;
        return Objects.equals(bindProperties, anyline.bindProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bindProperties);
    }

    @Override
    public String toString() {
        return "Anyline[" +
               "bindings=" + bindProperties.size() +
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }

    /**
     * Creates a new builder for constructing Anyline instances.
     *
     * @return a new Builder instance, never null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing Anyline instances using a fluent API.
     */
    public static final class Builder {
        private String comment;
        private List<PropertyBinding> bindProperties;

        private Builder() {}

        /**
         * Sets an optional comment describing this anyline.
         *
         * @param comment the comment text, may be null
         * @return this builder for method chaining
         */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the property bindings.
         *
         * @param bindProperties the property bindings, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindProperties is null
         */
        public Builder bindProperties(List<PropertyBinding> bindProperties) {
            Objects.requireNonNull(bindProperties, "bindProperties must not be null");
            this.bindProperties = bindProperties;
            return this;
        }

        /**
         * Sets the property bindings.
         *
         * @param bindProperties the property bindings, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindProperties is null
         */
        public Builder bindProperties(PropertyBinding... bindProperties) {
            Objects.requireNonNull(bindProperties, "bindProperties must not be null");
            this.bindProperties = Arrays.asList(bindProperties);
            return this;
        }

        /**
         * Constructs a new Anyline with the configured properties.
         *
         * @return a new Anyline instance, never null
         */
        public Anyline build() {
            if (bindProperties == null) {
                bindProperties = Collections.emptyList();
            }

            return new Anyline(comment, bindProperties);
        }
    }
}