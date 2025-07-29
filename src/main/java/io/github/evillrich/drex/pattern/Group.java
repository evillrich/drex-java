package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.Objects;

/**
 * Represents a group pattern element that creates nested JSON objects.
 * <p>
 * Group elements are used to create structured JSON objects within the output
 * hierarchy. They bind their child elements' extracted data to a named JSON
 * object, allowing for complex nested data structures.
 * <p>
 * Groups are commonly used to organize related data fields, such as collecting
 * address information into an "address" object or contact details into a
 * "contact" object.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see GroupingPatternElement
 */
public final class Group implements GroupingPatternElement {

    private final String comment;
    private final String bindObject;
    private final List<PatternElement> elements;

    /**
     * Creates a Group with validation and immutable list creation.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the JSON object to create, never null or empty
     * @param elements the child pattern elements, never null but may be empty
     * @throws IllegalArgumentException if bindObject is null/empty or elements contains null values
     */
    public Group(String comment, String bindObject, List<PatternElement> elements) {
        if (bindObject == null || bindObject.trim().isEmpty()) {
            throw new IllegalArgumentException("bindObject must not be null or empty");
        }
        Objects.requireNonNull(elements, "elements must not be null");
        
        // Validate no null elements
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null) {
                throw new IllegalArgumentException("Element at index " + i + " must not be null");
            }
        }
        
        // Trim bindObject and create immutable list
        this.comment = comment;
        this.bindObject = bindObject.trim();
        this.elements = List.copyOf(elements);
    }

    /**
     * Creates a new Group with the specified properties.
     *
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the JSON object to create, must not be null or empty
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if bindObject is null/empty or elements contains null values
     */
    public Group(String comment, String bindObject, PatternElement... elements) {
        this(comment, bindObject, List.of(elements));
    }

    /**
     * Returns the optional comment.
     *
     * @return the comment string, or null if no comment was provided
     */
    public String comment() {
        return comment;
    }

    /**
     * Returns the name of the JSON object to create.
     *
     * @return the object binding name, never null or empty
     */
    public String bindObject() {
        return bindObject;
    }

    /**
     * Returns the child pattern elements.
     *
     * @return an immutable list of child elements, never null but may be empty
     */
    public List<PatternElement> elements() {
        return elements;
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitGroup(this);
    }

    /**
     * Returns an immutable list of child pattern elements.
     *
     * @return an unmodifiable list of child elements, never null but may be empty
     */
    @Override
    public List<PatternElement> getElements() {
        return elements;
    }

    /**
     * Returns the name of the JSON object that this element creates.
     *
     * @return the object binding name, never null or empty
     */
    @Override
    public String getBindObject() {
        return bindObject;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     *
     * @return the comment string, or null if no comment was provided
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * Creates a builder-style pattern construction (backward compatibility).
     *
     * @return a new Group instance for fluent construction
     */
    public static Group builder() {
        return new Group(null, "", List.of());
    }

    /**
     * Sets the comment for this group pattern.
     *
     * @param comment optional descriptive comment, may be null
     * @return a new Group instance for method chaining
     */
    public Group comment(String comment) {
        return new Group(comment, this.bindObject, this.elements);
    }

    /**
     * Sets the name of the JSON object that this group creates.
     *
     * @param bindObject the name of the JSON object to create, must not be null or empty
     * @return a new Group instance for method chaining
     */
    public Group bindObject(String bindObject) {
        return new Group(this.comment, bindObject, this.elements);
    }

    /**
     * Sets the child pattern elements for this group.
     *
     * @param elements the child pattern elements, must not be null
     * @return a new Group instance for method chaining
     */
    public Group elements(PatternElement... elements) {
        return new Group(this.comment, this.bindObject, List.of(elements));
    }

    /**
     * Sets the child pattern elements for this group.
     *
     * @param elements the child pattern elements, must not be null
     * @return a new Group instance for method chaining
     */
    public Group elements(List<PatternElement> elements) {
        return new Group(this.comment, this.bindObject, elements);
    }

    /**
     * Completes the fluent construction.
     *
     * @return this Group instance
     */
    public Group build() {
        return this;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group group = (Group) obj;
        return Objects.equals(comment, group.comment) &&
               Objects.equals(bindObject, group.bindObject) &&
               Objects.equals(elements, group.elements);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(comment, bindObject, elements);
    }

    /**
     * Returns a string representation of this Group.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Group[" +
               (comment != null ? "comment=" + comment + ", " : "") +
               "bindObject=" + bindObject +
               ", elements=" + elements.size() + " elements" +
               "]";
    }
}