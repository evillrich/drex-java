package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for pattern elements that create JSON object contexts.
 * <p>
 * GroupingPatternElement represents pattern elements that bind their extracted
 * data to named JSON objects. This includes the root DrexPattern and Group
 * elements that create nested object structures in the output.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see DrexPattern
 * @see Group
 */
public abstract class GroupingPatternElement extends CompositePatternElement {

    private final String bindObject;

    /**
     * Constructs a new GroupingPatternElement with the specified object binding.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the JSON object to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if bindObject is null or empty, or if elements contains null values
     */
    protected GroupingPatternElement(String comment, String bindObject, List<PatternElement> elements) {
        super(comment, elements);
        
        if (bindObject == null || bindObject.trim().isEmpty()) {
            throw new IllegalArgumentException("bindObject must not be null or empty");
        }
        
        this.bindObject = bindObject.trim();
    }

    /**
     * Constructs a new GroupingPatternElement with the specified object binding.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the JSON object to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if bindObject is null or empty, or if elements contains null values
     */
    protected GroupingPatternElement(String comment, String bindObject, PatternElement... elements) {
        super(comment, elements);
        
        if (bindObject == null || bindObject.trim().isEmpty()) {
            throw new IllegalArgumentException("bindObject must not be null or empty");
        }
        
        this.bindObject = bindObject.trim();
    }

    /**
     * Returns the name of the JSON object that this element creates.
     * <p>
     * The bind object name is used to create a named JSON object in the output
     * structure. Child elements will bind their extracted data as properties
     * within this object.
     *
     * @return the object binding name, never null or empty
     */
    public final String getBindObject() {
        return bindObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        GroupingPatternElement that = (GroupingPatternElement) obj;
        return Objects.equals(bindObject, that.bindObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bindObject);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + 
               "[bindObject=" + bindObject + 
               ", elements=" + getElementCount() +
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }
}