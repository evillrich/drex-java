package io.github.evillrich.drex.pattern;

import java.util.Collections;
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
 * @param comment optional descriptive comment, may be null
 * @param bindObject the name of the JSON object to create, never null or empty
 * @param elements the child pattern elements, never null but may be empty
 * @since 1.0
 * @see GroupingPatternElement
 */
public record Group(
    String comment,
    String bindObject,
    List<PatternElement> elements
) implements GroupingPatternElement {

    /**
     * Creates a Group record with validation and immutable list creation.
     */
    public Group {
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
        bindObject = bindObject.trim();
        elements = Collections.unmodifiableList(List.copyOf(elements));
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

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitGroup(this);
    }

    /**
     * Returns an immutable list of child pattern elements.
     * <p>
     * This is a convenience method equivalent to accessing the {@code elements} component directly.
     *
     * @return an unmodifiable list of child elements, never null but may be empty
     */
    @Override
    public List<PatternElement> getElements() {
        return elements;
    }

    /**
     * Returns the name of the JSON object that this element creates.
     * <p>
     * This is a convenience method equivalent to accessing the {@code bindObject} component directly.
     *
     * @return the object binding name, never null or empty
     */
    @Override
    public String getBindObject() {
        return bindObject;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     * <p>
     * This is a convenience method equivalent to accessing the {@code comment} component directly.
     *
     * @return the comment string, or null if no comment was provided
     */
    @Override
    public String getComment() {
        return comment;
    }
}