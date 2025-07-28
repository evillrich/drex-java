package io.github.evillrich.drex.pattern;

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
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see Line
 * @see Anyline
 */
public abstract class LineElement extends PatternElement {

    /**
     * Constructs a new LineElement with an optional comment.
     *
     * @param comment optional descriptive comment for this line element, may be null
     */
    protected LineElement(String comment) {
        super(comment);
    }

    /**
     * Attempts to match the specified input line against this line element's pattern.
     * <p>
     * This method processes a single line of text input and attempts to match it
     * against the pattern defined by this line element. For Line elements, this
     * involves regex matching with capture groups. For Anyline elements, this
     * matches any non-empty line.
     * <p>
     * The pattern element must be compiled before calling this method.
     *
     * @param inputLine the line of text to match against, must not be null
     * @return a LineMatchResult indicating success or failure and any captured data
     * @throws IllegalArgumentException if inputLine is null
     * @throws IllegalStateException if the pattern has not been compiled
     */
    public abstract LineMatchResult match(String inputLine);
}