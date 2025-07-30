package io.github.evillrich.drex;

import java.util.List;

/**
 * Abstract base class for pattern elements that contain other pattern elements.
 * <p>
 * CompositePatternElement represents pattern elements that have child elements,
 * such as groups, repeats, and or-alternatives. This class provides common
 * functionality for managing collections of child pattern elements.
 * <p>
 * Implementations are immutable and thread-safe.
 *
 * @since 1.0
 * @see PatternElement
 */
abstract class CompositePatternElement extends PatternElement {

    /**
     * Returns an immutable list of child pattern elements.
     * <p>
     * The returned list cannot be modified and contains no null elements.
     *
     * @return an unmodifiable list of child elements, never null but may be empty
     */
    public abstract List<PatternElement> getElements();

    /**
     * Returns the number of child elements.
     *
     * @return the number of child elements, zero or greater
     */
    public int getElementCount() {
        return getElements().size();
    }

    /**
     * Returns true if this composite has no child elements.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return getElements().isEmpty();
    }

    /**
     * Compiles this composite pattern element and all its child elements.
     * <p>
     * This default implementation recursively compiles all child elements
     * in order. Implementations can override this method if they need additional
     * compilation logic beyond compiling their children.
     * <p>
     * This method is idempotent and can be called multiple times safely.
     *
     * @throws PatternCompilationException if any child element cannot be compiled
     */
    @Override
    protected void compileElement() {
        // Compile all child elements
        for (PatternElement element : getElements()) {
            element.compileElement();
        }
    }
}