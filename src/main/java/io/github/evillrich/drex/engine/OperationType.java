package io.github.evillrich.drex.engine;

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