package io.github.evillrich.drex.pattern;

import io.github.evillrich.drex.engine.NFA;

import java.util.List;
import java.util.Objects;

/**
 * Represents the root of a document pattern that can extract structured JSON data.
 * <p>
 * DrexPattern is the top-level pattern element that defines a complete document
 * processing pattern. It contains metadata about the pattern (version, name) and
 * the root collection of pattern elements that define the matching logic.
 * <p>
 * DrexPattern instances are immutable and thread-safe once constructed.
 *
 * @since 1.0
 * @see PatternElement
 */
public final class DrexPattern extends GroupingPatternElement {

    private final String version;
    private final String name;
    private final String comment;
    private final String bindObject;
    private final int editDistance;
    private final List<PatternElement> elements;
    private NFA nfa;

    /**
     * Creates a DrexPattern with validation and immutable list creation.
     *
     * @param version the pattern version string, never null or empty
     * @param name the pattern name, never null or empty
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the root JSON object, never null or empty
     * @param editDistance the maximum edit distance for fuzzy matching, never negative
     * @param elements the child pattern elements, never null but may be empty
     * @throws IllegalArgumentException if validation fails
     */
    public DrexPattern(String version, String name, String comment, String bindObject, 
                       int editDistance, List<PatternElement> elements) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version must not be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be null or empty");
        }
        if (bindObject == null || bindObject.trim().isEmpty()) {
            throw new IllegalArgumentException("bindObject must not be null or empty");
        }
        if (editDistance < 0) {
            throw new IllegalArgumentException("editDistance must be non-negative");
        }
        Objects.requireNonNull(elements, "elements must not be null");
        
        // Validate no null elements
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null) {
                throw new IllegalArgumentException("Element at index " + i + " must not be null");
            }
        }
        
        // Trim strings and create immutable list
        this.version = version.trim();
        this.name = name.trim();
        this.comment = comment;
        this.bindObject = bindObject.trim();
        this.editDistance = editDistance;
        this.elements = List.copyOf(elements);
    }

    /**
     * Creates a new DrexPattern with the specified properties.
     *
     * @param version the pattern version string, must not be null or empty
     * @param name the pattern name, must not be null or empty
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the root JSON object, must not be null or empty
     * @param editDistance the maximum edit distance for fuzzy matching, must be non-negative
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if validation fails
     */
    public DrexPattern(String version, String name, String comment, String bindObject, 
                       int editDistance, PatternElement... elements) {
        this(version, name, comment, bindObject, editDistance, List.of(elements));
    }

    /**
     * Returns the pattern version string.
     *
     * @return the version string, never null or empty
     */
    public String version() {
        return version;
    }

    /**
     * Returns the pattern name.
     *
     * @return the pattern name, never null or empty
     */
    public String name() {
        return name;
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
     * Returns the name of the root JSON object.
     *
     * @return the object binding name, never null or empty
     */
    public String bindObject() {
        return bindObject;
    }

    /**
     * Returns the maximum edit distance for fuzzy matching.
     *
     * @return the edit distance, zero or greater
     */
    public int editDistance() {
        return editDistance;
    }

    /**
     * Returns the child pattern elements.
     *
     * @return an immutable list of child elements, never null but may be empty
     */
    public List<PatternElement> elements() {
        return elements;
    }

    /**
     * Returns true if fuzzy matching is enabled for this pattern.
     *
     * @return true if editDistance is greater than 0, false otherwise
     */
    public boolean isFuzzyMatchingEnabled() {
        return editDistance > 0;
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitDrexPattern(this);
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
     * Returns the version string of this pattern.
     *
     * @return the version string, never null or empty
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the name of this pattern.
     *
     * @return the pattern name, never null or empty
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the maximum edit distance allowed for fuzzy matching.
     *
     * @return the edit distance, zero or greater
     */
    public int getEditDistance() {
        return editDistance;
    }

    /**
     * Compiles all pattern elements contained in this DrexPattern to ensure
     * all regex patterns are valid and ready for matching.
     * <p>
     * This method delegates to each child element's compile method, ensuring
     * that all Line elements have their regex patterns compiled.
     */
    @Override
    protected void compileElement() {
        for (PatternElement element : elements) {
            element.compileElement();
        }
    }

    /**
     * Compiles the specified pattern for efficient matching.
     * <p>
     * This public static method compiles a DrexPattern and all its child elements,
     * preparing them for matching operations. This is the preferred way to compile
     * patterns from client code.
     *
     * @param pattern the pattern to compile, must not be null
     * @throws IllegalArgumentException if pattern is null
     * @throws PatternCompilationException if the pattern cannot be compiled
     */
    public void compile() {
        this.compileElement();
        nfa = NFA.fromPattern(this);
    }

    /**
     * Creates a builder-style pattern construction (backward compatibility).
     *
     * @return a new DrexPattern instance for fluent construction
     */
    public static DrexPattern builder() {
        return new DrexPattern("", "", null, "", 0, List.of());
    }

    /**
     * Sets the version for this pattern.
     *
     * @param version the pattern version string, must not be null or empty
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern version(String version) {
        return new DrexPattern(version, this.name, this.comment, this.bindObject, this.editDistance, this.elements);
    }

    /**
     * Sets the name for this pattern.
     *
     * @param name the pattern name, must not be null or empty
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern name(String name) {
        return new DrexPattern(this.version, name, this.comment, this.bindObject, this.editDistance, this.elements);
    }

    /**
     * Sets the comment for this pattern.
     *
     * @param comment optional descriptive comment, may be null
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern comment(String comment) {
        return new DrexPattern(this.version, this.name, comment, this.bindObject, this.editDistance, this.elements);
    }

    /**
     * Sets the name of the root JSON object that this pattern creates.
     *
     * @param bindObject the name of the root JSON object, must not be null or empty
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern bindObject(String bindObject) {
        return new DrexPattern(this.version, this.name, this.comment, bindObject, this.editDistance, this.elements);
    }

    /**
     * Sets the maximum edit distance for fuzzy matching.
     *
     * @param editDistance the maximum edit distance for fuzzy matching, must be non-negative
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern editDistance(int editDistance) {
        return new DrexPattern(this.version, this.name, this.comment, this.bindObject, editDistance, this.elements);
    }

    /**
     * Sets the child pattern elements for this pattern.
     *
     * @param elements the child pattern elements, must not be null
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern elements(PatternElement... elements) {
        return new DrexPattern(this.version, this.name, this.comment, this.bindObject, this.editDistance, List.of(elements));
    }

    /**
     * Sets the child pattern elements for this pattern.
     *
     * @param elements the child pattern elements, must not be null
     * @return a new DrexPattern instance for method chaining
     */
    public DrexPattern elements(List<PatternElement> elements) {
        return new DrexPattern(this.version, this.name, this.comment, this.bindObject, this.editDistance, elements);
    }

    /**
     * Completes the fluent construction.
     *
     * @return this DrexPattern instance
     */
    public DrexPattern build() {
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
        DrexPattern that = (DrexPattern) obj;
        return editDistance == that.editDistance &&
               Objects.equals(version, that.version) &&
               Objects.equals(name, that.name) &&
               Objects.equals(comment, that.comment) &&
               Objects.equals(bindObject, that.bindObject) &&
               Objects.equals(elements, that.elements);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(version, name, comment, bindObject, editDistance, elements);
    }

    /**
     * Returns a string representation of this DrexPattern.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "DrexPattern[" +
               "version=" + version +
               ", name=" + name +
               (comment != null ? ", comment=" + comment : "") +
               ", bindObject=" + bindObject +
               ", editDistance=" + editDistance +
               ", elements=" + elements.size() + " elements" +
               "]";
    }
}