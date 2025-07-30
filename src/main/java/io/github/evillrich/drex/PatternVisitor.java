package io.github.evillrich.drex;

/**
 * Visitor interface for processing pattern elements in the Drex pattern hierarchy.
 * <p>
 * This interface implements the Visitor pattern, allowing operations to be performed
 * on different types of pattern elements without modifying their classes. Common
 * use cases include pattern validation, serialization, and matching execution.
 *
 * @param <T> the return type of visitor operations
 * @since 1.0
 * @see PatternElement
 */
interface PatternVisitor<T> {

    /**
     * Visits a DrexPattern element.
     *
     * @param pattern the DrexPattern to visit, never null
     * @return the result of visiting the pattern
     */
    T visitDrexPattern(DrexPattern pattern);

    /**
     * Visits a Group element.
     *
     * @param group the Group to visit, never null
     * @return the result of visiting the group
     */
    T visitGroup(Group group);

    /**
     * Visits a Repeat element.
     *
     * @param repeat the Repeat to visit, never null
     * @return the result of visiting the repeat
     */
    T visitRepeat(Repeat repeat);

    /**
     * Visits an Or element.
     *
     * @param or the Or to visit, never null
     * @return the result of visiting the or
     */
    T visitOr(Or or);

    /**
     * Visits a Line element.
     *
     * @param line the Line to visit, never null
     * @return the result of visiting the line
     */
    T visitLine(Line line);

    /**
     * Visits an Anyline element.
     *
     * @param anyline the Anyline to visit, never null
     * @return the result of visiting the anyline
     */
    T visitAnyline(Anyline anyline);
}