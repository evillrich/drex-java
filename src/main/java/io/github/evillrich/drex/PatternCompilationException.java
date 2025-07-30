package io.github.evillrich.drex;

/**
 * Exception thrown when a pattern element cannot be compiled for matching.
 * <p>
 * PatternCompilationException indicates that a pattern element contains
 * invalid configuration, malformed regular expressions, or other issues
 * that prevent it from being prepared for matching operations.
 * <p>
 * This is an unchecked exception as pattern compilation errors typically
 * indicate configuration problems that cannot be recovered from at runtime.
 * These errors should be caught during development and testing phases.
 *
 * @since 1.0
 * @see PatternElement#compileElement()
 */
public class PatternCompilationException extends RuntimeException {

    /**
     * Constructs a new PatternCompilationException with the specified detail message.
     *
     * @param message the detail message explaining the compilation failure
     */
    public PatternCompilationException(String message) {
        super(message);
    }

    /**
     * Constructs a new PatternCompilationException with the specified detail message and cause.
     *
     * @param message the detail message explaining the compilation failure
     * @param cause the underlying cause of the compilation failure
     */
    public PatternCompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new PatternCompilationException with the specified cause.
     *
     * @param cause the underlying cause of the compilation failure
     */
    public PatternCompilationException(Throwable cause) {
        super(cause);
    }
}