package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.regex.Pattern;

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
     * Returns the compiled regex pattern for matching lines.
     * <p>
     * This method returns the pattern that will be used to match against input lines.
     * The pattern element must be compiled before calling this method.
     *
     * @return the compiled Pattern, never null
     * @throws IllegalStateException if the pattern has not been compiled
     */
    public abstract Pattern getCompiledPattern();

    /**
     * Returns the property bindings for capturing data from matched lines.
     * <p>
     * This method returns a list of PropertyBinding objects that define how
     * regex capture groups should be bound to JSON properties.
     *
     * @return an immutable list of property bindings, never null but may be empty
     */
    public abstract List<PropertyBinding> getBindProperties();

    /**
     * Attempts to match the specified input line against this line element's pattern.
     * <p>
     * This method processes a single line of text input and attempts to match it
     * against the pattern defined by this line element. For Line elements, this
     * involves regex matching with capture groups. For Anyline elements, this
     * matches any non-empty line.
     * <p>
     * The pattern element must be compiled before calling this method.
     * <p>
     * <strong>Deprecated:</strong> This method is deprecated in favor of the inline
     * binding approach using {@link #getCompiledPattern()} and {@link #getBindProperties()}.
     * It will be removed in a future version.
     *
     * @param inputLine the line of text to match against, must not be null
     * @return a LineMatchResult indicating success or failure and any captured data
     * @throws IllegalArgumentException if inputLine is null
     * @throws IllegalStateException if the pattern has not been compiled
     * @deprecated Use inline binding with getCompiledPattern() and getBindProperties() instead
     */
    @Deprecated
    public abstract LineMatchResult match(String inputLine);
}