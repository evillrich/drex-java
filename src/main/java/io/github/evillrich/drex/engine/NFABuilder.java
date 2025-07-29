package io.github.evillrich.drex.engine;
import io.github.evillrich.drex.pattern.*;

import java.util.List;

/**
 * Visitor implementation that builds Non-deterministic Finite Automata (NFA) 
 * from Drex pattern elements.
 * <p>
 * This class implements the visitor pattern to recursively traverse pattern 
 * elements and construct an NFA representation suitable for document matching.
 * The construction follows Thompson's algorithm adapted for line-based document
 * processing with support for fuzzy matching via edit distance.
 * <p>
 * The NFA construction algorithm converts pattern elements into states and
 * transitions, supporting:
 * <ul>
 * <li>Line matching with regex patterns</li>
 * <li>Group boundaries for capture groups</li>
 * <li>Repeat patterns (zero-or-more, one-or-more, zero-or-one)</li>
 * <li>Alternative matching with Or elements</li>
 * <li>Fuzzy matching with edit distance operations</li>
 * </ul>
 * <p>
 * Instances are not thread-safe and should not be reused for multiple patterns.
 *
 * @since 1.0
 * @see PatternVisitor
 * @see NFA
 * @see DrexPattern
 */
public class NFABuilder implements PatternVisitor<NFA> {

    private final DrexPattern rootPattern;
    private int stateCounter = 0;

    /**
     * Creates a new NFABuilder for the specified root pattern.
     *
     * @param rootPattern the root DrexPattern being converted to NFA, must not be null
     * @throws IllegalArgumentException if rootPattern is null
     */
    public NFABuilder(DrexPattern rootPattern) {
        if (rootPattern == null) {
            throw new IllegalArgumentException("rootPattern must not be null");
        }
        this.rootPattern = rootPattern;
    }

    /**
     * Creates a new state with a unique number.
     */
    private State newState() {
        return new State(stateCounter++);
    }

    /**
     * Visits a DrexPattern element and builds the root NFA.
     * <p>
     * This creates the overall NFA structure with group boundaries for the
     * root binding object and processes all child elements.
     *
     * @param pattern the DrexPattern to visit, never null
     * @return the constructed NFA for the pattern
     */
    @Override
    public NFA visitDrexPattern(DrexPattern pattern) {
        // Build NFA for the concatenated child elements
        NFA childNFA = buildConcatenatedNFA(pattern.getElements());
        
        // Add new initial and final states with group boundaries for the root object
        State initial = newState();
        State finalState = newState();
        
        // Start group transition for the root binding object
        Transition startTransition = new Transition(Transition.OperationType.StartGroup, 
            initial, childNFA.getInitialState(), pattern, null);
        initial.addTransition(startTransition);
        
        // End group transition for the root binding object
        Transition endTransition = new Transition(Transition.OperationType.EndGroup,
            childNFA.getFinalState(), finalState, pattern, null);
        childNFA.getFinalState().addTransition(endTransition);
        
        return new NFA(initial, finalState);
    }

    /**
     * Visits a Group element and builds an NFA with group boundaries.
     * <p>
     * Creates an NFA that represents a capture group with start and end
     * markers for binding the extracted data to the specified object.
     *
     * @param group the Group to visit, never null  
     * @return the constructed NFA for the group
     */
    @Override
    public NFA visitGroup(Group group) {
        // Build NFA for the concatenated child elements
        NFA childNFA = buildConcatenatedNFA(group.getElements());
        
        // Add new initial and final states with group boundaries
        State initial = newState();
        State finalState = newState();
        
        // Start group transition
        Transition startTransition = new Transition(Transition.OperationType.StartGroup,
            initial, childNFA.getInitialState(), group, null);
        initial.addTransition(startTransition);
        
        // End group transition  
        Transition endTransition = new Transition(Transition.OperationType.EndGroup,
            childNFA.getFinalState(), finalState, group, null);
        childNFA.getFinalState().addTransition(endTransition);
        
        return new NFA(initial, finalState);
    }

    /**
     * Visits a Repeat element and builds an NFA for repetition.
     * <p>
     * Constructs the appropriate NFA structure based on the repeat mode:
     * zero-or-more, one-or-more, or zero-or-one.
     *
     * @param repeat the Repeat to visit, never null
     * @return the constructed NFA for the repeat pattern
     */
    @Override
    public NFA visitRepeat(Repeat repeat) {
        // Repeat should have exactly one child element
        if (repeat.getElements().isEmpty()) {
            throw new IllegalStateException("Repeat element must have child elements");
        }
        
        PatternElement childElement = repeat.getElements().getFirst();
        NFA childNFA = childElement.accept(this);
        
        return switch (repeat.getMode()) {
            case ZERO_OR_MORE -> buildZeroOrMore(childNFA, childElement);
            case ONE_OR_MORE -> buildOneOrMore(childNFA, childElement);  
            case ZERO_OR_ONE -> buildZeroOrOne(childNFA, childElement);
        };
    }

    /**
     * Visits an Or element and builds an NFA for alternatives.
     * <p>
     * Creates an NFA that tries each alternative in order, using epsilon
     * transitions to branch to each possibility.
     *
     * @param or the Or to visit, never null
     * @return the constructed NFA for the alternatives
     */
    @Override
    public NFA visitOr(Or or) {
        State newInitial = newState();
        State newFinal = newState();
        
        // Create alternatives for each child element
        for (PatternElement element : or.getElements()) {
            NFA alternativeNFA = element.accept(this);
            
            // Epsilon transition from new initial to alternative's initial
            Transition splitTransition = new Transition(Transition.OperationType.OrSplit,
                newInitial, alternativeNFA.getInitialState(), null, null);
            newInitial.addTransition(splitTransition);
            
            // Epsilon transition from alternative's final to new final
            Transition joinTransition = new Transition(Transition.OperationType.OrJoin,
                alternativeNFA.getFinalState(), newFinal, null, null);
            alternativeNFA.getFinalState().addTransition(joinTransition);
        }
        
        return new NFA(newInitial, newFinal);
    }

    /**
     * Visits a Line element and builds an NFA for line matching.
     * <p>
     * Creates an NFA that matches the line's regex pattern, with optional
     * fuzzy matching transitions if edit distance is enabled.
     *
     * @param line the Line to visit, never null
     * @return the constructed NFA for line matching
     */
    @Override
    public NFA visitLine(Line line) {
        return buildLineNFA(line, false);
    }

    /**
     * Visits an Anyline element and builds an NFA for any-line matching.
     * <p>
     * Creates an NFA that matches any single line, with optional fuzzy
     * matching transitions if edit distance is enabled.
     *
     * @param anyline the Anyline to visit, never null
     * @return the constructed NFA for any-line matching
     */
    @Override
    public NFA visitAnyline(Anyline anyline) {
        return buildLineNFA(anyline, true);
    }

    // Private helper methods

    /**
     * Builds an NFA by concatenating child elements.
     */
    private NFA buildConcatenatedNFA(List<PatternElement> elements) {
        if (elements.isEmpty()) {
            // Create empty NFA with epsilon transition
            State initial = newState();
            State finalState = newState();
            Transition epsilonTransition = new Transition(Transition.OperationType.StartContinuation,
                initial, finalState, null, null);
            initial.addTransition(epsilonTransition);
            return new NFA(initial, finalState);
        }
        
        NFA result = null;
        
        for (PatternElement element : elements) {
            NFA elementNFA = element.accept(this);
            
            if (result == null) {
                result = elementNFA;
            } else {
                // Concatenate by adding epsilon transition from result's final to element's initial
                Transition concatTransition = new Transition(Transition.OperationType.StartContinuation,
                    result.getFinalState(), elementNFA.getInitialState(), null, null);
                result.getFinalState().addTransition(concatTransition);
                
                // Create new NFA with result's initial and element's final
                result = new NFA(result.getInitialState(), elementNFA.getFinalState());
            }
        }
        
        return result;
    }

    /**
     * Builds an NFA for line matching with optional fuzzy matching.
     */
    private NFA buildLineNFA(LineElement lineElement, boolean isAnyLine) {
        State initial = newState();
        State finalState = newState();
        
        // Add fuzzy matching transitions if edit distance is enabled
        if (rootPattern.getEditDistance() > 0 && !isAnyLine) {
            // Insertion - anyline transition back to initial state
            Transition insertionTransition = new Transition(Transition.OperationType.MatchLine,
                Transition.EditType.Insertion, initial, initial, null, lineElement);
            initial.addTransition(insertionTransition);
            
            // Deletion - epsilon transition to final state
            Transition deletionTransition = new Transition(Transition.OperationType.RepeatZero,
                Transition.EditType.Deletion, initial, finalState, null, null);
            initial.addTransition(deletionTransition);
            
            // Substitution - anyline transition to final state  
            Transition substitutionTransition = new Transition(Transition.OperationType.MatchLine,
                Transition.EditType.Substitution, initial, finalState, null, lineElement);
            initial.addTransition(substitutionTransition);
        }
        
        // The main transition for the line element
        Transition mainTransition = new Transition(Transition.OperationType.MatchLine,
            initial, finalState, null, lineElement);
        initial.addTransition(mainTransition);
        
        return new NFA(initial, finalState);
    }

    /**
     * Builds a zero-or-more NFA (equivalent to * quantifier).
     */
    private NFA buildZeroOrMore(NFA childNFA, PatternElement childElement) {
        State newInitial = newState();
        State newFinal = newState();
        
        boolean isAnyLine = (childElement instanceof Anyline);
        
        // Handle "Zero" - epsilon transition to final (added first for proper priority)
        Transition zeroTransition = new Transition(Transition.OperationType.RepeatZero,
            newInitial, newFinal, null, null);
        newInitial.addTransition(zeroTransition);
        
        // Epsilon transition to child NFA
        Transition oneTransition = new Transition(Transition.OperationType.RepeatOne,
            newInitial, childNFA.getInitialState(), null, null);
        newInitial.addTransition(oneTransition);
        
        // Handle ordering for AnyLine vs other elements
        if (!isAnyLine) {
            // Epsilon transition from child final to new final
            Transition endTransition = new Transition(Transition.OperationType.RepeatEnd,
                childNFA.getFinalState(), newFinal, null, null);
            childNFA.getFinalState().addTransition(endTransition);
        }
        
        // Handle "More" - epsilon transition back to child initial (added last for priority)
        if (!isAnyLine) {
            Transition moreTransition = new Transition(Transition.OperationType.RepeatMore,
                childNFA.getFinalState(), childNFA.getInitialState(), null, null);
            childNFA.getFinalState().addTransition(moreTransition);
        } else {
            Transition anylineMoreTransition = new Transition(Transition.OperationType.RepeatAnyLineMore,
                childNFA.getFinalState(), childNFA.getInitialState(), null, null);
            childNFA.getFinalState().addTransition(anylineMoreTransition);
        }
        
        if (isAnyLine) {
            // For AnyLine, add final transition after the repeat transition
            Transition endTransition = new Transition(Transition.OperationType.RepeatEnd,
                childNFA.getFinalState(), newFinal, null, null);
            childNFA.getFinalState().addTransition(endTransition);
        }
        
        return new NFA(newInitial, newFinal);
    }

    /**
     * Builds a one-or-more NFA (equivalent to + quantifier).
     */
    private NFA buildOneOrMore(NFA childNFA, PatternElement childElement) {
        State newInitial = newState();
        State newFinal = newState();
        
        boolean isAnyLine = (childElement instanceof Anyline);
        
        // Epsilon transition to child NFA (must match at least once)
        Transition oneTransition = new Transition(Transition.OperationType.RepeatOne,
            newInitial, childNFA.getInitialState(), null, null);
        newInitial.addTransition(oneTransition);
        
        // Handle ordering for AnyLine vs other elements
        if (!isAnyLine) {
            // Epsilon transition from child final to new final
            Transition endTransition = new Transition(Transition.OperationType.RepeatEnd,
                childNFA.getFinalState(), newFinal, null, null);
            childNFA.getFinalState().addTransition(endTransition);
        }
        
        // Handle "More" - epsilon transition back to child initial (added last for priority)
        if (!isAnyLine) {
            Transition moreTransition = new Transition(Transition.OperationType.RepeatMore,
                childNFA.getFinalState(), childNFA.getInitialState(), null, null);
            childNFA.getFinalState().addTransition(moreTransition);
        } else {
            Transition anylineMoreTransition = new Transition(Transition.OperationType.RepeatAnyLineMore,
                childNFA.getFinalState(), childNFA.getInitialState(), null, null);
            childNFA.getFinalState().addTransition(anylineMoreTransition);
        }
        
        if (isAnyLine) {
            // For AnyLine, add final transition after the repeat transition
            Transition endTransition = new Transition(Transition.OperationType.RepeatEnd,
                childNFA.getFinalState(), newFinal, null, null);
            childNFA.getFinalState().addTransition(endTransition);
        }
        
        return new NFA(newInitial, newFinal);
    }

    /**
     * Builds a zero-or-one NFA (equivalent to ? quantifier).
     */
    private NFA buildZeroOrOne(NFA childNFA, PatternElement childElement) {
        State newInitial = newState();
        State newFinal = newState();
        
        boolean isAnyLine = (childElement instanceof Anyline);
        
        // Handle ordering for AnyLine vs other elements
        if (!isAnyLine) {
            // Epsilon transition from child final to new final
            Transition endTransition = new Transition(Transition.OperationType.RepeatEnd,
                childNFA.getFinalState(), newFinal, null, null);
            childNFA.getFinalState().addTransition(endTransition);
        }
        
        // Handle "Zero" - epsilon transition to final (added first for proper priority)
        Transition zeroTransition = new Transition(Transition.OperationType.RepeatZero,
            newInitial, newFinal, null, null);
        newInitial.addTransition(zeroTransition);
        
        // Epsilon transition to child NFA
        Transition oneTransition = new Transition(Transition.OperationType.RepeatOne,
            newInitial, childNFA.getInitialState(), null, null);
        newInitial.addTransition(oneTransition);
        
        if (isAnyLine) {
            // For AnyLine, add final transition at the end
            Transition endTransition = new Transition(Transition.OperationType.RepeatEnd,
                childNFA.getFinalState(), newFinal, null, null);
            childNFA.getFinalState().addTransition(endTransition);
        }
        
        return new NFA(newInitial, newFinal);
    }
}