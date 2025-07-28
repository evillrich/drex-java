package io.github.evillrich.drex.pattern;

import java.util.Objects;

/**
 * Abstract base class for all pattern elements in the Drex pattern hierarchy.
 * <p>
 * PatternElement represents a single component in a document pattern that can be
 * used to match and extract data from text documents. All pattern elements support
 * the Visitor pattern for processing and evaluation.
 * <p>
 * Pattern elements are immutable and thread-safe once constructed.
 *
 * @since 1.0
 * @see PatternVisitor
 */
public abstract class PatternElement {

    private final String comment;

    /**
     * Constructs a new PatternElement with an optional comment.
     *
     * @param comment optional descriptive comment for this pattern element, may be null
     */
    protected PatternElement(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     * <p>
     * Comments are used for documentation purposes and do not affect pattern matching behavior.
     *
     * @return the comment string, or null if no comment was provided
     */
    public final String getComment() {
        return comment;
    }

    /**
     * Compiles this pattern element for efficient matching.
     * <p>
     * This method prepares the pattern element for matching operations by
     * compiling regular expressions, validating structure, and performing
     * any other initialization required for efficient processing.
     * <p>
     * This method should be called once after pattern construction and before
     * any matching operations. Implementations may cache compiled state and
     * should be idempotent - multiple calls should have the same effect as a single call.
     *
     * @throws PatternCompilationException if the pattern cannot be compiled (unchecked)
     * @throws IllegalStateException if the pattern is in an invalid state for compilation
     */
    public abstract void compile();

    /**
     * Accepts a visitor for processing this pattern element.
     * <p>
     * This method implements the Visitor pattern, allowing different operations
     * to be performed on pattern elements without modifying their classes.
     *
     * @param visitor the visitor to accept, must not be null
     * @param <T> the return type of the visitor operation
     * @return the result of the visitor operation
     * @throws IllegalArgumentException if visitor is null
     */
    public abstract <T> T accept(PatternVisitor<T> visitor);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PatternElement that = (PatternElement) obj;
        return Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + 
               (comment != null ? "[comment=" + comment + "]" : "");
    }
}