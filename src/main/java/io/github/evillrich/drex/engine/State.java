package io.github.evillrich.drex.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a state in the NFA (Non-deterministic Finite Automaton).
 * <p>
 * Each state has a unique number for identification and maintains a list
 * of outgoing transitions to other states. States are mutable during NFA
 * construction but should be treated as immutable during matching.
 *
 * @since 1.0
 */
public final class State {
    private final int number;
    private final List<Transition> transitionsOut;

    /**
     * Creates a new state with the specified number.
     *
     * @param number the unique identifier for this state
     */
    public State(int number) {
        this.number = number;
        this.transitionsOut = new ArrayList<>();
    }

    /**
     * Returns the unique number identifying this state.
     *
     * @return the state number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns an unmodifiable view of the outgoing transitions from this state.
     *
     * @return the list of outgoing transitions, never null
     */
    public List<Transition> getTransitionsOut() {
        return List.copyOf(transitionsOut);
    }

    /**
     * Adds an outgoing transition from this state.
     * <p>
     * This method should only be called during NFA construction.
     *
     * @param transition the transition to add, must not be null
     * @throws IllegalArgumentException if transition is null
     */
    public void addTransition(Transition transition) {
        Objects.requireNonNull(transition, "Transition cannot be null");
        transitionsOut.add(transition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return number == state.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return String.format("State{number=%d, transitions=%d}", number, transitionsOut.size());
    }
}