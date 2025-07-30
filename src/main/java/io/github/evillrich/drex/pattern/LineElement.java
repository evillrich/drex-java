package io.github.evillrich.drex.pattern;

import java.util.List;

/**
 * Abstract base class for pattern elements that match individual text lines.
 * <p>
 * LineElement represents terminal pattern elements that process single lines
 * of text input. This includes Line elements that match regex patterns and
 * Anyline elements that match any single line.
 * <p>
 * Line elements are the atomic units of pattern matching in the Drex engine,
 * responsible for extracting data from individual lines and binding it to
 * JSON properties.
 * <p>
 * Implementations are immutable and thread-safe.
 *
 * @since 1.0
 * @see Line
 * @see Anyline
 */
public abstract class LineElement extends PatternElement {


    /**
     * Returns the property bindings for capturing data from matched lines.
     * <p>
     * This method returns a list of PropertyBinding objects that define how
     * regex capture groups should be bound to JSON properties.
     *
     * @return an immutable list of property bindings, never null but may be empty
     */
    public abstract List<PropertyBinding> getBindProperties();

}