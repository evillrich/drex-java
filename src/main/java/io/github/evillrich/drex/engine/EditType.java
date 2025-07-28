package io.github.evillrich.drex.engine;

/**
 * Represents the type of edit operation performed during fuzzy matching.
 * <p>
 * These edit types correspond to the basic operations in edit distance calculations:
 * character substitution, deletion, and insertion. The {@code None} type indicates
 * an exact match with no edit operations required.
 *
 * @since 1.0
 */
public enum EditType {
    /**
     * No edit operation required - exact match.
     */
    None,
    
    /**
     * Character substitution operation.
     */
    Substitution,
    
    /**
     * Character deletion operation.
     */
    Deletion,
    
    /**
     * Character insertion operation.
     */
    Insertion
}