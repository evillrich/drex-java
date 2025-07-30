package io.github.evillrich.drex.engine;

import io.github.evillrich.drex.pattern.GroupingPatternElement;
import io.github.evillrich.drex.pattern.LineElement;
import io.github.evillrich.drex.pattern.PropertyBinding;
import io.github.evillrich.drex.pattern.Repeat;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A greedy, non-backtracking NFA simulator implementation.
 * <p>
 * This simulator processes documents using a single-pass, greedy algorithm
 * that makes deterministic choices at decision points without backtracking.
 * For repeat patterns, it always attempts the "more" option first. For OR
 * patterns, it tries alternatives in order and takes the first successful match.
 * <p>
 * The simulator performs inline matching and binding, building the JSON structure
 * incrementally as patterns are matched without intermediate objects.
 * <p>
 * The simulator is thread-safe and can be reused across multiple simulations.
 *
 * @since 1.0
 */
public final class GreedyOnePassSimulator implements NFASimulator {
    private final NFA nfa;
    private final int editDistance;

    /**
     * Creates a new greedy simulator for the specified NFA.
     *
     * @param nfa the NFA to simulate, must not be null
     * @param editDistance the maximum edit distance for fuzzy matching (0 for exact matching)
     * @throws IllegalArgumentException if nfa is null or editDistance is negative
     */
    public GreedyOnePassSimulator(NFA nfa, int editDistance) {
        this.nfa = Objects.requireNonNull(nfa, "NFA cannot be null");
        if (editDistance < 0) {
            throw new IllegalArgumentException("Edit distance cannot be negative: " + editDistance);
        }
        this.editDistance = editDistance;
    }

    /**
     * Creates a new greedy simulator with exact matching (editDistance = 0).
     *
     * @param nfa the NFA to simulate, must not be null
     * @throws IllegalArgumentException if nfa is null
     */
    public GreedyOnePassSimulator(NFA nfa) {
        this(nfa, 0);
    }

    /**
     * Returns the NFA being simulated by this instance.
     *
     * @return the NFA, never null
     */
    public NFA getNfa() {
        return nfa;
    }

    /**
     * Returns the edit distance for fuzzy matching.
     *
     * @return the edit distance, always non-negative
     */
    public int getEditDistance() {
        return editDistance;
    }

    /**
     * Simulates the NFA against the provided document lines.
     *
     * @param lines the document lines to process, must not be null
     * @return the simulation result containing extracted data or failure information
     * @throws IllegalArgumentException if lines is null
     */
    public SimulationResult simulate(List<String> lines) {
        Objects.requireNonNull(lines, "Document lines cannot be null");
        return simulate(lines.toArray(new String[0]));
    }

    @Override
    public SimulationResult simulate(NFA nfa, String[] documentLines) {
        Objects.requireNonNull(nfa, "NFA cannot be null");
        Objects.requireNonNull(documentLines, "Document lines cannot be null");
        
        // Initialize binding context and simulation state
        BindingContext bindingContext = new BindingContext();
        State currentState = nfa.getInitialState();
        int lineIndex = 0;
        int linesMatched = 0;
        
        try {
            // Main simulation loop
            while (currentState != nfa.getFinalState() && lineIndex < documentLines.length) {
                String currentLine = documentLines[lineIndex];
                
                // Find the next transition to follow
                Transition transition = selectTransition(currentState, currentLine);
                if (transition == null) {
                    return new SimulationResult(
                        "No valid transition found at line " + lineIndex + ": " + currentLine,
                        lineIndex
                    );
                }
                
                // Execute the transition
                boolean advanceLine = executeTransition(transition, currentLine, lineIndex, bindingContext);
                if (advanceLine) {
                    lineIndex++;
                    linesMatched++;
                }
                
                // Move to next state
                currentState = transition.getToState();
            }
            
            // Check if we reached the final state
            if (currentState != nfa.getFinalState()) {
                return new SimulationResult(
                    "Pattern incomplete: reached end of document without completing pattern",
                    lineIndex
                );
            }
            
            // Success - return extracted data
            return new SimulationResult(bindingContext.toJsonMap(), lineIndex, linesMatched);
            
        } catch (Exception e) {
            return new SimulationResult(
                "Simulation error: " + e.getMessage(),
                lineIndex
            );
        }
    }

    /**
     * Selects the next transition to follow from the current state.
     * <p>
     * This implements the greedy selection strategy:
     * - For MatchLine transitions, try exact match first, then fuzzy if enabled
     * - For branching (Or), try alternatives in order
     * - For repeat, prefer "more" over "zero" when both are available
     *
     * @param currentState the current state
     * @param currentLine the current line being processed
     * @return the selected transition, or null if no valid transition is found
     */
    private Transition selectTransition(State currentState, String currentLine) {
        List<Transition> transitions = currentState.getTransitionsOut();
        
        // Try exact matches first
        for (Transition transition : transitions) {
            if (transition.getEditOperation() == Transition.EditType.None) {
                if (isTransitionValid(transition, currentLine)) {
                    return transition;
                }
            }
        }
        
        // Try fuzzy matches if edit distance > 0
        if (editDistance > 0) {
            for (Transition transition : transitions) {
                if (transition.getEditOperation() != Transition.EditType.None) {
                    if (isTransitionValid(transition, currentLine)) {
                        return transition;
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Checks if a transition is valid for the current line.
     *
     * @param transition the transition to check
     * @param currentLine the current line
     * @return true if the transition can be taken, false otherwise
     */
    private boolean isTransitionValid(Transition transition, String currentLine) {
        switch (transition.getOperation()) {
            case MatchLine:
                LineElement lineElement = transition.getLine();
                if (lineElement == null) {
                    return false;
                }
                Pattern pattern = lineElement.getCompiledPattern();
                return pattern.matcher(currentLine).find();
                
            case OrSplit:
            case OrJoin:
            case RepeatZero:
            case RepeatOne:
            case RepeatMore:
            case RepeatEnd:
            case StartGroup:
            case EndGroup:
            case StartContinuation:
            case EndContinuation:
                // These transitions are always valid (they don't depend on line content)
                return true;
                
            default:
                return false;
        }
    }

    /**
     * Executes a transition and updates the binding context.
     *
     * @param transition the transition to execute
     * @param currentLine the current line being processed
     * @param lineIndex the current line index
     * @param bindingContext the binding context to update
     * @return true if the line index should advance, false otherwise
     */
    private boolean executeTransition(Transition transition, String currentLine, 
                                    int lineIndex, BindingContext bindingContext) {
        switch (transition.getOperation()) {
            case MatchLine:
                return executeMatchLine(transition, currentLine, lineIndex, bindingContext);
                
            case StartGroup:
                return executeStartGroup(transition, bindingContext);
                
            case EndGroup:
                return executeEndGroup(transition, bindingContext);
                
            case RepeatOne:
                return executeRepeatStart(transition, bindingContext);
                
            case RepeatEnd:
                return executeRepeatEnd(transition, bindingContext);
                
            case OrSplit:
            case OrJoin:
            case RepeatZero:
            case RepeatMore:
            case StartContinuation:
            case EndContinuation:
                // These operations don't require special handling in this simplified implementation
                return false;
                
            default:
                return false;
        }
    }

    /**
     * Executes a MatchLine transition with inline binding.
     */
    private boolean executeMatchLine(Transition transition, String currentLine, 
                                   int lineIndex, BindingContext bindingContext) {
        LineElement lineElement = transition.getLine();
        Pattern pattern = lineElement.getCompiledPattern();
        Matcher matcher = pattern.matcher(currentLine);
        
        if (matcher.find()) {
            List<PropertyBinding> bindings = lineElement.getBindProperties();
            
            // Handle Anyline vs Line elements differently
            if (lineElement instanceof io.github.evillrich.drex.pattern.Anyline) {
                // For Anyline, bind the entire line to all specified properties
                for (PropertyBinding binding : bindings) {
                    String formattedValue = bindingContext.formatValue(currentLine, binding.getFormat());
                    
                    // Bind the property
                    bindingContext.bindProperty(
                        binding.getProperty(), 
                        formattedValue, 
                        binding.getFormat()
                    );
                    
                    // Record capture for future position tracking
                    bindingContext.recordCapture(new CaptureInfo(
                        bindingContext.getCurrentPath() + "." + binding.getProperty(),
                        currentLine,
                        formattedValue,
                        lineIndex,
                        0,
                        currentLine.length()
                    ));
                }
            } else {
                // For Line elements, bind captured groups
                for (int i = 0; i < bindings.size() && i + 1 <= matcher.groupCount(); i++) {
                    PropertyBinding binding = bindings.get(i);
                    String capturedValue = matcher.group(i + 1); // Regex groups are 1-based
                    String formattedValue = bindingContext.formatValue(capturedValue, binding.getFormat());
                    
                    // Bind the property
                    bindingContext.bindProperty(
                        binding.getProperty(), 
                        formattedValue, 
                        binding.getFormat()
                    );
                    
                    // Record capture for future position tracking
                    bindingContext.recordCapture(new CaptureInfo(
                        bindingContext.getCurrentPath() + "." + binding.getProperty(),
                        capturedValue,
                        formattedValue,
                        lineIndex,
                        matcher.start(i + 1),
                        matcher.end(i + 1)
                    ));
                }
            }
            return true; // Advance to next line
        }
        
        return false; // Match failed
    }

    /**
     * Executes a StartGroup transition.
     */
    private boolean executeStartGroup(Transition transition, BindingContext bindingContext) {
        GroupingPatternElement group = transition.getGroup();
        if (group != null && group.getBindObject() != null) {
            bindingContext.pushObject(group.getBindObject());
        }
        return false; // Don't advance line
    }

    /**
     * Executes an EndGroup transition.
     */
    private boolean executeEndGroup(Transition transition, BindingContext bindingContext) {
        bindingContext.popObject();
        return false; // Don't advance line
    }

    /**
     * Executes a RepeatOne transition (start of repeat).
     */
    private boolean executeRepeatStart(Transition transition, BindingContext bindingContext) {
        GroupingPatternElement group = transition.getGroup();
        if (group != null && group instanceof io.github.evillrich.drex.pattern.Repeat) {
            io.github.evillrich.drex.pattern.Repeat repeat = (io.github.evillrich.drex.pattern.Repeat) group;
            String bindArrayName = repeat.getBindArray();
            bindingContext.pushArray(bindArrayName);
            bindingContext.pushArrayItem(); // Start first array item
        }
        return false; // Don't advance line
    }

    /**
     * Executes a RepeatEnd transition.
     */
    private boolean executeRepeatEnd(Transition transition, BindingContext bindingContext) {
        bindingContext.popObject(); // Pop current array item
        bindingContext.popArray();  // Pop array context
        return false; // Don't advance line
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GreedyOnePassSimulator that = (GreedyOnePassSimulator) obj;
        return Objects.equals(nfa, that.nfa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nfa);
    }

    @Override
    public String toString() {
        return String.format("GreedyOnePassSimulator{nfa=%s}", nfa);
    }
}