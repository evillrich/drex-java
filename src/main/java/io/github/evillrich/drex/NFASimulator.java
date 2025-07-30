package io.github.evillrich.drex;

import java.util.List;

/**
 * Interface for NFA simulation algorithms.
 * <p>
 * NFA simulators execute the pattern matching logic by traversing the NFA
 * states and transitions while processing input document lines. Different
 * implementations may use various strategies such as greedy matching,
 * backtracking, or parallel execution.
 * <p>
 * Implementations should be thread-safe if they will be used concurrently.
 *
 * @since 1.0
 */
interface NFASimulator {
    
    /**
     * Simulates the NFA against the given document lines to extract matching data.
     * <p>
     * The simulator processes the document according to its specific algorithm,
     * following the NFA transitions and executing the associated operations.
     *
     * @param documentLines the lines of the document to process, must not be null
     * @return the result of the simulation including extracted data and metadata
     * @throws IllegalArgumentException if nfa or documentLines is null
     */
    SimulationResult simulate(List<String> documentLines);
}