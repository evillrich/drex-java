package io.github.evillrich.drex;

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
abstract class PatternElement {

    /**
     * Returns the optional comment associated with this pattern element.
     * <p>
     * Comments are used for documentation purposes and do not affect pattern matching behavior.
     *
     * @return the comment string, or null if no comment was provided
     */
    public abstract String getComment();

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
    protected abstract void compileElement();

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
}