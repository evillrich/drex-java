package io.github.evillrich.drex.engine;

import io.github.evillrich.drex.pattern.DrexPattern;
import java.util.Objects;

/**
 * Represents a Non-deterministic Finite Automaton (NFA) for pattern matching.
 * <p>
 * An NFA consists of an initial state where matching begins and a final state
 * that indicates successful pattern completion. The NFA is constructed from
 * pattern elements and used by simulators to perform document matching.
 * <p>
 * NFA instances are immutable once constructed and are thread-safe.
 *
 * @since 1.0
 */
public final class NFA {
    private final State initialState;
    private final State finalState;

    /**
     * Creates a new NFA with the specified initial and final states.
     *
     * @param initialState the starting state for pattern matching, must not be null
     * @param finalState the accepting state indicating successful match, must not be null
     * @throws IllegalArgumentException if either state is null
     */
    public NFA(State initialState, State finalState) {
        this.initialState = Objects.requireNonNull(initialState, "Initial state cannot be null");
        this.finalState = Objects.requireNonNull(finalState, "Final state cannot be null");
    }

    /**
     * Creates an NFA from the specified DrexPattern.
     * <p>
     * This factory method constructs an NFA by visiting the pattern elements
     * using the visitor pattern. The pattern is first compiled to ensure all
     * regex patterns are valid before NFA construction.
     *
     * @param pattern the DrexPattern to convert to NFA, must not be null
     * @return the constructed NFA for the pattern, never null
     * @throws IllegalArgumentException if pattern is null
     * @throws io.github.evillrich.drex.pattern.PatternCompilationException if pattern compilation fails
     */
    public static NFA fromPattern(DrexPattern pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        
        // Build the NFA using the visitor pattern
        NFABuilder builder = new NFABuilder(pattern);
        return builder.visitDrexPattern(pattern);
    }

    /**
     * Returns the initial state where pattern matching begins.
     *
     * @return the initial state, never null
     */
    public State getInitialState() {
        return initialState;
    }

    /**
     * Returns the final state indicating successful pattern completion.
     *
     * @return the final state, never null
     */
    public State getFinalState() {
        return finalState;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NFA nfa = (NFA) obj;
        return Objects.equals(initialState, nfa.initialState) &&
               Objects.equals(finalState, nfa.finalState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialState, finalState);
    }

    @Override
    public String toString() {
        return String.format("NFA{initial=%s, final=%s}", 
                           initialState.getNumber(), finalState.getNumber());
    }
}