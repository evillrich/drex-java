package io.github.evillrich.drex.pattern;

/**
 * Represents a binding between a regex capture group and a JSON property.
 * <p>
 * PropertyBinding defines how captured text from regex patterns should be
 * mapped to properties in the output JSON structure. Each binding specifies
 * a property name and an optional formatter function to normalize the captured text.
 * <p>
 * Property bindings are positionally mapped to regex capture groups:
 * the first binding corresponds to capture group 1, the second to group 2, etc.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @param property the JSON property name to bind to, never null or empty
 * @param format optional formatter function name, may be null for no formatting
 * @since 1.0
 * @see Line
 * @see Anyline
 */
public record PropertyBinding(
    String property,
    String format
) {

    /**
     * Creates a PropertyBinding with validation and trimming.
     */
    public PropertyBinding {
        if (property == null || property.trim().isEmpty()) {
            throw new IllegalArgumentException("property must not be null or empty");
        }
        
        // Trim the property and format strings
        property = property.trim();
        format = format != null ? format.trim() : null;
        
        // Convert empty format strings to null for consistency
        if (format != null && format.isEmpty()) {
            format = null;
        }
    }

    /**
     * Creates a new PropertyBinding with the specified property name and no formatter.
     *
     * @param property the JSON property name to bind to, must not be null or empty
     * @throws IllegalArgumentException if property is null or empty
     */
    public PropertyBinding(String property) {
        this(property, null);
    }

    /**
     * Creates a new PropertyBinding with the specified property name.
     * <p>
     * This is a convenience factory method equivalent to calling the constructor.
     *
     * @param property the JSON property name to bind to, must not be null or empty
     * @return a new PropertyBinding instance, never null
     * @throws IllegalArgumentException if property is null or empty
     */
    public static PropertyBinding of(String property) {
        return new PropertyBinding(property);
    }

    /**
     * Creates a new PropertyBinding with the specified property name and formatter.
     * <p>
     * This is a convenience factory method equivalent to calling the constructor.
     *
     * @param property the JSON property name to bind to, must not be null or empty
     * @param format optional formatter function name, may be null for no formatting
     * @return a new PropertyBinding instance, never null
     * @throws IllegalArgumentException if property is null or empty
     */
    public static PropertyBinding of(String property, String format) {
        return new PropertyBinding(property, format);
    }

    /**
     * Returns the JSON property name that captured text will be bound to.
     * <p>
     * This is a convenience method equivalent to accessing the {@code property} component directly.
     *
     * @return the property name, never null or empty
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns the optional formatter function name for normalizing captured text.
     * <p>
     * Formatters are used to normalize captured text into consistent string formats
     * before binding to JSON properties. Common formatters include currency(),
     * parseDate(), and trim().
     * <p>
     * This is a convenience method equivalent to accessing the {@code format} component directly.
     *
     * @return the formatter function name, or null if no formatting is applied
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns true if this binding has a formatter function.
     *
     * @return true if a formatter is specified, false otherwise
     */
    public boolean hasFormat() {
        return format != null;
    }

    /**
     * Returns a string representation of this PropertyBinding.
     * <p>
     * Includes the property name and format (if present).
     */
    @Override
    public String toString() {
        return "PropertyBinding[property=" + property + 
               (format != null ? ", format=" + format : "") + "]";
    }
}