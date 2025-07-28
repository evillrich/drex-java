package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for pattern elements that contain other pattern elements.
 * <p>
 * CompositePatternElement represents pattern elements that have child elements,
 * such as groups, repeats, and or-alternatives. This class provides common
 * functionality for managing collections of child pattern elements.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see PatternElement
 */
public abstract class CompositePatternElement extends PatternElement {

    private final List<PatternElement> elements;

    /**
     * Constructs a new CompositePatternElement with the specified child elements.
     *
     * @param comment optional descriptive comment, may be null
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if elements is null or contains null elements
     */
    protected CompositePatternElement(String comment, List<PatternElement> elements) {
        super(comment);
        Objects.requireNonNull(elements, "elements must not be null");
        
        // Validate no null elements
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null) {
                throw new IllegalArgumentException("Element at index " + i + " must not be null");
            }
        }
        
        // Create defensive copy
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    /**
     * Constructs a new CompositePatternElement with the specified child elements.
     *
     * @param comment optional descriptive comment, may be null
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if elements is null or contains null elements
     */
    protected CompositePatternElement(String comment, PatternElement... elements) {
        this(comment, Arrays.asList(elements));
    }

    /**
     * Returns an immutable list of child pattern elements.
     * <p>
     * The returned list cannot be modified and contains no null elements.
     *
     * @return an unmodifiable list of child elements, never null but may be empty
     */
    public final List<PatternElement> getElements() {
        return elements;
    }

    /**
     * Returns the number of child elements.
     *
     * @return the number of child elements, zero or greater
     */
    public final int getElementCount() {
        return elements.size();
    }

    /**
     * Returns true if this composite has no child elements.
     *
     * @return true if empty, false otherwise
     */
    public final boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Compiles this composite pattern element and all its child elements.
     * <p>
     * This default implementation recursively compiles all child elements
     * in order. Subclasses can override this method if they need additional
     * compilation logic beyond compiling their children.
     * <p>
     * This method is idempotent and can be called multiple times safely.
     *
     * @throws PatternCompilationException if any child element cannot be compiled
     */
    @Override
    public void compile() {
        // Compile all child elements
        for (PatternElement element : elements) {
            element.compile();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CompositePatternElement that = (CompositePatternElement) obj;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elements);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + 
               "[elements=" + elements.size() + 
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }
}