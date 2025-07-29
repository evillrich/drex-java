package io.github.evillrich.drex.pattern;

/**
 * Interface for pattern elements that create JSON object contexts.
 * <p>
 * GroupingPatternElement represents pattern elements that bind their extracted
 * data to named JSON objects. This includes the root DrexPattern and Group
 * elements that create nested object structures in the output.
 * <p>
 * Implementations are immutable and thread-safe.
 *
 * @since 1.0
 * @see DrexPattern
 * @see Group
 */
public interface GroupingPatternElement extends CompositePatternElement {

    /**
     * Returns the name of the JSON object that this element creates.
     * <p>
     * The bind object name is used to create a named JSON object in the output
     * structure. Child elements will bind their extracted data as properties
     * within this object.
     *
     * @return the object binding name, never null or empty
     */
    String getBindObject();
}