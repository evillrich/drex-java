package io.github.evillrich.drex.pattern;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
 */
public final class Anyline extends LineElement {

    private final String comment;
    private final List<PropertyBinding> bindProperties;

    /**
     * Creates an Anyline with validation and immutable list creation.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindProperties the property bindings for the matched line, never null but may be empty
     * @throws IllegalArgumentException if bindProperties is null or contains null values
     */
    public Anyline(String comment, List<PropertyBinding> bindProperties) {
        Objects.requireNonNull(bindProperties, "bindProperties must not be null");
        
        // Validate no null bindings
        for (int i = 0; i < bindProperties.size(); i++) {
            if (bindProperties.get(i) == null) {
                throw new IllegalArgumentException("Property binding at index " + i + " must not be null");
            }
        }
        
        // Create immutable list
        this.comment = comment;
        this.bindProperties = List.copyOf(bindProperties);
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
     * Returns the optional comment.
     *
     * @return the comment string, or null if no comment was provided
     */
    public String comment() {
        return comment;
    }

    /**
     * Returns the property bindings.
     *
     * @return an immutable list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> bindProperties() {
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
    protected void compileElement() {
        // Anyline doesn't need compilation - no-op but required by interface
        // This method is idempotent and can be called multiple times safely
    }



    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitAnyline(this);
    }

    /**
     * Returns the property bindings that define how the matched line maps to JSON properties.
     *
     * @return the list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> getBindProperties() {
        return bindProperties;
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
     * @return a new Anyline instance for fluent construction
     */
    public static Anyline builder() {
        return new Anyline(null, Collections.emptyList());
    }

    /**
     * Sets the comment for this anyline pattern.
     *
     * @param comment optional descriptive comment, may be null
     * @return a new Anyline instance for method chaining
     */
    public Anyline comment(String comment) {
        return new Anyline(comment, this.bindProperties);
    }

    /**
     * Sets the property bindings for the matched line.
     *
     * @param bindings the property bindings for the matched line, must not be null
     * @return a new Anyline instance for method chaining
     */
    public Anyline bindProperties(PropertyBinding... bindings) {
        return new Anyline(this.comment, List.of(bindings));
    }

    /**
     * Sets the property bindings for the matched line.
     *
     * @param bindings the property bindings for the matched line, must not be null
     * @return a new Anyline instance for method chaining
     */
    public Anyline bindProperties(List<PropertyBinding> bindings) {
        return new Anyline(this.comment, bindings);
    }

    /**
     * Completes the fluent construction.
     *
     * @return this Anyline instance
     */
    public Anyline build() {
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
        Anyline anyline = (Anyline) obj;
        return Objects.equals(comment, anyline.comment) &&
               Objects.equals(bindProperties, anyline.bindProperties);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(comment, bindProperties);
    }

    /**
     * Returns a string representation of this Anyline.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Anyline[" +
               (comment != null ? "comment=" + comment + ", " : "") +
               "bindProperties=" + bindProperties.size() + " bindings" +
               "]";
    }
}