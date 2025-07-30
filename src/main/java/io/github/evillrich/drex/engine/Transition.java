package io.github.evillrich.drex.engine;

import io.github.evillrich.drex.pattern.CompositePatternElement;
import io.github.evillrich.drex.pattern.GroupingPatternElement;
import io.github.evillrich.drex.pattern.LineElement;
import java.util.Objects;

/**
 * Represents a transition between two states in the NFA.
 * <p>
 * A transition defines an operation to be performed during pattern matching,
 * including the operation type, any associated edit operation for fuzzy matching,
 * and references to pattern elements (groups or lines) that should be processed.
 * Transitions are immutable once created.
 *
 * @since 1.0
 */
public final class Transition {

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

    /**
     * Represents the type of operation performed by an NFA transition.
     * <p>
     * These operation types correspond to different phases of pattern matching:
     * line matching, logical operations (or), repetition control, grouping,
     * and continuation handling.
     *
     * @since 1.0
     */
    public enum OperationType {
        /**
         * Match a line pattern against the current input line.
         */
        MatchLine,
        
        /**
         * Split execution to try multiple alternatives in an OR operation.
         */
        OrSplit,
        
        /**
         * Join execution paths after an OR operation completes.
         */
        OrJoin,
        
        /**
         * Transition for zero repetitions in a repeat pattern.
         */
        RepeatZero,
        
        /**
         * Transition for one repetition in a repeat pattern.
         */
        RepeatOne,
        
        /**
         * Transition for additional repetitions in a repeat pattern.
         */
        RepeatMore,
        
        /**
         * Transition for consuming lines during repetition of anyline patterns.
         */
        RepeatAnyLineMore,
        
        /**
         * Transition to end a repeat pattern.
         */
        RepeatEnd,
        
        /**
         * Transition to start a grouping context.
         */
        StartGroup,
        
        /**
         * Transition to end a grouping context.
         */
        EndGroup,
        
        /**
         * Transition to start a continuation context.
         */
        StartContinuation,
        
        /**
         * Transition to end a continuation context.
         */
        EndContinuation
    }

    private final OperationType operation;
    private final EditType editOperation;
    private final State fromState;
    private final State toState;
    private final CompositePatternElement compositeElement;
    private final LineElement line;

    /**
     * Creates a new transition between two states.
     *
     * @param operation the type of operation this transition performs, must not be null
     * @param editOperation the edit operation type for fuzzy matching, must not be null
     * @param fromState the source state, must not be null
     * @param toState the destination state, must not be null
     * @param compositeElement the composite pattern element (Group, Repeat, Or, etc.) associated with this transition, may be null
     * @param line the line element associated with this transition, may be null
     * @throws IllegalArgumentException if any required parameter is null
     */
    public Transition(OperationType operation, EditType editOperation, 
                     State fromState, State toState, 
                     CompositePatternElement compositeElement, LineElement line) {
        this.operation = Objects.requireNonNull(operation, "Operation type cannot be null");
        this.editOperation = Objects.requireNonNull(editOperation, "Edit operation cannot be null");
        this.fromState = Objects.requireNonNull(fromState, "From state cannot be null");
        this.toState = Objects.requireNonNull(toState, "To state cannot be null");
        this.compositeElement = compositeElement;
        this.line = line;
    }

    /**
     * Creates a new transition with no edit operation (exact match).
     *
     * @param operation the type of operation this transition performs, must not be null
     * @param fromState the source state, must not be null
     * @param toState the destination state, must not be null
     * @param compositeElement the composite pattern element associated with this transition, may be null
     * @param line the line element associated with this transition, may be null
     * @throws IllegalArgumentException if any required parameter is null
     */
    public Transition(OperationType operation, State fromState, State toState, 
                     CompositePatternElement compositeElement, LineElement line) {
        this(operation, EditType.None, fromState, toState, compositeElement, line);
    }

    /**
     * Returns the operation type for this transition.
     *
     * @return the operation type, never null
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * Returns the edit operation type for fuzzy matching.
     *
     * @return the edit operation type, never null
     */
    public EditType getEditOperation() {
        return editOperation;
    }

    /**
     * Returns the source state of this transition.
     *
     * @return the from state, never null
     */
    public State getFromState() {
        return fromState;
    }

    /**
     * Returns the destination state of this transition.
     *
     * @return the to state, never null
     */
    public State getToState() {
        return toState;
    }

    /**
     * Returns the composite pattern element associated with this transition.
     *
     * @return the composite element (Group, Repeat, Or, etc.), may be null
     */
    public CompositePatternElement getCompositeElement() {
        return compositeElement;
    }

    /**
     * Returns the grouping pattern element associated with this transition.
     * This is a convenience method that casts the composite element to GroupingPatternElement.
     *
     * @return the group element, may be null
     * @throws ClassCastException if the composite element is not a GroupingPatternElement
     * @deprecated Use getCompositeElement() and instanceof checks instead
     */
    @Deprecated
    public GroupingPatternElement getGroup() {
        return (GroupingPatternElement) compositeElement;
    }

    /**
     * Returns the line element associated with this transition.
     *
     * @return the line element, may be null
     */
    public LineElement getLine() {
        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transition that = (Transition) obj;
        return operation == that.operation &&
               editOperation == that.editOperation &&
               Objects.equals(fromState, that.fromState) &&
               Objects.equals(toState, that.toState) &&
               Objects.equals(compositeElement, that.compositeElement) &&
               Objects.equals(line, that.line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, editOperation, fromState, toState, compositeElement, line);
    }

    @Override
    public String toString() {
        return String.format("Transition{%s->%s, op=%s, edit=%s}", 
                           fromState.getNumber(), toState.getNumber(), 
                           operation, editOperation);
    }
}